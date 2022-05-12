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
import me.zhengjie.domain.TrainNewStaff;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.FileDeptService;
import me.zhengjie.service.TrainNewStaffService;
import me.zhengjie.service.dto.TrainNewStaffDto;
import me.zhengjie.service.dto.TrainNewStaffQueryCriteria;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author TongMin Jie
 * @date 2022-05-06
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：培训-考试员工信息")
@RequestMapping("/api/train/examStaff")
public class TrainExamStaffController {

    private final FileDeptService fileDeptService;
    private final TrainNewStaffService staffService;
    private static final String ENTITY_NAME = "TrainExamStaff";

    @ApiOperation("导出新员工培训数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('train:list')")
    public void download(HttpServletResponse response, TrainNewStaffQueryCriteria criteria) throws IOException {
        //部门查询
        initDepartChildren(criteria);
        staffService.download(staffService.queryAll(criteria), response);
    }

    @ApiOperation("查询新员工培训信息")
    @GetMapping
    @PreAuthorize("@el.check('train:list')")
    public ResponseEntity<Object> query(TrainNewStaffQueryCriteria criteria, Pageable pageable) {
        //部门查询
        initDepartChildren(criteria);
        return new ResponseEntity<>(staffService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    @ApiOperation("查询单个新员工培训信息")
    @GetMapping(value = "/byId")
    @PreAuthorize("@el.check('train:list')")
    public ResponseEntity<Object> getById(@RequestParam("id") Long id) {
        return new ResponseEntity<>(staffService.findById(id), HttpStatus.OK);
    }

    @Log("新增新员工培训信息")
    @ApiOperation("新增新员工培训信息")
    @PostMapping
    @PreAuthorize("@el.check('newStaff:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody TrainNewStaffDto resource) {
        if (resource.getId() != null) {
            throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
        }
        staffService.create(resource);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改新员工培训信息")
    @ApiOperation("修改新员工培训信息")
    @PutMapping
    @PreAuthorize("@el.check('newStaff:edit')")
    public ResponseEntity<Object> update(@Validated(TrainNewStaff.Update.class) @RequestBody TrainNewStaff resource) {
        staffService.update(resource);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除新员工培训信息")
    @ApiOperation("删除新员工培训信息")
    @DeleteMapping
    @PreAuthorize("@el.check('newStaff:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        // 验证是否被审核计划关联
        // caliOrgService.verification(ids);
        staffService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private void initDepartChildren(TrainNewStaffQueryCriteria criteria) {
        if (!ObjectUtils.isEmpty(criteria.getDepartId())) {
            criteria.getDepartIds().add(criteria.getDepartId());
            // 先查找是否存在子节点
            List<FileDept> data = fileDeptService.findByPid(criteria.getDepartId());
            // 然后把子节点的ID都加入到集合中
            criteria.getDepartIds().addAll(fileDeptService.getDeptChildren(data));
        }
    }
}