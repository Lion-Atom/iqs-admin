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
import me.zhengjie.domain.IssueFile;
import me.zhengjie.service.IssueFileService;
import me.zhengjie.service.dto.IssueBindFileDto;
import me.zhengjie.service.dto.IssueBindFileQueryDto;
import me.zhengjie.service.dto.IssueFileQueryDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2021-07-26
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：8D-附件")
@RequestMapping("/api/issueFile")
public class IssueFileController {

    private final IssueFileService issueFileService;

    @ApiOperation("查询8D附件")
    @PostMapping(value = "/byExample")
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> getByExample(@RequestBody @Validated IssueBindFileQueryDto queryDto) {
        return new ResponseEntity<>(issueFileService.findByCondV2(queryDto), HttpStatus.OK);
    }

    @ApiOperation("查询8D关联附件")
    @PostMapping(value = "/bindFile")
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> getBindFilesByExample(@RequestBody @Validated IssueBindFileQueryDto queryDto) {
        return new ResponseEntity<>(issueFileService.getBindFilesByExample(queryDto), HttpStatus.OK);
    }

    @Log("上传附件")
    @ApiOperation("上传附件")
    @PostMapping
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> create(@RequestParam("issueId") Long issueId, @RequestParam("stepName") String stepName, @RequestParam("file") MultipartFile file) {
        issueFileService.create(issueId, stepName, file);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("同步临时文件")
    @ApiOperation("同步临时文件")
    @PostMapping(value = "/syncTempFile")
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> syncTempFiles(@RequestBody @Validated List<IssueBindFileDto> resources) {
        issueFileService.syncTempFiles(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("删除附件")
    @ApiOperation("删除附件")
    @DeleteMapping
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids){
        issueFileService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}