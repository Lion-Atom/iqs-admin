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
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.ChangeFactorService;
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
@Api(tags = "工具：变更管理-变更因素信息")
@RequestMapping("/api/changeFactor")
public class ChangeFactorController {

    private final ChangeFactorService factorService;
    private static final String ENTITY_NAME = "changeFactor";

    @ApiOperation("查询相关记录")
    @GetMapping(value = "/byChangeId")
    @PreAuthorize("@el.check('change:list')")
    public ResponseEntity<Object> getById(@RequestParam("changeId") Long changeId) {
        return new ResponseEntity<>(factorService.findByChangeId(changeId), HttpStatus.OK);
    }


    @Log("新增变更影响因素")
    @ApiOperation("新增变更影响因素")
    @PostMapping
    @PreAuthorize("@el.check('change:edit')")
    public ResponseEntity<Object> create(@Validated @RequestBody ChangeFactor resources) {
        if (resources.getId() != null) {
            throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
        }
        return new ResponseEntity<>(factorService.create(resources), HttpStatus.CREATED);
    }

    @Log("修改变更影响因素")
    @ApiOperation("修改变更影响因素")
    @PutMapping
    @PreAuthorize("@el.check('change:edit')")
    public ResponseEntity<Object> update(@Validated(ChangeFactor.Update.class) @RequestBody ChangeFactor resources) {
        factorService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @Log("删除变更影响因素")
    @ApiOperation("删除变更影响因素")
    @DeleteMapping
    @PreAuthorize("@el.check('change:edit')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        // 验证是否被关联
        factorService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}