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
import me.zhengjie.service.InstruCaliFileService;
import me.zhengjie.service.dto.InstruCaliFileQueryCriteria;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2022-03-14
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：仪器校准报告管理")
@RequestMapping("/api/instruCaliFile")
public class InstruCaliFileController {

    private final InstruCaliFileService fileService;

    @ApiOperation("查询仪器校准报告")
    @PostMapping(value = "/getByExample")
    @PreAuthorize("@el.check('calibration:list')")
    public ResponseEntity<Object> getByExample(@RequestBody InstruCaliFileQueryCriteria criteria) {
        return new ResponseEntity<>(fileService.queryAll(criteria), HttpStatus.OK);
    }

    @Log("上传仪校机构相关附件")
    @ApiOperation("上传仪校机构相关附件")
    @PostMapping
    @PreAuthorize("@el.check('calibration:edit')")
    public ResponseEntity<Object> uploadFile(@RequestParam("caliId") Long caliId, @RequestParam("isLatest") Boolean isLatest,
                                             @RequestParam("caliResult") String caliResult, @RequestParam("failDesc") String failDesc, @RequestParam("file") MultipartFile file) {
        fileService.uploadFile(caliId, isLatest, caliResult, failDesc, file);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("删除仪校机构相关附件")
    @ApiOperation("删除仪校机构相关附件")
    @DeleteMapping
    @PreAuthorize("@el.check('calibration:edit')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        fileService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}