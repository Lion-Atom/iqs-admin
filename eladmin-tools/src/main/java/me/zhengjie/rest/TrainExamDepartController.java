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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.zhengjie.annotation.Log;
import me.zhengjie.domain.FileDept;
import me.zhengjie.domain.TrainExamDepart;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.FileDeptService;
import me.zhengjie.service.TrainExamDepartService;
import me.zhengjie.service.dto.TrainExamDepartQueryCriteria;
import me.zhengjie.utils.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * @author TongMin Jie
 * @date 2022-05-09
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：培训-考试关联部门")
@RequestMapping("/api/train/examDepart")
public class TrainExamDepartController {

    private final TrainExamDepartService examDepartService;
    private final FileDeptService fileDeptService;
    private static final String ENTITY_NAME = "TrainExamDepart";

    @ApiOperation("查询考试关联部门信息")
    @GetMapping
    @PreAuthorize("@el.check('train:list')")
    public ResponseEntity<Object> query(TrainExamDepartQueryCriteria criteria) {
        Boolean isAdmin = SecurityUtils.getIsAdmin();
        List<Long> scopes = SecurityUtils.getCurrentUserDataScope();
        if (!isAdmin && ObjectUtils.isEmpty(criteria.getDeptId())) {
            // criteria.setDeptId(deptId); // 注释并更改为获取权限范围的赋值原因：个人所拥有的权限可能大于或小于子集的部门权限
            criteria.getDepartIds().addAll(scopes);
        }
        //部门查询
        if (!ObjectUtils.isEmpty(criteria.getDeptId())) {
            criteria.getDepartIds().add(criteria.getDeptId());
            // 先查找是否存在子节点
            List<FileDept> data = fileDeptService.findByPid(criteria.getDeptId());
            // 然后把子节点的ID都加入到集合中
            criteria.getDepartIds().addAll(fileDeptService.getDeptChildren(data));
        }
        return new ResponseEntity<>(examDepartService.queryAll(criteria), HttpStatus.OK);
    }

    /*@ApiOperation("查询单个考试关联部门信息")
    @GetMapping(value = "/byId")
    @PreAuthorize("@el.check('train:list')")
    public ResponseEntity<Object> getById(@RequestParam("id") Long id) {
        return new ResponseEntity<>(examDepartService.findById(id), HttpStatus.OK);
    }*/

    @Log("新增考试关联部门信息")
    @ApiOperation("新增考试关联部门信息")
    @PostMapping
    @PreAuthorize("@el.check('exam:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody TrainExamDepart resource) {
        if (resource.getId() != null) {
            throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
        }
        examDepartService.create(resource);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改考试关联部门信息")
    @ApiOperation("修改考试关联部门信息")
    @PutMapping
    @PreAuthorize("@el.check('exam:edit')")
    public ResponseEntity<Object> update(@Validated(TrainExamDepart.Update.class) @RequestBody TrainExamDepart resource) {
        examDepartService.update(resource);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除考试关联部门信息")
    @ApiOperation("删除考试关联部门信息")
    @DeleteMapping
    @PreAuthorize("@el.check('exam:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        // caliOrgService.verification(ids);
        examDepartService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}