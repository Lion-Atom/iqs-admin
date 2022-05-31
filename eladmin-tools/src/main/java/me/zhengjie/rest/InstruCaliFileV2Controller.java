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
import me.zhengjie.service.InstruCaliFileServiceV2;
import me.zhengjie.service.MaintainFileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2022-04-11
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：仪器校准报告管理")
@RequestMapping("/api/instruCaliFileV2")
public class InstruCaliFileV2Controller {

    private final InstruCaliFileServiceV2 fileService;

    @ApiOperation("查询仪器校准报告")
    @GetMapping(value = "/byCaliId")
    @PreAuthorize("@el.check('maintain:list')")
    public ResponseEntity<Object> getByCaliId(@RequestParam("caliId") Long caliId) {
        return new ResponseEntity<>(fileService.getByCaliId(caliId), HttpStatus.OK);
    }

    @Log("上传设备保养相关附件")
    @ApiOperation("上传设备保养相关附件")
    @PostMapping
    @PreAuthorize("@el.check('maintain:edit')")
    public ResponseEntity<Object> uploadFile(@RequestParam("caliId") Long caliId, @RequestParam("file") MultipartFile file) {
        fileService.uploadFile(caliId, file);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("删除设备保养相关附件")
    @ApiOperation("删除设备保养相关附件")
    @DeleteMapping
    @PreAuthorize("@el.check('maintain:edit')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        fileService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}