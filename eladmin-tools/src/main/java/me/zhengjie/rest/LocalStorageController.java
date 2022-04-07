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
package me.zhengjie.rest;

import lombok.RequiredArgsConstructor;
import me.zhengjie.annotation.Log;
import me.zhengjie.domain.*;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.IssueFileRepository;
import me.zhengjie.service.FileCategoryService;
import me.zhengjie.service.FileDeptService;
import me.zhengjie.service.LocalStorageService;
import me.zhengjie.service.dto.*;
import me.zhengjie.utils.FileUtil;
import me.zhengjie.utils.SecurityUtils;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

/**
 * @author Zheng Jie
 * @date 2019-09-05
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：本地存储管理")
@RequestMapping("/api/localStorage")
public class LocalStorageController {

    private final LocalStorageService localStorageService;

    private final FileDeptService fileDeptService;

    private final FileCategoryService fileCategoryService;

    @ApiOperation("查询文件")
    @GetMapping
    @PreAuthorize("@el.check('storage:list')")
    public ResponseEntity<Object> query(LocalStorageQueryCriteria criteria, Pageable pageable) {
        // 获取当前登陆人所在部门,若是管理员则放开限制
        // Long deptId = SecurityUtils.getCurrentDeptId();
        Boolean isAdmin = SecurityUtils.getIsAdmin();
        List<Long> scopes = SecurityUtils.getCurrentUserDataScope();
        // criteria.getId() !=null 说明是绑定文件，则不设置权限部门范围
        // criteria.getId() ==null 无关绑定，那么需要判断是否要设置部门权限
        // criteria.getAnonymousAccess() 是否设部门权限等限制---例如：绑定参考文件时候就不许限制所属部门
        if (!isAdmin && ObjectUtils.isEmpty(criteria.getDeptId()) && criteria.getId() == null && !criteria.getAnonymousAccess()) {
            // criteria.setDeptId(deptId); // 注释并更改为获取权限范围的赋值原因：个人所拥有的权限可能大于或小于子集的部门权限
            criteria.getDeptIds().addAll(scopes);
        }

        // 已作废文件不可添加为参考文献 impl中已设置拦截

        //部门查询
        if (!ObjectUtils.isEmpty(criteria.getDeptId())) {
            criteria.getDeptIds().add(criteria.getDeptId());
            // 先查找是否存在子节点
            List<FileDept> data = fileDeptService.findByPid(criteria.getDeptId());
            // 然后把子节点的ID都加入到集合中
            criteria.getDeptIds().addAll(fileDeptService.getDeptChildren(data));
        }
        //分类查询
        if (!ObjectUtils.isEmpty(criteria.getFileCategoryId())) {
            criteria.getFileCategoryIds().add(criteria.getFileCategoryId());
            // 先查找是否存在子节点
            List<FileCategory> data = fileCategoryService.findByPid(criteria.getFileCategoryId());
            // 然后把子节点的ID都加入到集合中
            criteria.getFileCategoryIds().addAll(fileCategoryService.getFileCategoryChildren(data));
        }

        return new ResponseEntity<>(localStorageService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    @ApiOperation("条件查询文件信息")
    @PostMapping(value = "/byExample")
    @PreAuthorize("@el.check('storage:list')")
    public ResponseEntity<Object> getByExample(@RequestBody FileQueryDto queryDto) {
        return new ResponseEntity<>(localStorageService.findByExample(queryDto), HttpStatus.OK);
    }

    @ApiOperation("标识集合查询文件")
    @PostMapping(value = "/byIds")
    @PreAuthorize("@el.check('storage:list')")
    public ResponseEntity<Object> queryByIds(@RequestBody Long[] ids) {
        return new ResponseEntity<>(localStorageService.queryAllByIds(ids), HttpStatus.OK);
    }

    @ApiOperation("标识集合查询文件临时信息")
    @PostMapping(value = "/tempByIds")
    @PreAuthorize("@el.check('storage:list')")
    public ResponseEntity<Object> queryByTempIds(@RequestBody Long[] ids) {
        return new ResponseEntity<>(localStorageService.queryAllByIds(ids), HttpStatus.OK);
    }

    @ApiOperation("查询单个文件")
    @GetMapping(value = "/byId")
    @PreAuthorize("@el.check('storage:list')")
    public ResponseEntity<Object> getById(@RequestParam Long fileId) {
        return new ResponseEntity<>(localStorageService.findById(fileId), HttpStatus.OK);
    }

    /*@ApiOperation("按照文件类型查询文件")
    @GetMapping
    @PreAuthorize("@el.check('storage:list')")
    public ResponseEntity<Object> queryType(LocalStorageQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(localStorageService.queryAll(criteria, pageable), HttpStatus.OK);
    }*/

    @ApiOperation("查询文件相关审批任务")
    @GetMapping(value = "/getPreTrailByFileId")
    @PreAuthorize("@el.check('storage:list')")
    // latestVersion为true只获取最新任务
    public ResponseEntity<Object> getPreTrailByFileId(@RequestParam("fileId") Long fileId, @RequestParam("latestVersion") Boolean latestVersion) {
        return new ResponseEntity<>(localStorageService.getPreTrailByFileId(fileId, latestVersion), HttpStatus.OK);
    }

    @ApiOperation("查询文件临时相关审批任务")
    @GetMapping(value = "/getPreTrailByFileTempId")
    @PreAuthorize("@el.check('storage:list')")
    // latestVersion为true只获取最新任务
    public ResponseEntity<Object> getPreTrailByFileTempId(@RequestParam("fileTempId") Long fileTempId, @RequestParam("latestVersion") Boolean latestVersion) {
        return new ResponseEntity<>(localStorageService.getPreTrailByFileTempId(fileTempId, latestVersion), HttpStatus.OK);
    }

    @ApiOperation("查询文件当前审批进度信息")
    @GetMapping(value = "/getApprovalProcessByFileId")
    @PreAuthorize("@el.check('storage:list')")
    public ResponseEntity<Object> getApprovalProcessByFileId(@RequestParam("fileId") Long fileId) {
        return new ResponseEntity<>(localStorageService.getApprovalProcessByFileId(fileId), HttpStatus.OK);
    }

    @ApiOperation("查询文件当前审批进度信息")
    @GetMapping(value = "/getApprovalProcessListByFileId")
    @PreAuthorize("@el.check('storage:list')")
    public ResponseEntity<Object> getApprovalProcessListByFileId(@RequestParam("fileId") Long fileId) {
        return new ResponseEntity<>(localStorageService.getApprovalProcessListByFileId(fileId), HttpStatus.OK);
    }

    @ApiOperation("查询文件当前临时审批进度信息")
    @GetMapping(value = "/getApprovalProcessByFileIdV2")
    @PreAuthorize("@el.check('storage:list')")
    public ResponseEntity<Object> getTempApprovalProcessByFileId(@RequestParam("fileId") Long fileId,@RequestParam("isTemp") Boolean isTemp) {
        return new ResponseEntity<>(localStorageService.getApprovalProcessByFileIdV2(fileId,isTemp), HttpStatus.OK);
    }

    @ApiOperation("导出数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('storage:list')")
    public void download(HttpServletResponse response, LocalStorageQueryCriteria criteria) throws IOException {
        //获取当前登陆人所在部门,若是管理员则放开限制
        Long deptId = SecurityUtils.getCurrentDeptId();
        Boolean isAdmin = SecurityUtils.getIsAdmin();
        // criteria.getId() !=null 说明是绑定文件，则不设置权限部门范围
        // criteria.getId() ==null 无关绑定，那么需要判断是否药设置部门权限
        // criteria.getAnonymousAccess() 是否设部门权限等限制---例如：绑定参考文件时候就不许限制所属部门
        if (!isAdmin && ObjectUtils.isEmpty(criteria.getDeptId()) && criteria.getId() == null && !criteria.getAnonymousAccess()) {
            criteria.setDeptId(deptId);
        }
        //部门查询
        if (!ObjectUtils.isEmpty(criteria.getDeptId())) {
            criteria.getDeptIds().add(criteria.getDeptId());
            // 先查找是否存在子节点
            List<FileDept> data = fileDeptService.findByPid(criteria.getDeptId());
            // 然后把子节点的ID都加入到集合中
            criteria.getDeptIds().addAll(fileDeptService.getDeptChildren(data));
        }
        localStorageService.download(localStorageService.queryAll(criteria), response);
    }

    @Log("上传文件")
    @ApiOperation("上传文件")
    @PostMapping
    @PreAuthorize("@el.check('storage:add')")
    public ResponseEntity<Object> create(@RequestParam("name") String name,@RequestParam("version") String version, @RequestParam("fileLevelId") Long fileLevelId, @RequestParam("fileCategoryId") Long fileCategoryId,
                                         @RequestParam("deptId") Long deptId, @RequestParam("fileStatus") String fileStatus, @RequestParam("fileType") String fileType, @RequestParam("securityLevel") String securityLevel,
                                         @RequestParam("expirationTime") Timestamp expirationTime, @RequestParam("fileDesc") String fileDesc, @RequestParam("file") MultipartFile file,
                                         @RequestParam("bindingFiles") List<Long> bindingFiles,@RequestParam("bindingDepts") Set<Long> bindingDepts) {
        localStorageService.create(name,version, fileLevelId, fileCategoryId, deptId, fileStatus, fileType, securityLevel, expirationTime, fileDesc, file, bindingFiles,bindingDepts, false);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation("上传待审批变更文件")
    @Log("上传待审批文件变更信息")
    @PostMapping(value = "/upload/preTrailV2")
    @PreAuthorize("@el.check('storage:add')")
    public ResponseEntity<Object> uploadPreTrail(@RequestParam("id") Long id, @RequestParam("version") String version, @RequestParam("approvalStatus") String approvalStatus, @RequestParam("file") MultipartFile file) {
        //提交审批变更文件信息
        PreTrail preTrail = localStorageService.uploadPreTrailV2(id,version, approvalStatus, file);
        return new ResponseEntity<>(preTrail, HttpStatus.ACCEPTED);
    }

    @Deprecated
    @ApiOperation("变更文件")
    @Log("变更文件版本")
    @PostMapping(value = "/cover")
    @PreAuthorize("@el.check('storage:add')")
    public ResponseEntity<Object> cover(@RequestParam Long id, @RequestParam("file") MultipartFile file) {
        // 需要提交审批
        localStorageService.cover(id, file);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @ApiOperation("撤销版本变更")
    @Log("撤销文件版本变更")
    @PostMapping(value = "/undo")
    //@PreAuthorize("@el.check('storage:add')")
    public ResponseEntity undo(@RequestBody Long id) {
        localStorageService.undo(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation("回滚覆盖版本")
    @Log("回滚覆盖版本")
    @PostMapping(value = "/rollBackCover")
    //@PreAuthorize("@el.check('storage:add')")
    public ResponseEntity<Object> rollBackCover(@RequestBody @Validated RollbackDto dto) {
        localStorageService.rollBackCover(dto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/pictures")
    @ApiOperation("上传图片")
    public ResponseEntity<Object> upload(@RequestParam MultipartFile file) {
        Long deptId = SecurityUtils.getCurrentDeptId();
        // 判断文件是否为图片
        String suffix = FileUtil.getExtensionName(file.getOriginalFilename());
        if (!FileUtil.IMAGE.equals(FileUtil.getFileType(suffix))) {
            throw new BadRequestException("只能上传图片");
        }
        LocalStorage localStorage = localStorageService.create(null,"A/0", 23L, 15L, deptId,
                "release", "others", "internal", null, null, file, null,null, true);
        return new ResponseEntity<>(localStorage, HttpStatus.OK);
    }

    @Log("修改文件")
    @ApiOperation("修改文件")
    @PutMapping
    @PreAuthorize("@el.check('storage:edit')")
    public ResponseEntity<Object> update(@Validated @RequestBody LocalStorage resources) {
        localStorageService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除文件")
    @DeleteMapping
    @ApiOperation("多选删除")
    public ResponseEntity<Object> delete(@RequestBody Long[] ids) {
        localStorageService.deleteAll(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}