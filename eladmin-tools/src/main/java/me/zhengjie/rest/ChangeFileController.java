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
 * @date 2021-07-26
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：变更附件管理")
@RequestMapping("/api/changeFile")
public class ChangeFileController {

    private final ChangeFileService fileService;

    @ApiOperation("查询变更下相关附件")
    @PostMapping(value = "/byCond")
    @PreAuthorize("@el.check('change:list')")
    public ResponseEntity<Object> getByExample(@RequestBody @Validated ChangeFileQueryDto dto) {
        return new ResponseEntity<>(fileService.findByCond(dto), HttpStatus.OK);
    }

    @Log("上传变更相关附件")
    @ApiOperation("上传变更相关附件")
    @PostMapping
    @PreAuthorize("@el.check('change:edit')")
    public ResponseEntity<Object> uploadFile(@RequestParam("changeId") Long changeId, @RequestParam("factorId") Long factorId,
                                             @RequestParam("fileType") String fileType, @RequestParam("file") MultipartFile file) {
        fileService.uploadFile(changeId,factorId,fileType, file);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }


    @Log("删除变更相关附件")
    @ApiOperation("删除变更相关附件")
    @DeleteMapping
    @PreAuthorize("@el.check('change:edit')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        fileService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}