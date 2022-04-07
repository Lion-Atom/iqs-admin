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
import me.zhengjie.domain.SupplierAnnualAssessment;
import me.zhengjie.domain.SupplierCusAudit;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.SupplierAnuAssessService;
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
@Api(tags = "工具：供应商-年度评估")
@RequestMapping("/api/supplierAnuAssessment")
public class SupplierAnnualAssessController {

    private final SupplierAnuAssessService supplierAnuAssessService;
    private static final String ENTITY_NAME = "supplierAnnualAssessment";

    @ApiOperation("查询供应商年度评估信息")
    @GetMapping(value = "/bySupplierId")
    @PreAuthorize("@el.check('supplier:list')")
    public ResponseEntity<Object> getById(@RequestParam("supplierId") Long supplierId) {
        return new ResponseEntity<>(supplierAnuAssessService.findBySupplierId(supplierId), HttpStatus.OK);
    }

    @Log("新增供应商年度评估信息")
    @ApiOperation("新增供应商年度评估信息")
    @PostMapping
    @PreAuthorize("@el.check('supplier:edit')")
    public ResponseEntity create(@Validated @RequestBody SupplierAnnualAssessment resources){
        if (resources.getId() != null) {
            throw new BadRequestException("A new "+ ENTITY_NAME +" cannot already have an ID");
        }
        return new ResponseEntity<>(supplierAnuAssessService.create(resources),HttpStatus.CREATED);
    }

    @Log("修改供应商年度评估信息")
    @ApiOperation("修改供应商年度评估信息")
    @PutMapping
    @PreAuthorize("@el.check('supplier:edit')")
    public ResponseEntity<Object> update(@Validated(SupplierAnnualAssessment.Update.class) @RequestBody SupplierAnnualAssessment resources){
        supplierAnuAssessService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("更新绑定供应商年度评估")
    @ApiOperation("更新绑定供应商年度评估")
    @PostMapping("/replace")
    @PreAuthorize("@el.check('supplier:edit')")
    public ResponseEntity<Object> replaceBindSupplierId(@RequestBody @Validated SupplierCusAuditReplaceDto dto) {
        supplierAnuAssessService.replaceBindSupplierId(dto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("删除供应商年度评估信息")
    @ApiOperation("删除供应商年度评估信息")
    @DeleteMapping
    @PreAuthorize("@el.check('supplier:edit')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids){
        supplierAnuAssessService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}