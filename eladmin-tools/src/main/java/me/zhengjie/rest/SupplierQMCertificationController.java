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
import me.zhengjie.domain.SupplierQMCertification;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.SupplierQMCertificationService;
import me.zhengjie.service.dto.SupplierQMCerReplaceDto;
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
@Api(tags = "工具：供应商-质量管理认证")
@RequestMapping("/api/SupplierQMCertification")
public class SupplierQMCertificationController {

    private final SupplierQMCertificationService certificationService;
    private static final String ENTITY_NAME = "SupplierQMCertificationService";

    @ApiOperation("查询供应商质量管理认证信息")
    @GetMapping(value = "/bySupplierId")
    @PreAuthorize("@el.check('supplier:list')")
    public ResponseEntity<Object> getByExample(@RequestParam("supplierId") Long supplierId) {
        return new ResponseEntity<>(certificationService.findBySupplierId(supplierId), HttpStatus.OK);
    }

    @Log("新增质量管理认证信息")
    @ApiOperation("新增质量管理认证信息")
    @PostMapping
    @PreAuthorize("@el.check('supplier:edit')")
    public ResponseEntity<Object> create(@Validated @RequestBody SupplierQMCertification resources) {
        if (resources.getId() != null) {
            throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
        }
        return new ResponseEntity<>(certificationService.create(resources), HttpStatus.CREATED);
    }

    @ApiOperation("查询供应商相关附件")
    @GetMapping(value = "/init/byUid")
    @PreAuthorize("@el.check('supplier:list')")
    public ResponseEntity<Object> initCertification(@RequestParam("uId") Long uId) {
        return new ResponseEntity<>(certificationService.initCertification(uId), HttpStatus.OK);
    }

    @Log("修改质量管理认证信息")
    @ApiOperation("修改质量管理认证信息")
    @PutMapping
    @PreAuthorize("@el.check('supplier:edit')")
    public ResponseEntity<Object> update(@Validated(SupplierQMCertification.Update.class) @RequestBody SupplierQMCertification resources) {
        certificationService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("更新绑定质量管理认证信息")
    @ApiOperation("更新绑定质量管理认证信息")
    @PostMapping("/replace")
    @PreAuthorize("@el.check('supplier:edit')")
    public ResponseEntity<Object> replaceCer(@RequestBody @Validated SupplierQMCerReplaceDto dto) {
        certificationService.replaceCer(dto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("删除供应商质量管理认证信息")
    @ApiOperation("删除供应商质量管理认证信息")
    @DeleteMapping
    @PreAuthorize("@el.check('supplier:edit')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        certificationService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}