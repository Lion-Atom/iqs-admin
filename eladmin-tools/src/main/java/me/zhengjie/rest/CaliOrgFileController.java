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
import me.zhengjie.service.CaliOrgFileService;
import me.zhengjie.service.ChangeFileService;
import me.zhengjie.service.dto.ChangeFileQueryDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2022-03-11
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：仪校机构附件管理")
@RequestMapping("/api/caliOrgFile")
public class CaliOrgFileController {

    private final CaliOrgFileService fileService;

    @ApiOperation("查询仪校机构下相关附件")
    @GetMapping(value = "/byCaliOrgId")
    @PreAuthorize("@el.check('caliorg:list')")
    public ResponseEntity<Object> getByCaliOrgId(@RequestParam("caliOrgId") Long caliOrgId) {
        return new ResponseEntity<>(fileService.findByCaliOrgId(caliOrgId), HttpStatus.OK);
    }

    @ApiOperation("查询仪校机构下相关附件")
    @GetMapping(value = "/delByCaliOrgIdAndName")
    @PreAuthorize("@el.check('caliorg:list')")
    public ResponseEntity<Object> delByCaliOrgIdAndName(@RequestParam("caliOrgId") Long caliOrgId,@RequestParam("realName") String realName) {
        fileService.delByCaliOrgIdAndName(caliOrgId,realName);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Log("上传仪校机构相关附件")
    @ApiOperation("上传仪校机构相关附件")
    @PostMapping
    @PreAuthorize("@el.check('caliorg:edit')")
    public ResponseEntity<Object> uploadFile(@RequestParam("caliOrgId") Long caliOrgId,  @RequestParam("file") MultipartFile file) {
        fileService.uploadFile(caliOrgId, file);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }


    @Log("删除仪校机构相关附件")
    @ApiOperation("删除仪校机构相关附件")
    @DeleteMapping
    @PreAuthorize("@el.check('caliorg:edit')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        fileService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}