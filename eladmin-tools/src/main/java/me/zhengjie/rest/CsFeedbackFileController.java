package me.zhengjie.rest;/*
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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.zhengjie.annotation.Log;
import me.zhengjie.service.CsFeedbackFileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2022-04-11
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：客户反馈附件信息")
@RequestMapping("/api/csFeedbackFile")
public class CsFeedbackFileController {

    private final CsFeedbackFileService fileService;

    @ApiOperation("查询客户反馈附件信息")
    @GetMapping(value = "/byCsFeedbackId")
    public ResponseEntity<Object> getByCsFeedbackId(@RequestParam("csFeedbackId") Long csFeedbackId) {
        return new ResponseEntity<>(fileService.getByCsFeedbackId(csFeedbackId), HttpStatus.OK);
    }

    @Log("上传客户反馈相关附件")
    @ApiOperation("上传客户反馈相关附件")
    @PostMapping
    public ResponseEntity<Object> uploadFile(@RequestParam("csFeedbackId") Long csFeedbackId, @RequestParam("file") MultipartFile file) {
        fileService.uploadFile(csFeedbackId, file);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("删除客户反馈相关附件信息")
    @ApiOperation("删除客户反馈相关附件信息")
    @DeleteMapping
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        fileService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}