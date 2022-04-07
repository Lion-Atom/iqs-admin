package me.zhengjie.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.FileCategory;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.repository.FileCategoryRepository;
import me.zhengjie.repository.LocalStorageRepository;
import me.zhengjie.service.FileCategoryService;
import me.zhengjie.service.dto.FileCategoryDto;
import me.zhengjie.service.dto.FileCategoryQueryCriteria;
import me.zhengjie.service.mapstruct.FileCategoryMapper;
import me.zhengjie.utils.*;
import me.zhengjie.utils.enums.DataScopeEnum;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/4/28 14:03
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "fileCategory")
public class FileCategoryServiceImpl implements FileCategoryService {

    private final FileCategoryRepository fileCategoryRepository;
    private final FileCategoryMapper fileCategoryMapper;
    private final LocalStorageRepository localStorageRepository;
    //    private final UserRepository userRepository;
    private final RedisUtils redisUtils;

    @Override
    public List<FileCategoryDto> queryAll(FileCategoryQueryCriteria criteria, Boolean isQuery) throws Exception {
        Sort sort = Sort.by(Sort.Direction.ASC, "cateSort");
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
        List<FileCategoryDto> list = fileCategoryMapper.toDto(fileCategoryRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), sort));
        // 如果为空，就代表为自定义权限或者本级权限，就需要去重，不理解可以注释掉，看查询结果
        if (StringUtils.isBlank(dataScopeType)) {
            return deduplication(list);
        }
        return list;
    }

    private List<FileCategoryDto> deduplication(List<FileCategoryDto> list) {
        List<FileCategoryDto> levelDtos = new ArrayList<>();
        for (FileCategoryDto categoryDto : list) {
            boolean flag = true;
            for (FileCategoryDto dto : list) {
                if (dto.getId().equals(categoryDto.getPid())) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                levelDtos.add(categoryDto);
            }
        }
        return levelDtos;
    }

    @Override
    public FileCategoryDto findById(Long id) {
        FileCategory category = fileCategoryRepository.findById(id).orElseGet(FileCategory::new);
        ValidationUtil.isNull(category.getId(), "FileCategory", "id", id);
        return fileCategoryMapper.toDto(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(FileCategory resources) {

        //重名校验
        FileCategory category = fileCategoryRepository.findByName(resources.getName());
        if(category != null){
            throw new EntityExistException(FileCategory.class,"name",resources.getName());
        }

        fileCategoryRepository.save(resources);
        // 计算子节点数目
        resources.setSubCount(0);
        // 清理缓存
        updateSubCnt(resources.getPid());
        // 清理自定义文件权限的datascope缓存
        delCaches(resources.getPid());
    }

    private void updateSubCnt(Long pid) {
        if (pid != null) {
            int count = fileCategoryRepository.countByPid(pid);
            fileCategoryRepository.updateSubCntById(count, pid);
        }
    }

    private void delCaches(Long pid) {
        /*List<Approver> users = userRepository.findByRoleDeptId(id);
        // 删除数据权限
        redisUtils.delByKeys(CacheKey.DATA_USER, users.stream().map(Approver::getId).collect(Collectors.toSet()));
        redisUtils.del(CacheKey.DEPT_ID + id);*/
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(FileCategory resources) {
        // 旧的上级
        Long oldPid = findById(resources.getId()).getPid();
        Long newPid = resources.getPid();
        if (resources.getPid() != null && resources.getId().equals(resources.getPid())) {
            throw new BadRequestException("上级不能为自己");
        }
        FileCategory category = fileCategoryRepository.findById(resources.getId()).orElseGet(FileCategory::new);
        //重名校验
        FileCategory old = fileCategoryRepository.findByName(resources.getName());
        if(old != null && !old.getId().equals(resources.getId())){
            throw new EntityExistException(FileCategory.class,"name",resources.getName());
        }
        ValidationUtil.isNull(category.getId(), "FileCategory", "id", resources.getId());
        resources.setId(category.getId());
        fileCategoryRepository.save(resources);
        // 更新父节点中子节点数目
        updateSubCnt(oldPid);
        updateSubCnt(newPid);
        // 清理缓存
        delCaches(resources.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<FileCategoryDto> fileCategoryDtos) {
        for (FileCategoryDto levelDto : fileCategoryDtos) {
            // 清理缓存
            delCaches(levelDto.getId());
            fileCategoryRepository.deleteById(levelDto.getId());
            updateSubCnt(levelDto.getPid());
        }
    }

    @Override
    public List<FileCategory> findByPid(long pid) {
        return fileCategoryRepository.findByPid(pid);
    }

    @Override
    public void download(List<FileCategoryDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (FileCategoryDto categoryDto : queryAll) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("分类名称", categoryDto.getName());
            map.put("分类状态", categoryDto.getEnabled() ? "启用" : "停用");
            map.put("创建日期", categoryDto.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public Set<FileCategoryDto> getDeleteFileCategorys(List<FileCategory> fileCategoryList, Set<FileCategoryDto> fileCategoryDtos) {
        for (FileCategory cate : fileCategoryList) {
            fileCategoryDtos.add(fileCategoryMapper.toDto(cate));
            List<FileCategory> cates = fileCategoryRepository.findByPid(cate.getId());
            if(ValidationUtil.isNotEmpty(cates)){
                getDeleteFileCategorys(cates, fileCategoryDtos);
            }
        }
        return fileCategoryDtos;
    }

    @Override
    public List<FileCategoryDto> getSuperior(FileCategoryDto fileCategoryDto, List<FileCategory> fileCategories) {
        if (fileCategoryDto.getPid() == null) {
            fileCategories.addAll(fileCategoryRepository.findByPidIsNull());
            return fileCategoryMapper.toDto(fileCategories);
        }
        fileCategories.addAll(fileCategoryRepository.findByPid(fileCategoryDto.getPid()));
        return getSuperior(findById(fileCategoryDto.getPid()), fileCategories);
    }

    @Override
    public Object buildTree(List<FileCategoryDto> fileCategoryDtos) {
        Set<FileCategoryDto> trees = new LinkedHashSet<>();
        Set<FileCategoryDto> depts = new LinkedHashSet<>();
        List<String> deptNames = fileCategoryDtos.stream().map(FileCategoryDto::getName).collect(Collectors.toList());
        boolean isChild;
        for (FileCategoryDto fileCategoryDto : fileCategoryDtos) {
            isChild = false;
            if (fileCategoryDto.getPid() == null) {
                trees.add(fileCategoryDto);
            }
            for (FileCategoryDto it : fileCategoryDtos) {
                if (it.getPid() != null && fileCategoryDto.getId().equals(it.getPid())) {
                    isChild = true;
                    if (fileCategoryDto.getChildren() == null) {
                        fileCategoryDto.setChildren(new ArrayList<>());
                    }
                    fileCategoryDto.getChildren().add(it);
                }
            }
            if (isChild) {
                depts.add(fileCategoryDto);
            } else if (fileCategoryDto.getPid() != null && !deptNames.contains(findById(fileCategoryDto.getPid()).getName())) {
                depts.add(fileCategoryDto);
            }
        }

        if (CollectionUtil.isEmpty(trees)) {
            trees = depts;
        }
        Map<String, Object> map = new HashMap<>(2);
        map.put("totalElements", fileCategoryDtos.size());
        map.put("content", CollectionUtil.isEmpty(trees) ? fileCategoryDtos : trees);
        return map;
    }

    @Override
    public List<Long> getFileCategoryChildren(List<FileCategory> fileCategoryList) {
        List<Long> list = new ArrayList<>();
        fileCategoryList.forEach(cate -> {
                    if (cate!=null && cate.getEnabled()) {
                        List<FileCategory> categories = fileCategoryRepository.findByPid(cate.getId());
                        if (categories.size() != 0) {
                            list.addAll(getFileCategoryChildren(categories));
                        }
                        list.add(cate.getId());
                    }
                }
        );
        return list;
    }

    @Override
    public void verification(Set<FileCategoryDto> fileCategoryDtos) {
        Set<Long> levelIds = fileCategoryDtos.stream().map(FileCategoryDto::getId).collect(Collectors.toSet());
        if(localStorageRepository.countByCateIds(levelIds) > 0){
            throw new BadRequestException("Exists Files in this category,please release the file and try again!所选分类有文件存放，请解除文件后再试！");
        }
    }
}
