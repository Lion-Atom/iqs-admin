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
import me.zhengjie.service.SupplierFileService;
import me.zhengjie.service.TempCerFileService;
import me.zhengjie.service.dto.SupplierFileQueryDto;
import me.zhengjie.service.dto.SupplierFileReplaceDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2021-07-26
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：供应商附件管理")
@RequestMapping("/api/supplierFile")
public class SupplierFileController {

    private final SupplierFileService supplierFileService;

    @ApiOperation("查询供应商下相关附件")
    @PostMapping(value = "/byCond")
    @PreAuthorize("@el.check('supplier:list')")
    public ResponseEntity<Object> getByExample(@RequestBody @Validated SupplierFileQueryDto dto) {
        return new ResponseEntity<>(supplierFileService.findByCond(dto), HttpStatus.OK);
    }

    @Log("上传供应商相关附件")
    @ApiOperation("上传供应商相关附件")
    @PostMapping
    @PreAuthorize("@el.check('supplier:edit')")
    public ResponseEntity<Object> uploadFile(@RequestParam("supplierId") Long supplierId, @RequestParam("contactId") Long contactId,
                                             @RequestParam("fileType") String fileType, @RequestParam("file") MultipartFile file) {
        supplierFileService.uploadFile(supplierId,contactId,fileType, file);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("更新绑定供应商附件")
    @ApiOperation("更新绑定供应商附件")
    @PostMapping("/replace")
    @PreAuthorize("@el.check('supplier:edit')")
    public ResponseEntity<Object> updateFile(@RequestBody @Validated SupplierFileReplaceDto dto) {
        supplierFileService.updateFile(dto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }


    @Log("删除供应商相关附件")
    @ApiOperation("删除供应商相关附件")
    @DeleteMapping
    @PreAuthorize("@el.check('supplier:edit')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        supplierFileService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}