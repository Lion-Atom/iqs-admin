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
import me.zhengjie.domain.Supplier;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.SupplierService;
import me.zhengjie.service.dto.SupplierQueryCriteria;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * @author TongMin Jie
 * @date 2021-11-22
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：供应商管理")
@RequestMapping("/api/supplier")
public class SupplierController {

    private final SupplierService supplierService;
    private static final String ENTITY_NAME = "supplier";

    @ApiOperation("导出供应商数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('supplier:list')")
    public void download(HttpServletResponse response, SupplierQueryCriteria criteria) throws IOException {
        supplierService.download(supplierService.queryAll(criteria), response);
    }

    @ApiOperation("查询供应商信息")
    @GetMapping
    @PreAuthorize("@el.check('supplier:list')")
    public ResponseEntity<Object> query(SupplierQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(supplierService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    @ApiOperation("获取随机ID")
    @GetMapping(value = "/getUid")
    @PreAuthorize("@el.check('supplier:list')")
    public ResponseEntity<Object> getUid() {
        return new ResponseEntity<>(ValidationUtil.initGuid(), HttpStatus.OK);
    }

    @ApiOperation("查询单个供应商信息")
    @GetMapping(value = "/byId")
    @PreAuthorize("@el.check('supplier:list')")
    public ResponseEntity<Object> getById(@RequestParam("supplierId") Long supplierId) {
        return new ResponseEntity<>(supplierService.findById(supplierId), HttpStatus.OK);
    }

    @Log("新增供应商信息")
    @ApiOperation("新增供应商信息")
    @PostMapping
    @PreAuthorize("@el.check('supplier:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody Supplier resources) {
        if (resources.getId() != null) {
            throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
        }
        return new ResponseEntity<>(supplierService.create(resources), HttpStatus.CREATED);
    }

    @Log("修改供应商信息")
    @ApiOperation("修改供应商信息")
    @PutMapping
    @PreAuthorize("@el.check('supplier:edit')")
    public ResponseEntity<Object> update(@Validated(Supplier.Update.class) @RequestBody Supplier resources) {
        supplierService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除供应商信息")
    @ApiOperation("删除供应商信息")
    @DeleteMapping
    @PreAuthorize("@el.check('supplier:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        // 验证是否被审核计划关联
        supplierService.verification(ids);
        supplierService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}