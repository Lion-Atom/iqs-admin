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
import me.zhengjie.domain.SupplierQMMethod;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.SupplierQMMethodService;
import me.zhengjie.service.dto.SupplierQMMethodReplaceDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2021-11-30
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：供应商-质量管理工具")
@RequestMapping("/api/SupplierQMMethod")
public class SupplierQMMethodController {

    private final SupplierQMMethodService methodService;
    private static final String ENTITY_NAME = "SupplierQMMethod";

    @ApiOperation("查询供应商质量管理工具")
    @GetMapping(value = "/bySupplierId")
    @PreAuthorize("@el.check('supplier:list')")
    public ResponseEntity<Object> getByExample(@RequestParam("supplierId") Long supplierId) {
        return new ResponseEntity<>(methodService.findBySupplierId(supplierId), HttpStatus.OK);
    }

    @Log("新增供应商质量管理工具")
    @ApiOperation("新增供应商质量管理工具")
    @PostMapping
    @PreAuthorize("@el.check('supplier:edit')")
    public ResponseEntity<Object> create(@Validated @RequestBody SupplierQMMethod resources) {
        if (resources.getId() != null) {
            throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
        }
        return new ResponseEntity<>(methodService.create(resources), HttpStatus.CREATED);
    }

    @ApiOperation("查询供应商相关附件")
    @GetMapping(value = "/init/byUid")
    @PreAuthorize("@el.check('supplier:list')")
    public ResponseEntity<Object> initCertification(@RequestParam("uId") Long uId) {
        return new ResponseEntity<>(methodService.initMethod(uId), HttpStatus.OK);
    }

    @Log("修改供应商质量管理工具")
    @ApiOperation("修改供应商质量管理工具")
    @PutMapping
    @PreAuthorize("@el.check('supplier:edit')")
    public ResponseEntity<Object> update(@Validated(SupplierQMMethod.Update.class) @RequestBody SupplierQMMethod resources) {
        methodService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("更新绑定供应商质量管理工具")
    @ApiOperation("更新绑定供应商质量管理工具")
    @PostMapping("/replace")
    @PreAuthorize("@el.check('supplier:edit')")
    public ResponseEntity<Object> replaceMethod(@RequestBody @Validated SupplierQMMethodReplaceDto dto) {
        methodService.replaceMethod(dto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("删除供应商供应商质量管理工具")
    @ApiOperation("删除供应商供应商质量管理工具")
    @DeleteMapping
    @PreAuthorize("@el.check('supplier:edit')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        methodService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}