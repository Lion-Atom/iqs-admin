package me.zhengjie.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.FileLevel;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.repository.LocalStorageRepository;
import me.zhengjie.service.FileLevelService;
import me.zhengjie.service.dto.FileLevelDto;
import me.zhengjie.service.dto.FileLevelQueryCriteria;
import me.zhengjie.repository.FileLevelRepository;
import me.zhengjie.service.mapstruct.FileLevelMapper;
import me.zhengjie.utils.*;
import me.zhengjie.utils.enums.DataScopeEnum;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/4/23 15:37
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "fileLevel")
public class FileLevelServiceImpl implements FileLevelService {

    private final FileLevelRepository fileLevelRepository;
    private final FileLevelMapper fileLevelMapper;
    //    private final UserRepository userRepository;
    private final RedisUtils redisUtils;
    private final LocalStorageRepository localStorageRepository;

    @Override
    public List<FileLevelDto> queryAll(FileLevelQueryCriteria criteria, Boolean isQuery) throws Exception {
        Sort sort = Sort.by(Sort.Direction.ASC, "levelSort");
        String dataScopeType = SecurityUtils.getDataScopeType();
        if (isQuery) {
            if (dataScopeType.equals(DataScopeEnum.ALL.getValue())) {
                criteria.setPidIsNull(true);
            }
            List<Field> fields = QueryHelp.getAllFields(criteria.getClass(), new ArrayList<>());
            List<String> fieldNames = new ArrayList<String>() {{
                add("pidIsNull");
                add("enabled");
            }};
            for (Field field : fields) {
                //设置对象的访问权限，保证对private的属性的访问
                field.setAccessible(true);
                Object val = field.get(criteria);
                if (fieldNames.contains(field.getName())) {
                    continue;
                }
                if (ObjectUtil.isNotNull(val)) {
                    criteria.setPidIsNull(null);
                    break;
                }
            }
        }
        List<FileLevelDto> list = fileLevelMapper.toDto(fileLevelRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), sort));
        // 如果为空，就代表为自定义权限或者本级权限，就需要去重，不理解可以注释掉，看查询结果
        if (StringUtils.isBlank(dataScopeType)) {
            return deduplication(list);
        }
        return list;
    }

    private List<FileLevelDto> deduplication(List<FileLevelDto> list) {
        List<FileLevelDto> levelDtos = new ArrayList<>();
        for (FileLevelDto levelDto : list) {
            boolean flag = true;
            for (FileLevelDto dto : list) {
                if (dto.getId().equals(levelDto.getPid())) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                levelDtos.add(levelDto);
            }
        }
        return levelDtos;
    }

    @Override
    public FileLevelDto findById(Long id) {
        FileLevel level = fileLevelRepository.findById(id).orElseGet(FileLevel::new);
        ValidationUtil.isNull(level.getId(), "FileLevel", "id", id);
        return fileLevelMapper.toDto(level);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(FileLevel resources) {
        //新增判断仅且只能有唯一的一级节点
        if (resources.getPid() == null) {
            List<FileLevel> list = fileLevelRepository.findByPidIsNull();
            if (ValidationUtil.isNotEmpty(list)) {
                String msg = "一级节点已经存在不可再设置，当前系统有且仅允许一个一级文件等级存在";
                throw new BadRequestException(msg);
            }
        }

        //重名校验
        FileLevel level = fileLevelRepository.findByName(resources.getName());
        if(level != null){
            throw new EntityExistException(FileLevel.class,"name",resources.getName());
        }

        fileLevelRepository.save(resources);
        // 计算子节点数目
        resources.setSubCount(0);
        // 清理缓存
        updateSubCnt(resources.getPid());
        // 清理自定义文件权限的datascope缓存
        delCaches(resources.getPid());
    }

    private void updateSubCnt(Long pid) {
        if (pid != null) {
            int count = fileLevelRepository.countByPid(pid);
            fileLevelRepository.updateSubCntById(count, pid);
        }
    }

    //文件等级很少，因此不需要设置缓存，不需要删除
    private void delCaches(Long pid) {
        /*
        // 删除数据权限
        redisUtils.delByKeys(CacheKey.DATA_USER, users.stream().map(Approver::getId).collect(Collectors.toSet()));
        redisUtils.del(CacheKey.DEPT_ID + id);*/
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(FileLevel resources) {
        // 旧的上级
        Long oldPid = findById(resources.getId()).getPid();
        Long newPid = resources.getPid();
        if (resources.getPid() != null && resources.getId().equals(resources.getPid())) {
            throw new BadRequestException("上级不能为自己");
        }
        FileLevel dept = fileLevelRepository.findById(resources.getId()).orElseGet(FileLevel::new);
        //重名校验
        FileLevel old = fileLevelRepository.findByName(resources.getName());
        if(old != null && !old.getId().equals(resources.getId())){
            throw new EntityExistException(FileLevel.class,"name",resources.getName());
        }
        ValidationUtil.isNull(dept.getId(), "FileLevel", "id", resources.getId());
        resources.setId(dept.getId());
        if (resources.getPid() == null && dept.getPid() != null) {
            String msg = "一级节点已经存在不可再设置，当前系统有且仅允许一个一级文件等级存在";
            throw new BadRequestException(msg);
        }
        fileLevelRepository.save(resources);
        // 更新父节点中子节点数目
        updateSubCnt(oldPid);
        updateSubCnt(newPid);
        // 清理缓存
        delCaches(resources.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<FileLevelDto> fileLevelDtos) {
        for (FileLevelDto levelDto : fileLevelDtos) {
            // 清理缓存
            delCaches(levelDto.getId());
            fileLevelRepository.deleteById(levelDto.getId());
            updateSubCnt(levelDto.getPid());
        }
    }

    @Override
    public List<FileLevel> findByPid(long pid) {
        return fileLevelRepository.findByPid(pid);
    }

    @Override
    public void download(List<FileLevelDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (FileLevelDto deptDTO : queryAll) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("等级名称", deptDTO.getName());
            map.put("等级状态", deptDTO.getEnabled() ? "启用" : "停用");
            map.put("创建日期", deptDTO.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public Set<FileLevelDto> getDeleteFileLevels(List<FileLevel> fileLevelList, Set<FileLevelDto> fileLevelDtos) {
        for (FileLevel level : fileLevelList) {
            fileLevelDtos.add(fileLevelMapper.toDto(level));
            List<FileLevel> levels = fileLevelRepository.findByPid(level.getId());
            if(ValidationUtil.isNotEmpty(levels)){
                getDeleteFileLevels(levels, fileLevelDtos);
            }
        }
        return fileLevelDtos;
    }

    @Override
    public List<FileLevelDto> getSuperior(FileLevelDto fileLevelDto, List<FileLevel> fileLevels) {
        if (fileLevelDto.getPid() == null) {
            fileLevels.addAll(fileLevelRepository.findByPidIsNull());
            return fileLevelMapper.toDto(fileLevels);
        }
        fileLevels.addAll(fileLevelRepository.findByPid(fileLevelDto.getPid()));
        return getSuperior(findById(fileLevelDto.getPid()), fileLevels);
    }

    @Override
    public Object buildTree(List<FileLevelDto> fileLevelDtos) {
        Set<FileLevelDto> trees = new LinkedHashSet<>();
        Set<FileLevelDto> depts = new LinkedHashSet<>();
        List<String> deptNames = fileLevelDtos.stream().map(FileLevelDto::getName).collect(Collectors.toList());
        boolean isChild;
        for (FileLevelDto fileLevelDto : fileLevelDtos) {
            isChild = false;
            if (fileLevelDto.getPid() == null) {
                trees.add(fileLevelDto);
            }
            for (FileLevelDto it : fileLevelDtos) {
                if (it.getPid() != null && fileLevelDto.getId().equals(it.getPid())) {
                    isChild = true;
                    if (fileLevelDto.getChildren() == null) {
                        fileLevelDto.setChildren(new ArrayList<>());
                    }
                    fileLevelDto.getChildren().add(it);
                }
            }
            if (isChild) {
                depts.add(fileLevelDto);
            } else if (fileLevelDto.getPid() != null && !deptNames.contains(findById(fileLevelDto.getPid()).getName())) {
                depts.add(fileLevelDto);
            }
        }

        if (CollectionUtil.isEmpty(trees)) {
            trees = depts;
        }
        Map<String, Object> map = new HashMap<>(2);
        map.put("totalElements", fileLevelDtos.size());
        map.put("content", CollectionUtil.isEmpty(trees) ? fileLevelDtos : trees);
        return map;
    }

    @Override
    public List<Long> getFileLevelChildren(List<FileLevel> FileLevelList) {
        return null;
    }

    @Override
    public void verification(Set<FileLevelDto> fileLevelDtos) {
        Set<Long> levelIds = fileLevelDtos.stream().map(FileLevelDto::getId).collect(Collectors.toSet());
        if(localStorageRepository.countByLevelIds(levelIds) > 0){
            throw new BadRequestException("Exists Files in this level or its children,please release the file and try again!所选等级或其子级有关联文件，请解除文件后再试！");
        }
    }
}
