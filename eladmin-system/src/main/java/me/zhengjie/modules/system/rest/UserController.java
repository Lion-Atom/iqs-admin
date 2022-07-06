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
package me.zhengjie.modules.system.rest;

import cn.hutool.core.collection.CollectionUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.zhengjie.annotation.Log;
import me.zhengjie.config.RsaProperties;
import me.zhengjie.domain.FileDept;
import me.zhengjie.modules.system.domain.Dept;
import me.zhengjie.modules.system.service.DataService;
import me.zhengjie.modules.system.domain.User;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.modules.system.domain.vo.UserPassVo;
import me.zhengjie.modules.system.service.DeptService;
import me.zhengjie.modules.system.service.RoleService;
import me.zhengjie.modules.system.service.dto.RoleSmallDto;
import me.zhengjie.modules.system.service.dto.UserDto;
import me.zhengjie.modules.system.service.dto.UserQueryCriteria;
import me.zhengjie.modules.system.service.VerifyService;
import me.zhengjie.utils.*;
import me.zhengjie.modules.system.service.UserService;
import me.zhengjie.utils.enums.CodeEnum;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Zheng Jie
 * @date 2018-11-23
 */
@Api(tags = "系统：用户管理")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final DataService dataService;
    private final DeptService deptService;
    private final RoleService roleService;
    private final VerifyService verificationCodeService;

    @ApiOperation("导出用户数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('user:list')")
    public void download(HttpServletResponse response, UserQueryCriteria criteria) throws IOException {
        //获取当前登陆人所在部门,若是管理员则放开限制
        Long deptId = SecurityUtils.getCurrentDeptId();
        Boolean isAdmin = SecurityUtils.getIsAdmin();
        if (!isAdmin && ObjectUtils.isEmpty(criteria.getDeptId())) {
            criteria.setDeptId(deptId);
        }
        //部门查询
        if (!ObjectUtils.isEmpty(criteria.getDeptId())) {
            criteria.getDeptIds().add(criteria.getDeptId());
            // 先查找是否存在子节点
            List<Dept> data = deptService.findByPid(criteria.getDeptId());
            // 然后把子节点的ID都加入到集合中
            criteria.getDeptIds().addAll(deptService.getDeptChildren(data));
        }
        userService.download(userService.queryAll(criteria), response);
    }

    @ApiOperation("查询用户")
    @GetMapping
    @PreAuthorize("@el.check('user:list')")
    public ResponseEntity<Object> query(UserQueryCriteria criteria, Pageable pageable) {
        if (!ObjectUtils.isEmpty(criteria.getDeptId())) {
            criteria.getDeptIds().add(criteria.getDeptId());
            // 先查找是否存在子节点
            List<Dept> data = deptService.findByPid(criteria.getDeptId());
            // 然后把子节点的ID都加入到集合中
            criteria.getDeptIds().addAll(deptService.getDeptChildren(data));
        }
        // 数据权限
        List<Long> dataScopes = dataService.getDeptIds(userService.findByName(SecurityUtils.getCurrentUsername()));
        // criteria.getDeptIds() 不为空并且数据权限不为空则取交集
        if (!CollectionUtils.isEmpty(criteria.getDeptIds()) && !CollectionUtils.isEmpty(dataScopes)) {
            // 取交集
            criteria.getDeptIds().retainAll(dataScopes);
            if (!CollectionUtil.isEmpty(criteria.getDeptIds())) {
                return new ResponseEntity<>(userService.queryAll(criteria, pageable), HttpStatus.OK);
            }
        } else {
            // 否则取并集
            criteria.getDeptIds().addAll(dataScopes);
            return new ResponseEntity<>(userService.queryAll(criteria, pageable), HttpStatus.OK);
        }
        return new ResponseEntity<>(PageUtil.toPage(null, 0), HttpStatus.OK);
    }


    @Log("新增用户")
    @ApiOperation("新增用户")
    @PostMapping
    @PreAuthorize("@el.check('user:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody User resources) {
        checkLevel(resources);
        // 默认密码 123456
        resources.setPassword(passwordEncoder.encode("123456"));
        userService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation("查询上级:根据部门ID获取上级数据")
    @GetMapping("/superior")
    @PreAuthorize("@el.check('user:list','dept:list')")
    public ResponseEntity<Object> getSuperior(@Param("deptId") Long deptId, @Param("editId") Long editId) {
        UserQueryCriteria criteria = new UserQueryCriteria();
        criteria.getDeptIds().add(deptId);
        // todo 获取上级部门标识集合,上级可能来自上级部门
        criteria.getDeptIds().addAll(deptService.getSuperiorIds(deptId));
        criteria.setEnabled(true);
        List<UserDto> list = userService.queryAll(criteria);
        if (editId != null) {
            // 去除自身
//            list.removeIf(dto -> dto.getId().equals(editId));
            // 去除下级集合
            list = userService.removeSelfAndChildren(editId, list);
        }
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @ApiOperation("根据部门ID获取部门管理者")
    @GetMapping("/havDepartMaster")
    @PreAuthorize("@el.check('user:list','dept:list')")
    public ResponseEntity<Object> havDepartMaster(@Param("deptId") Long deptId) {
        Map<String, Object> map = new HashMap<>();
        map.put("hav", false);
        UserQueryCriteria criteria = new UserQueryCriteria();
        criteria.getDeptIds().add(deptId);
        criteria.setEnabled(true);
        List<UserDto> list = userService.queryAll(criteria);
        if (ValidationUtil.isNotEmpty(list)) {
            for (UserDto user : list) {
                if (user.getEnabled() && user.getIsDepartMaster()) {
                    map.put("hav", true);
                    map.put("masterId", user.getId());
                    map.put("masterName", user.getUsername());
                    break;
                }
            }
        }
        return new ResponseEntity<>(map, HttpStatus.OK);
    }


    @ApiOperation("根据部门ID获取部门成员")
    @GetMapping("/byDeptId")
    @PreAuthorize("@el.check('user:list','dept:list')")
    public ResponseEntity<Object> byDeptId(@Param("deptId") Long deptId) {
//        List<User> users = userService.getByDeptId(deptId);
        // 先查找是否存在子节点
        List<Dept> data = deptService.findByPid(deptId);
        // 然后把子节点的ID都加入到集合中
        Set<Long> departIds = new HashSet<>();
        departIds.add(deptId);
        departIds.addAll(deptService.getDeptChildren(data));
        return new ResponseEntity<>(userService.getByDeptIds(departIds), HttpStatus.OK);
    }

    @ApiOperation("根据部门ID获取部门成员")
    @PostMapping("/byDeptIds")
    @PreAuthorize("@el.check('user:list','dept:list')")
    public ResponseEntity<Object> byDeptId(@RequestBody List<Long> deptIds) {
//        List<User> users = userService.getByDeptId(deptId);
        // 先查找是否存在子节点
        Set<Long> departIds = new HashSet<>();
        if(ValidationUtil.isNotEmpty(deptIds)) {
            deptIds.forEach(deptId->{
                List<Dept> data = deptService.findByPid(deptId);
                // 然后把子节点的ID都加入到集合中
                departIds.add(deptId);
                departIds.addAll(deptService.getDeptChildren(data));
            });
        }
        return new ResponseEntity<>(userService.getByDeptIds(departIds), HttpStatus.OK);
    }

    @ApiOperation("查询可选审批人")
    @GetMapping("/approvers")
    @PreAuthorize("@el.check('user:list','dept:list')")
    public ResponseEntity<Object> getApprovers(@Param("createBy") String createBy) {
        List<UserDto> list = new ArrayList<>();
        UserDto user = userService.findByName(createBy);
        UserQueryCriteria criteria = new UserQueryCriteria();
        if (user.getDept() != null) {
            if (!user.getIsDepartMaster() && user.getSuperiorId() != null) {
                //非部门管理员同时存在上级
                criteria.getDeptIds().add(user.getDept().getId());
                // 获取上级部门标识集合,上级可能来自上级部门
                criteria.getDeptIds().addAll(deptService.getSuperiorIds(user.getDept().getId()));
                list = userService.queryAll(criteria);
                // 去除自身
                if (user.getId() != null) {
                    list.removeIf(dto -> dto.getId().equals(user.getId()));
                }
            } else {
                // 部门管理员或者未设置上级 -- 向上查找？暂设置找质量部Master:一般只存在admin这个账号不设置上级
                // todo 部门管理员则应当查找上级部门master/质量部master
                list.addAll(userService.getApprovers(user.getDept().getId()));
            }
        }
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @Log("修改用户")
    @ApiOperation("修改用户")
    @PutMapping
    @PreAuthorize("@el.check('user:edit')")
    public ResponseEntity<Object> update(@Validated(User.Update.class) @RequestBody User resources) throws Exception {
        checkLevel(resources);
        // todo 如果修改这是部门管理者：如状态修改等、需要关联其它部门成员的上级
        userService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("修改用户：个人中心")
    @ApiOperation("修改用户：个人中心")
    @PutMapping(value = "/center")
    public ResponseEntity<Object> center(@Validated(User.Update.class) @RequestBody User resources) {
        if (!resources.getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BadRequestException("不能修改他人资料");
        }
        userService.updateCenter(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("重置用户密码")
    @ApiOperation("重置用户密码")
    @PutMapping(value = "/initPass")
    public ResponseEntity<Object> initPass(@Validated(User.Update.class) @RequestBody User resources) throws Exception {
        // 默认密码 123456
//        resources.setPassword(passwordEncoder.encode("123456"));
        userService.initPass(resources.getId(), resources.getUsername(), passwordEncoder.encode("123456"));
        return new ResponseEntity<>(resources.getUsername() + "密码已重置为：123456", HttpStatus.ACCEPTED);
    }

    @Log("删除用户")
    @ApiOperation("删除用户")
    @DeleteMapping
    @PreAuthorize("@el.check('user:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        for (Long id : ids) {
            Integer currentLevel = Collections.min(roleService.findByUsersId(SecurityUtils.getCurrentUserId()).stream().map(RoleSmallDto::getLevel).collect(Collectors.toList()));
            Integer optLevel = Collections.min(roleService.findByUsersId(id).stream().map(RoleSmallDto::getLevel).collect(Collectors.toList()));
            if (currentLevel > optLevel) {
                throw new BadRequestException("角色权限不足，不能删除：" + userService.findById(id).getUsername());
            }
        }
        userService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation("修改密码")
    @PostMapping(value = "/updatePass")
    public ResponseEntity<Object> updatePass(@RequestBody UserPassVo passVo) throws Exception {
        String oldPass = RsaUtils.decryptByPrivateKey(RsaProperties.privateKey, passVo.getOldPass());
        String newPass = RsaUtils.decryptByPrivateKey(RsaProperties.privateKey, passVo.getNewPass());
        UserDto user = userService.findByName(SecurityUtils.getCurrentUsername());
        if (!passwordEncoder.matches(oldPass, user.getPassword())) {
            throw new BadRequestException("修改失败，旧密码错误");
        }
        if (passwordEncoder.matches(newPass, user.getPassword())) {
            throw new BadRequestException("新密码不能与旧密码相同");
        }
        userService.updatePass(user.getUsername(), passwordEncoder.encode(newPass));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation("修改头像")
    @PostMapping(value = "/updateAvatar")
    public ResponseEntity<Object> updateAvatar(@RequestParam MultipartFile avatar) {
        return new ResponseEntity<>(userService.updateAvatar(avatar), HttpStatus.OK);
    }

    @Log("修改邮箱")
    @ApiOperation("修改邮箱")
    @PostMapping(value = "/updateEmail/{code}")
    public ResponseEntity<Object> updateEmail(@PathVariable String code, @RequestBody User user) throws Exception {
        String password = RsaUtils.decryptByPrivateKey(RsaProperties.privateKey, user.getPassword());
        UserDto userDto = userService.findByName(SecurityUtils.getCurrentUsername());
        if (!passwordEncoder.matches(password, userDto.getPassword())) {
            throw new BadRequestException("密码错误");
        }
        verificationCodeService.validated(CodeEnum.EMAIL_RESET_EMAIL_CODE.getKey() + user.getEmail(), code);
        userService.updateEmail(userDto.getUsername(), user.getEmail());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 如果当前用户的角色级别低于创建用户的角色级别，则抛出权限不足的错误
     *
     * @param resources /
     */
    private void checkLevel(User resources) {
        Integer currentLevel = Collections.min(roleService.findByUsersId(SecurityUtils.getCurrentUserId()).stream().map(RoleSmallDto::getLevel).collect(Collectors.toList()));
        Integer optLevel = roleService.findByRoles(resources.getRoles());
        if (currentLevel > optLevel) {
            throw new BadRequestException("角色权限不足");
        }
    }
}
