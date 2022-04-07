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
import me.zhengjie.domain.SupplierContact;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.SupplierContactService;
import me.zhengjie.service.dto.SupplierContactQueryCriteria;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * @author TongMin Jie
 * @date 2021-11-23
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：供应商联系人管理")
@RequestMapping("/api/supplierContact")
public class SupplierContactController {

    private final SupplierContactService contactService;
    private static final String ENTITY_NAME = "supplierContact";

    @ApiOperation("导出供应商联系人数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('supplier:list')")
    public void download(HttpServletResponse response, SupplierContactQueryCriteria criteria) throws IOException {
        contactService.download(contactService.queryAll(criteria), response);
    }

    @ApiOperation("查询供应商联系人信息")
    @GetMapping
    @PreAuthorize("@el.check('supplier:list')")
    public ResponseEntity<Object> query(SupplierContactQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(contactService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    @ApiOperation("查询供应商对应联系人信息")
    @GetMapping(value = "/bySupplierId")
    @PreAuthorize("@el.check('supplier:list')")
    public ResponseEntity<Object> getBySupplierId(@RequestParam("supplierId") Long supplierId) {
        return new ResponseEntity<>(contactService.findBySupplierId(supplierId), HttpStatus.OK);
    }

    @ApiOperation("查询单个供应商联系人信息")
    @GetMapping(value = "/byId")
    @PreAuthorize("@el.check('supplier:list')")
    public ResponseEntity<Object> getById(@RequestParam("contactId") Long contactId) {
        return new ResponseEntity<>(contactService.findById(contactId), HttpStatus.OK);
    }

    @Log("新增供应商联系人信息")
    @ApiOperation("新增供应商联系人信息")
    @PostMapping
    @PreAuthorize("@el.check('supplier:edit')")
    public ResponseEntity<Object> create(@Validated @RequestBody SupplierContact resources) {
        if (resources.getId() != null) {
            throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
        }
        return new ResponseEntity<>(contactService.create(resources), HttpStatus.CREATED);
    }

    @Log("修改供应商联系人信息")
    @ApiOperation("修改供应商联系人信息")
    @PutMapping
    @PreAuthorize("@el.check('supplier:edit')")
    public ResponseEntity<Object> update(@Validated(SupplierContact.Update.class) @RequestBody SupplierContact resources) {
        contactService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除供应商联系人信息")
    @ApiOperation("删除供应商联系人信息")
    @DeleteMapping
    @PreAuthorize("@el.check('supplier:edit')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        // 验证是否被审核计划关联
        contactService.verification(ids);
        contactService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Deprecated
    @ApiOperation("修改联系照片")
    @PostMapping(value = "/updateAvatar")
    @PreAuthorize("@el.check('supplier:edit')")
    public ResponseEntity<Object> updateAvatar(@RequestParam("contactId") Long contactId,@RequestParam MultipartFile avatar) {
        return new ResponseEntity<>(contactService.updateAvatar(contactId,avatar), HttpStatus.OK);
    }

}