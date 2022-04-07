/*
 *  Copyright 2019-2020 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package me.zhengjie.modules.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.FileDept;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.modules.system.domain.Dept;
import me.zhengjie.modules.system.domain.User;
import me.zhengjie.modules.system.repository.FileRepository;
import me.zhengjie.modules.system.repository.RoleRepository;
import me.zhengjie.modules.system.repository.UserRepository;
import me.zhengjie.modules.system.service.dto.DeptDto;
import me.zhengjie.modules.system.service.dto.DeptFileDto;
import me.zhengjie.modules.system.service.dto.DeptQueryCriteria;
import me.zhengjie.modules.system.service.dto.DeptQueryCriteriaV2;
import me.zhengjie.modules.system.service.mapstruct.DeptFileMapper;
import me.zhengjie.repository.LocalStorageRepository;
import me.zhengjie.service.FileDeptService;
import me.zhengjie.utils.*;
import me.zhengjie.modules.system.repository.DeptRepository;
import me.zhengjie.modules.system.service.DeptService;
import me.zhengjie.modules.system.service.mapstruct.DeptMapper;
import me.zhengjie.utils.enums.DataScopeEnum;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Zheng Jie
 * @date 2019-03-25
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "dept")
public class DeptServiceImpl implements DeptService {

    private final DeptRepository deptRepository;
    private final DeptMapper deptMapper;
    private final DeptFileMapper deptFileMapper;
    private final UserRepository userRepository;
    private final RedisUtils redisUtils;
    private final RoleRepository roleRepository;
    private final LocalStorageRepository fileRepository;
    private final FileDeptService fileDeptService;

    @Override
    public List<DeptDto> queryAll(DeptQueryCriteria criteria, Boolean isQuery) throws Exception {
        Sort sort = Sort.by(Sort.Direction.ASC, "deptSort");
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
        List<DeptDto> list = deptMapper.toDto(deptRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), sort));
        // 如果为空，就代表为自定义权限或者本级权限，就需要去重，不理解可以注释掉，看查询结果
        if (StringUtils.isBlank(dataScopeType)) {
            return deduplication(list);
        }
        return list;
    }

    @Override
    public List<DeptFileDto> queryAllV2(DeptQueryCriteria criteria, Boolean isQuery) throws Exception {
        Sort sort = Sort.by(Sort.Direction.ASC, "deptSort");
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
        List<DeptFileDto> list = deptFileMapper.toDto(deptRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), sort));
        // 部门对应的文件数量
        setDeptFileCount(list);
        if (StringUtils.isBlank(dataScopeType)) {
            return deduplicationV2(list);
        }
        return list;
    }

    @Override
    @Cacheable(key = "'id:' + #p0")
    public DeptDto findById(Long id) {
        Dept dept = deptRepository.findById(id).orElseGet(Dept::new);
        ValidationUtil.isNull(dept.getId(), "Dept", "id", id);
        return deptMapper.toDto(dept);
    }

    @Override
    public List<Dept> findByPid(long pid) {
        return deptRepository.findByPid(pid);
    }

    @Override
    public Set<Dept> findByRoleId(Long id) {
        return deptRepository.findByRoleId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Dept resources) {
        // 部门同名校验
        String name = resources.getName();
        // 遍历-查找到顶级部门
        if (resources.getPid() == null) {
            //顶级部门-既不能跟统级别顶级部门重名，也不可以跟子集重名(但新建时候不可能存在子集)
            List<Dept> deptList = deptRepository.findByNameAndPidIsNull(name);
            if (ValidationUtil.isNotEmpty(deptList)) {
                throw new BadRequestException("同级部门名称已存在，请重新命名");
            }
        } else {
            // 遍历-查找到顶级部门
            Long pid = resources.getPid();
            Long topDeptId;
            List<String> rList = new ArrayList<>();
            do {
                Dept pDept = deptRepository.findById(pid).orElseGet(Dept::new);
                rList.add(pDept.getName());
                pid = pDept.getPid();
                topDeptId = pDept.getId();
            } while (pid != null);
            // 根据顶级部门id找寻所有子集部门名称
            List<Dept> data = deptRepository.findByPid(topDeptId);
            // 然后把子节点的ID都加入到集合中
            rList.addAll(getDeptChildrenName(data));
            // rList必有重复项，不用理会，无关功能
            if (rList.contains(resources.getName())) {
                throw new BadRequestException("同一个顶级部门下禁止部门重名，请重新命名");
            }
        }

        deptRepository.save(resources);
        // 计算子节点数目
        resources.setSubCount(0);
        // 清理缓存
        updateSubCnt(resources.getPid());
        // 清理自定义角色权限的datascope缓存
        delCaches(resources.getPid());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Dept resources) {
        // 旧的部门
        Long oldPid = findById(resources.getId()).getPid();
        Long newPid = resources.getPid();
        if (resources.getPid() != null && resources.getId().equals(resources.getPid())) {
            throw new BadRequestException("上级不能为自己");
        }
        // 重名校验
        if (resources.getPid() == null) {
            //顶级部门
            List<Dept> deptList = deptRepository.findByNameAndPidIsNullAndIsNotSelf(resources.getName(), resources.getId());
            if (ValidationUtil.isNotEmpty(deptList)) {
                throw new BadRequestException("同级部门名称已存在，请重新重名");
            }
        } else {
            // 遍历-查找到顶级部门
            Long topDeptId;
            do {
                Dept pDept = deptRepository.findById(newPid).orElseGet(Dept::new);
                newPid = pDept.getPid();
                topDeptId = pDept.getId();
            } while (newPid != null);
            // 根据顶级部门id找寻所有子集部门名称
            List<Dept> data = deptRepository.findByPid(topDeptId);
            // 然后把子节点的ID都加入到集合中
            Set<Long> rList = new HashSet<>(getDeptChildren(data));
            // rList必有重复项
            rList.add(topDeptId);
            rList.removeIf(val -> val.equals(resources.getId()));
            List<String> nameList = deptRepository.findAllByIdAndName(resources.getName(), rList);
            if (nameList.contains(resources.getName())) {
                throw new BadRequestException("同一个顶级部门下禁止部门重名，请重新命名");
            }
        }
        Dept dept = deptRepository.findById(resources.getId()).orElseGet(Dept::new);
        ValidationUtil.isNull(dept.getId(), "Dept", "id", resources.getId());
        resources.setId(dept.getId());
        deptRepository.save(resources);
        // 更新父节点中子节点数目
        updateSubCnt(oldPid);
        updateSubCnt(newPid);
        // 清理缓存
        delCaches(resources.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<DeptDto> deptDtos) {
        for (DeptDto deptDto : deptDtos) {
            // 清理缓存
            delCaches(deptDto.getId());
            deptRepository.deleteById(deptDto.getId());
            updateSubCnt(deptDto.getPid());
        }
    }

    @Override
    public void download(List<DeptDto> deptDtos, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (DeptDto deptDTO : deptDtos) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("部门名称", deptDTO.getName());
            map.put("部门状态", deptDTO.getEnabled() ? "启用" : "停用");
            map.put("创建日期", deptDTO.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public void downloadV2(List<DeptFileDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (DeptFileDto deptDTO : queryAll) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("部门名称", deptDTO.getName());
            map.put("文件数量", deptDTO.getFileCount());
            if (deptDTO.getPid() != null) {
                Dept dept = deptRepository.findById(deptDTO.getPid()).orElseGet(Dept::new);
                String superiorName = dept.getName();
                map.put("上级部门", superiorName);
            }
            map.put("部门状态", deptDTO.getEnabled() ? "启用" : "停用");
            map.put("创建日期", deptDTO.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public Set<DeptDto> getDeleteDepts(List<Dept> menuList, Set<DeptDto> deptDtos) {
        for (Dept dept : menuList) {
            deptDtos.add(deptMapper.toDto(dept));
            List<Dept> depts = deptRepository.findByPid(dept.getId());
            if (depts != null && depts.size() != 0) {
                getDeleteDepts(depts, deptDtos);
            }
        }
        return deptDtos;
    }

    @Override
    public List<Long> getDeptChildren(List<Dept> deptList) {
        List<Long> list = new ArrayList<>();
        deptList.forEach(dept -> {
                    if (dept != null && dept.getEnabled()) {
                        List<Dept> depts = deptRepository.findByPid(dept.getId());
                        if (deptList.size() != 0) {
                            list.addAll(getDeptChildren(depts));
                        }
                        list.add(dept.getId());
                    }
                }
        );
        return list;
    }

    public List<String> getDeptChildrenName(List<Dept> deptList) {
        List<String> list = new ArrayList<>();
        deptList.forEach(dept -> {
                    if (dept != null && dept.getEnabled()) {
                        List<Dept> depts = deptRepository.findByPid(dept.getId());
                        if (deptList.size() != 0) {
                            list.addAll(getDeptChildrenName(depts));
                        }
                        list.add(dept.getName());
                    }
                }
        );
        return list;
    }

    @Override
    public List<DeptDto> getSuperior(DeptDto deptDto, List<Dept> depts) {
        if (SecurityUtils.getIsAdmin()) {
            // 管理员可选全部
            if (deptDto.getPid() == null) {
                depts.addAll(deptRepository.findByPidIsNull());
                return deptMapper.toDto(depts);
            }
            depts.addAll(deptRepository.findByPid(deptDto.getPid()));
            return getSuperior(findById(deptDto.getPid()), depts);
        } else {
            Long currDeptId = SecurityUtils.getCurrentDeptId();
            List<Long> deptIds = new ArrayList<>();
            deptIds.add(currDeptId);
            // 先查找是否存在子节点
            List<FileDept> data = fileDeptService.findByPid(currDeptId);
            // 然后把子节点的ID都加入到集合中
            deptIds.addAll(fileDeptService.getDeptChildren(data));
            return deptMapper.toDto(deptRepository.findAllById(deptIds));
        }
    }

    @Override
    public Object buildTree(List<DeptDto> deptDtos) {
        Set<DeptDto> trees = new LinkedHashSet<>();
        Set<DeptDto> depts = new LinkedHashSet<>();
        List<String> deptNames = deptDtos.stream().map(DeptDto::getName).collect(Collectors.toList());
        boolean isChild;
        for (DeptDto deptDTO : deptDtos) {
            isChild = false;
            if (deptDTO.getPid() == null) {
                trees.add(deptDTO);
            }
            for (DeptDto it : deptDtos) {
                if (it.getPid() != null && deptDTO.getId().equals(it.getPid())) {
                    isChild = true;
                    if (deptDTO.getChildren() == null) {
                        deptDTO.setChildren(new ArrayList<>());
                    }
                    deptDTO.getChildren().add(it);
                }
            }
            if (isChild) {
                depts.add(deptDTO);
            } else if (deptDTO.getPid() != null && !deptNames.contains(findById(deptDTO.getPid()).getName())) {
                depts.add(deptDTO);
            }
        }

        if (CollectionUtil.isEmpty(trees)) {
            trees = depts;
        }
        Map<String, Object> map = new HashMap<>(2);
        map.put("totalElements", deptDtos.size());
        map.put("content", CollectionUtil.isEmpty(trees) ? deptDtos : trees);
        return map;
    }

    @Override
    public void verification(Set<DeptDto> deptDtos) {
        Set<Long> deptIds = deptDtos.stream().map(DeptDto::getId).collect(Collectors.toSet());
        //部门和用户
        if (userRepository.countByDepts(deptIds) > 0) {
            throw new BadRequestException("所选部门存在用户关联，请解除后再试！");
        }
        //部门和角色
        if (roleRepository.countByDepts(deptIds) > 0) {
            throw new BadRequestException("所选部门存在角色关联，请解除后再试！");
        }
        //部门和文件
        if (fileRepository.countByDepts(deptIds) > 0) {
            throw new BadRequestException("所选部门存在文件关联，请解除后再试！");
        }
    }

    @Override
    public List<DeptFileDto> queryDeptByExample(DeptQueryCriteriaV2 criteria) {
        List<DeptFileDto> list = deptFileMapper.toDto(deptRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder)));
        // 部门对应的文件数量
        setDeptFileCount(list);
        return list;
    }

    @Override
    public List<DeptDto> getDeptTree(DeptDto deptDto) {
        List<DeptDto> deptDtos = new ArrayList<>();
        Long topId = null;
        if (deptDto.getPid() != null) {
            // 1.获取顶级部门ID
            Long pid = deptDto.getPid();
            do {
                Dept pDept = deptRepository.findById(pid).orElseGet(Dept::new);
                pid = pDept.getPid();
                topId = pDept.getId();
            } while (pid != null);
            // 2.获取顶级部门ID
        } else {
            // 此为top级部门
            topId = deptDto.getId();
        }
        Dept dept = deptRepository.findById(topId).orElseGet(Dept::new);
        deptDtos.add(deptMapper.toDto(dept));
        // 2.获取一级部门及其子集
        List<Dept> deptList = deptRepository.findByPid(topId);
        if (ValidationUtil.isNotEmpty(deptList)) {
            Set<Long> rList = new HashSet<>(getDeptChildren(deptList));
            List<Dept> depts = deptRepository.findAllByIdIn(rList);
            deptDtos.addAll(deptMapper.toDto(depts));
        }
        deptDtos.addAll(deptMapper.toDto(deptList));
        return deptDtos;
    }

    @Override
    public Set<Long> getSuperiorIds(Long deptId) {
        Dept dept = deptRepository.findById(deptId).orElseGet(Dept::new);
        Set<Long> set = new HashSet<>();
        if (dept != null && dept.getPid() != null) {
            // 1.获取顶级部门ID
            Long pid = dept.getPid();
            set.add(pid);
            do {
                Dept pDept = deptRepository.findById(pid).orElseGet(Dept::new);
                pid = pDept.getPid();
                set.add(pid);
            } while (pid != null);
        }
        return set;
    }


    private void setDeptFileCount(List<DeptFileDto> list) {
        if (ValidationUtil.isNotEmpty(list)) {
            list.forEach(dept -> {
                Long deptId = dept.getId();
                //获取子级
                Set<Long> deptIds = new HashSet<>();
                deptIds.add(deptId);
                // 先查找是否存在子节点
                List<FileDept> data = fileDeptService.findByPid(deptId);
                // 然后把子节点的ID都加入到集合中
                deptIds.addAll(fileDeptService.getDeptChildren(data));
                Integer fileCount = fileRepository.getCountByDeptIdIn(deptIds);
                // 部门对应文件数量
                dept.setFileCount(fileCount);
            });
        }
    }

    private void updateSubCnt(Long deptId) {
        if (deptId != null) {
            int count = deptRepository.countByPid(deptId);
            deptRepository.updateSubCntById(count, deptId);
        }
    }

    private List<DeptFileDto> deduplicationV2(List<DeptFileDto> list) {
        List<DeptFileDto> deptDtos = new ArrayList<>();
        for (DeptFileDto deptDto : list) {
            boolean flag = true;
            for (DeptFileDto dto : list) {
                if (dto.getId().equals(deptDto.getPid())) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                deptDtos.add(deptDto);
            }
        }
        return deptDtos;
    }

    private List<DeptDto> deduplication(List<DeptDto> list) {
        List<DeptDto> deptDtos = new ArrayList<>();
        for (DeptDto deptDto : list) {
            boolean flag = true;
            for (DeptDto dto : list) {
                if (dto.getId().equals(deptDto.getPid())) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                deptDtos.add(deptDto);
            }
        }
        return deptDtos;
    }

    /**
     * 清理缓存
     *
     * @param id /
     */
    public void delCaches(Long id) {
        List<User> users = userRepository.findByRoleDeptId(id);
        // 删除数据权限
        redisUtils.delByKeys(CacheKey.DATA_USER, users.stream().map(User::getId).collect(Collectors.toSet()));
        redisUtils.del(CacheKey.DEPT_ID + id);
    }
}
