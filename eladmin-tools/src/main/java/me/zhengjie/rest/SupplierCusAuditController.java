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
import me.zhengjie.domain.SupplierCusAudit;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.SupplierCusAuditService;
import me.zhengjie.service.dto.SupplierCusAuditReplaceDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
* @author Tong Minjie
* @date 2021-07-26
*/
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：供应商-客户审核")
@RequestMapping("/api/supplierCusAudit")
public class SupplierCusAuditController {

    private final SupplierCusAuditService supplierCusAuditService;
    private static final String ENTITY_NAME = "supplierCusAudit";

    @ApiOperation("查询供应商客户审核信息")
    @GetMapping(value = "/bySupplierId")
    @PreAuthorize("@el.check('supplier:list')")
    public ResponseEntity<Object> getById(@RequestParam("supplierId") Long supplierId) {
        return new ResponseEntity<>(supplierCusAuditService.findBySupplierId(supplierId), HttpStatus.OK);
    }

    @Log("新增供应商客户审核信息")
    @ApiOperation("新增供应商客户审核信息")
    @PostMapping
    @PreAuthorize("@el.check('supplier:edit')")
    public ResponseEntity create(@Validated @RequestBody SupplierCusAudit resources){
        if (resources.getId() != null) {
            throw new BadRequestException("A new "+ ENTITY_NAME +" cannot already have an ID");
        }
        return new ResponseEntity<>(supplierCusAuditService.create(resources),HttpStatus.CREATED);
    }

    @Log("修改供应商客户审核信息")
    @ApiOperation("修改供应商客户审核信息")
    @PutMapping
    @PreAuthorize("@el.check('supplier:edit')")
    public ResponseEntity<Object> update(@Validated(SupplierCusAudit.Update.class) @RequestBody SupplierCusAudit resources){
        supplierCusAuditService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("更新绑定供应商客户审核")
    @ApiOperation("更新绑定供应商客户审核")
    @PostMapping("/replace")
    @PreAuthorize("@el.check('supplier:edit')")
    public ResponseEntity<Object> replaceBindSupplierId(@RequestBody @Validated SupplierCusAuditReplaceDto dto) {
        supplierCusAuditService.replaceBindSupplierId(dto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("删除供应商客户审核信息")
    @ApiOperation("删除供应商客户审核信息")
    @DeleteMapping
    @PreAuthorize("@el.check('supplier:edit')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids){
        supplierCusAuditService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}