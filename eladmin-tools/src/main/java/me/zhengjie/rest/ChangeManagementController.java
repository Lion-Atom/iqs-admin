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
import me.zhengjie.domain.ChangeFactor;
import me.zhengjie.domain.ChangeManagement;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.ChangeFactorService;
import me.zhengjie.service.ChangeManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * @author Zheng Jie
 * @date 2019-03-25
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：变更管理-变更管理信息")
@RequestMapping("/api/changeManagement")
public class ChangeManagementController {

    private final ChangeManagementService managementService;
    private static final String ENTITY_NAME = "changeManagement";

    @ApiOperation("查询变更管理信息")
    @GetMapping(value = "/byChangeId")
    @PreAuthorize("@el.check('change:list')")
    public ResponseEntity<Object> getById(@RequestParam("changeId") Long changeId) {
        return new ResponseEntity<>(managementService.findByChangeId(changeId), HttpStatus.OK);
    }


    @Log("新增变更管理信息")
    @ApiOperation("新增变更管理信息")
    @PostMapping
    @PreAuthorize("@el.check('change:edit')")
    public ResponseEntity<Object> create(@Validated @RequestBody ChangeManagement resources) {
        if (resources.getId() != null) {
            throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
        }
        return new ResponseEntity<>(managementService.create(resources), HttpStatus.CREATED);
    }

    @Log("修改变更管理信息")
    @ApiOperation("修改变更管理信息")
    @PutMapping
    @PreAuthorize("@el.check('change:edit')")
    public ResponseEntity<Object> update(@Validated(ChangeManagement.Update.class) @RequestBody ChangeManagement resources) {
        return new ResponseEntity<>(managementService.update(resources),HttpStatus.ACCEPTED);
    }

    @Log("删除变更管理信息")
    @ApiOperation("删除变更管理信息")
    @DeleteMapping
    @PreAuthorize("@el.check('change:edit')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        // 验证是否被关联
        managementService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}