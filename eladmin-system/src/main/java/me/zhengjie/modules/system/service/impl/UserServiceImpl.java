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

import lombok.RequiredArgsConstructor;
import me.zhengjie.config.FileProperties;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.*;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.modules.quartz.task.TestTask;
import me.zhengjie.modules.security.service.OnlineUserService;
import me.zhengjie.modules.security.service.UserCacheClean;
import me.zhengjie.modules.system.domain.Dept;
import me.zhengjie.modules.system.domain.Job;
import me.zhengjie.modules.system.domain.User;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.exception.EntityNotFoundException;
import me.zhengjie.modules.system.repository.DeptRepository;
import me.zhengjie.modules.system.repository.UserRepository;
import me.zhengjie.modules.system.service.UserService;
import me.zhengjie.modules.system.service.dto.*;
import me.zhengjie.modules.system.service.mapstruct.UserMapper;
import me.zhengjie.repository.ToolsUserRepository;
import me.zhengjie.repository.TrainCertificationRepository;
import me.zhengjie.repository.TrainExamStaffRepository;
import me.zhengjie.repository.TrainNewStaffRepository;
import me.zhengjie.service.dto.ToolsUserDto;
import me.zhengjie.service.mapstruct.ToolsUserMapper;
import me.zhengjie.utils.*;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Zheng Jie
 * @date 2018-11-23
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "user")
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final FileProperties properties;
    private final RedisUtils redisUtils;
    private final UserCacheClean userCacheClean;
    private final OnlineUserService onlineUserService;
    private final DeptRepository deptRepository;
    private final TrainNewStaffRepository staffTrainRepository;
    private final TrainExamStaffRepository examStaffRepository;
    private final TrainCertificationRepository trainCertRepository;

    @Override
    public Map<String, Object> queryAll(UserQueryCriteria criteria, Pageable pageable) {
        Map<String, Object> map = new HashMap<>();
        List<UserDto> list = new ArrayList<>();
        long total = 0L;
        if (criteria.getQueryAll()) {
            criteria.setDeptIds(null);
        }
        Page<User> page = userRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        // 获取上级主管和部门信息
        if (ValidationUtil.isNotEmpty(page.getContent())) {
            list = userMapper.toDto(page.getContent());
            // 上级部门ID集合
            Set<Long> pidList = new HashSet<>();
            Map<Long, String> pmMap = new HashMap<>();
            // 员工ID集合
            Set<Long> idList = new HashSet<>();
            Map<Long, String> nameMap = new HashMap<>();
            list.forEach(user -> {
                if (user.getDept() != null) {
                    user.setDeptId(user.getDept().getId());
                    user.setDeptName(user.getDept().getName());
                }
                if (user.getSuperiorId() != null) {
                    idList.add(user.getSuperiorId());
                } else if (user.getDept() != null && user.getDept().getPid() != null) {
                    pidList.add(user.getDept().getPid());
                }
                if (ValidationUtil.isNotEmpty(Collections.singletonList(user.getJobs()))) {
                    List<JobSmallDto> jobList = new ArrayList<>(user.getJobs());
                    user.setJobName(jobList.get(0).getName());
                }
            });
            List<User> superiors = userRepository.findAllById(idList);
            List<User> pDeptMasterList = userRepository.findByDeptIdInAndIsMaster(pidList, true);
            if (ValidationUtil.isNotEmpty(pDeptMasterList)) {
                pDeptMasterList.forEach(pm -> {
                    pmMap.put(pm.getDept().getId(), pm.getUsername());
                });
            }
            if (ValidationUtil.isNotEmpty(superiors)) {
                superiors.forEach(superior -> {
                    nameMap.put(superior.getId(), superior.getUsername());
                });
            }
            list.forEach(user -> {
                if (user.getSuperiorId() != null) {
                    user.setSuperiorName(nameMap.get(user.getSuperiorId()));
                } else if (user.getDept() != null) {
                    // 向上找上级
                    if (user.getDept().getPid() != null) {
                        user.setSuperiorName(pmMap.get(user.getDept().getPid()));
                    } else {
                        // 找不到默认为自身
                        user.setSuperiorName(user.getUsername());
                    }
                }
            });
            total = page.getTotalElements();
        }
        //  PageUtil.toPage(page.map(userMapper::toDto))
        map.put("content", list);
        map.put("totalElements", total);
        return map;
    }

    @Override
    public List<UserDto> queryAll(UserQueryCriteria criteria) {
        List<User> users = userRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
        return userMapper.toDto(users);
    }

    @Override
    @Cacheable(key = "'id:' + #p0")
    @Transactional(rollbackFor = Exception.class)
    public UserDto findById(long id) {
        User user = userRepository.findById(id).orElseGet(User::new);
        ValidationUtil.isNull(user.getId(), "Approver", "id", id);
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(User resources) {
        if (userRepository.findByUsername(resources.getUsername()) != null) {
            throw new EntityExistException(User.class, "username", resources.getUsername());
        }
        if (userRepository.findByEmail(resources.getEmail()) != null) {
            throw new EntityExistException(User.class, "email", resources.getEmail());
        }
        if (userRepository.findByPhone(resources.getPhone()) != null) {
            throw new EntityExistException(User.class, "phone", resources.getPhone());
        }
        // 部门master只能有一个
        Dept dept = resources.getDept();
        if (resources.getIsDepartMaster()) {
            List<User> userList = userRepository.findByDeptIdAndIsMaster(dept.getId(), true);
            if (ValidationUtil.isNotEmpty(userList)) {
                throw new BadRequestException("所选部门已有管理者[" + userList.get(0).getUsername() + "]了，请解除后再试！");
            }
        }
        userRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(User resources) throws Exception {
        User user = userRepository.findById(resources.getId()).orElseGet(User::new);
        ValidationUtil.isNull(user.getId(), "Approver", "id", resources.getId());
        // 部门master唯一性判断
        if (resources.getIsDepartMaster()) {
            Dept dept = resources.getDept();
            List<User> userList = userRepository.findByDeptIdAndIsMaster(dept.getId(), true);
            userList.removeIf(dto -> dto.getId().equals(resources.getId()));
            if (ValidationUtil.isNotEmpty(userList)) {
                throw new BadRequestException("所选部门已有管理者[" + userList.get(0).getUsername() + "]了，请解除后再试！");
            }
        }
        User user1 = userRepository.findByUsername(resources.getUsername());
        User user2 = userRepository.findByEmail(resources.getEmail());
        User user3 = userRepository.findByPhone(resources.getPhone());
        if (user1 != null && !user.getId().equals(user1.getId())) {
            throw new EntityExistException(User.class, "username", resources.getUsername());
        }
        if (user2 != null && !user.getId().equals(user2.getId())) {
            throw new EntityExistException(User.class, "email", resources.getEmail());
        }
        if (user3 != null && !user.getId().equals(user3.getId())) {
            throw new EntityExistException(User.class, "phone", resources.getPhone());
        }
        // 如果用户的角色改变
        if (!resources.getRoles().equals(user.getRoles())) {
            redisUtils.del(CacheKey.DATA_USER + resources.getId());
            redisUtils.del(CacheKey.MENU_USER + resources.getId());
            redisUtils.del(CacheKey.ROLE_AUTH + resources.getId());
        }
        // 如果用户被禁用，则清除用户登录信息
        if (!resources.getEnabled()) {
            onlineUserService.kickOutForUsername(resources.getUsername());
        }
        user.setUsername(resources.getUsername());
        user.setEmail(resources.getEmail());
        user.setEnabled(resources.getEnabled());
        user.setRoles(resources.getRoles());
        user.setDept(resources.getDept());
        user.setJobs(resources.getJobs());
        user.setPhone(resources.getPhone());
        user.setNickName(resources.getNickName());
        user.setHireDate(resources.getHireDate());
        user.setStaffType(resources.getStaffType());
        user.setJobType(resources.getJobType());
        user.setJobNum(resources.getJobNum());
        user.setWorkshop(resources.getWorkshop());
        user.setTeam(resources.getTeam());
        user.setGender(resources.getGender());
        user.setSuperiorId(resources.getSuperiorId());
        user.setIsDepartMaster(resources.getIsDepartMaster());
        userRepository.save(user);
        // 清除缓存
        delCaches(user.getId(), user.getUsername());
        // 同步更新员工信息
        Long userId = resources.getId();
        UserDto userDto = getToolsUserDto(user);
        // 同步更新培训记录
        syncUserInfo(userId, userDto);
    }

    private void syncUserInfo(Long userId, UserDto userDto) {
        // 同步更新员工培训记录
        syncTrNewStaffInfo(userId, userDto);
        // 同步更新员工考试信息
        syncExamStaffInfo(userId, userDto);
        // 同步更新员工证书信息
        syncStaffCertInfo(userId, userDto);
    }

    private void syncStaffCertInfo(Long userId, UserDto userDto) {
        List<TrainCertification> certs = trainCertRepository.findAllByUserId(userId);
        if (ValidationUtil.isNotEmpty(certs)) {
            certs.forEach(cert -> {
//                cert.setStaffType(user.getStaffType());
                cert.setJobType(userDto.getJobType());
                cert.setStaffName(userDto.getUsername());
                cert.setHireDate(userDto.getHireDate());
                cert.setDepartId(userDto.getDeptId());
//                cert.setWorkshop(user.getWorkshop());
//                cert.setTeam(user.getTeam());
                cert.setSuperior(userDto.getSuperiorName());
                cert.setJobNum(userDto.getJobNum());
                cert.setJobName(userDto.getJobName());
            });
            trainCertRepository.saveAll(certs);
        }
    }

    private void syncExamStaffInfo(Long userId, UserDto userDto) {
        List<TrainExamStaff> examStaffs = examStaffRepository.findAllByUserId(userId);
        if (ValidationUtil.isNotEmpty(examStaffs)) {
            examStaffs.forEach(examStaff -> {
                examStaff.setStaffType(userDto.getStaffType());
                examStaff.setJobType(userDto.getJobType());
                examStaff.setStaffName(userDto.getUsername());
                examStaff.setHireDate(userDto.getHireDate());
                examStaff.setDepartId(userDto.getDeptId());
                examStaff.setWorkshop(userDto.getWorkshop());
                examStaff.setTeam(userDto.getTeam());
                examStaff.setSuperior(userDto.getSuperiorName());
                examStaff.setJobNum(userDto.getJobNum());
                examStaff.setJobName(userDto.getJobName());
            });
            examStaffRepository.saveAll(examStaffs);
        }
    }

    private void syncTrNewStaffInfo(Long userId, UserDto userDto) {
        List<TrainNewStaff> newStaffList = staffTrainRepository.findAllByUserId(userId);
        if (ValidationUtil.isNotEmpty(newStaffList)) {
            newStaffList.forEach(newStaff -> {
                newStaff.setStaffType(userDto.getStaffType());
                newStaff.setJobType(userDto.getJobType());
                newStaff.setStaffName(userDto.getUsername());
                newStaff.setHireDate(userDto.getHireDate());
                newStaff.setDepartId(userDto.getDeptId());
                newStaff.setWorkshop(userDto.getWorkshop());
                newStaff.setTeam(userDto.getTeam());
                newStaff.setSuperior(userDto.getSuperiorName());
                newStaff.setJobNum(userDto.getJobNum());
                newStaff.setJobName(userDto.getJobName());
            });
            staffTrainRepository.saveAll(newStaffList);
        }
    }

    private UserDto getToolsUserDto(User entity) {
        User user = userRepository.findById(entity.getId()).orElseGet(User::new);
        ValidationUtil.isNull(user.getId(), "User", "id", entity.getId());
        UserDto userDto = userMapper.toDto(user);
        if (ValidationUtil.isNotEmpty(Collections.singletonList(user.getJobs()))) {
            List<Job> jobList = new ArrayList<>(user.getJobs());
            userDto.setJobName(jobList.get(0).getName());
        }
        if (user.getDept() != null) {
            userDto.setDeptId(user.getDept().getId());
        }
        if (user.getSuperiorId() != null) {
            User sup = userRepository.findById(user.getSuperiorId()).orElseGet(User::new);
            ValidationUtil.isNull(sup.getId(), "User", "id", user.getSuperiorId());
            userDto.setSuperiorName(sup.getUsername());
        } else if (user.getDept() != null) {
            if (user.getDept().getPid() != null) {
                List<User> sups = userRepository.findByDeptIdAndIsMaster(user.getDept().getPid(), true);
                if (ValidationUtil.isNotEmpty(sups)) {
                    userDto.setSuperiorName(sups.get(0).getUsername());
                }
            } else {
                userDto.setSuperiorName(userDto.getUsername());
            }
        }
        return userDto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCenter(User resources) {
        User user = userRepository.findById(resources.getId()).orElseGet(User::new);
        User user1 = userRepository.findByPhone(resources.getPhone());
        if (user1 != null && !user.getId().equals(user1.getId())) {
            throw new EntityExistException(User.class, "phone", resources.getPhone());
        }
        user.setNickName(resources.getNickName());
        user.setPhone(resources.getPhone());
        user.setGender(resources.getGender());
        userRepository.save(user);
        // 清理缓存
        delCaches(user.getId(), user.getUsername());
    }

    @Override
    public List<UserDto> getApprovers(Long deptId) {
        List<UserDto> dtoList = new ArrayList<>();
        // 获取部门信息
        Dept dept = deptRepository.findById(deptId).orElseGet(Dept::new);
        //获取上级部门
        if (dept.getPid() != null) {
            //非顶级部门
            // 上级部门
            Dept pDept = deptRepository.findById(dept.getPid()).orElseGet(Dept::new);

            if (pDept.getPid() != null) {
                // 非一级部门
                List<User> fyjList = userRepository.findByDeptIdAndIsMaster(pDept.getId(), true);
                if (ValidationUtil.isNotEmpty(fyjList)) {
                    dtoList.addAll(userMapper.toDto(fyjList));
                } else {
                    throw new BadRequestException("上级部门[" + pDept.getName() + "]未设置部门管理者，请联系管理员！");
                }
            } else {
                // 一级部门则直接提交质量部master审批
                List<User> yjList = userRepository.findByDeptIdAndIsMaster(CommonConstants.ZL_DEPART, true);
                if (ValidationUtil.isNotEmpty(yjList)) {
                    dtoList.addAll(userMapper.toDto(yjList));
                } else {
                    throw new BadRequestException("质量部门未设置部门管理者，请联系管理员！");
                }
            }
        } else {
            // 当前创建者所在部门是顶级部门，向本部门master发起任务审批（允许自批），向质量部门master发起审批任务请求
            //无上级，则自批
            User user = userRepository.findById(SecurityUtils.getCurrentUserId()).orElseGet(User::new);
            dtoList.add(userMapper.toDto(user));
        }
        return dtoList;
    }

    @Override
    public List<User> getByDeptId(Long deptId) {
        List<User> userList = new ArrayList<>();
        userList = userRepository.findByDeptId(deptId, true);
        return userList;
    }

    @Override
    public List<UserDto> removeSelfAndChildren(Long editId, List<UserDto> list) {
        list.removeIf(dto -> dto.getId().equals(editId));
        List<User> children = userRepository.findBySuperiorId(editId);
        if (ValidationUtil.isNotEmpty(children)) {
            Set<Long> rList = new HashSet<>(getChildren(children));
            rList.forEach(id -> {
                list.removeIf(dto -> dto.getId().equals(id));
            });
        }
        return list;
    }


    private List<Long> getChildren(List<User> children) {
        List<Long> list = new ArrayList<>();
        children.forEach(user -> {
                    if (user != null) {
                        List<User> users = userRepository.findBySuperiorId(user.getId());
                        if (users.size() != 0) {
                            list.addAll(getChildren(users));
                        }
                        list.add(user.getId());
                    }
                }
        );
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        for (Long id : ids) {
            // 清理缓存
            UserDto user = findById(id);
            // todo 查询是否存在关联的任务

            delCaches(user.getId(), user.getUsername());
        }
        userRepository.deleteAllByIdIn(ids);
    }

    @Override
    public UserDto findByName(String userName) {
        User user = userRepository.findByUsername(userName);
        if (user == null) {
            throw new EntityNotFoundException(User.class, "name", userName);
        } else {
            return userMapper.toDto(user);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePass(String username, String pass) {
        userRepository.updatePass(username, pass, new Date());
        flushCache(username);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initPass(Long id, String username, String pass) {
        userRepository.updatePass(username, pass, new Date());
        delCaches(id, username);
    }

    @Override
    public List<User> getByDeptIds(Set<Long> departIds) {
        return userRepository.findByDeptIdInAndEnabled(departIds, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> updateAvatar(MultipartFile multipartFile) {
        User user = userRepository.findByUsername(SecurityUtils.getCurrentUsername());
        String oldPath = user.getAvatarPath();
        File file = FileUtil.upload(multipartFile, properties.getPath().getAvatar());
        user.setAvatarPath(Objects.requireNonNull(file).getPath());
        user.setAvatarName(file.getName());
        userRepository.save(user);
        if (StringUtils.isNotBlank(oldPath)) {
            FileUtil.del(oldPath);
        }
        @NotBlank String username = user.getUsername();
        flushCache(username);
        return new HashMap<String, String>(1) {{
            put("avatar", file.getName());
        }};
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEmail(String username, String email) {
        userRepository.updateEmail(username, email);
        flushCache(username);
    }

    @Override
    public void download(List<UserDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (UserDto userDTO : queryAll) {
            List<String> roles = userDTO.getRoles().stream().map(RoleSmallDto::getName).collect(Collectors.toList());
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("用户名", userDTO.getUsername());
            map.put("工号", userDTO.getJobNum());
            map.put("员工类型", userDTO.getStaffType());
            map.put("角色", roles);
            map.put("部门", userDTO.getDept().getName());
            map.put("岗位", userDTO.getJobs().stream().map(JobSmallDto::getName).collect(Collectors.toList()));
            map.put("邮箱", userDTO.getEmail());
            map.put("状态", userDTO.getEnabled() ? "启用" : "禁用");
            map.put("手机号码", userDTO.getPhone());
            map.put("修改密码的时间", userDTO.getPwdResetTime());
            map.put("创建日期", userDTO.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    /**
     * 清理缓存
     *
     * @param id /
     */
    public void delCaches(Long id, String username) {
        redisUtils.del(CacheKey.USER_ID + id);
        flushCache(username);
    }

    /**
     * 清理 登陆时 用户缓存信息
     *
     * @param username /
     */
    private void flushCache(String username) {
        userCacheClean.cleanUserCache(username);
    }
}
