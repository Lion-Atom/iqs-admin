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
import me.zhengjie.service.ApQuestionFileService;
import me.zhengjie.service.TempCerFileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2021-07-26
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：审核计划-VDA6.3模板下认证信息对应的附件")
@RequestMapping("/api/tempCerFile")
public class TempCerFileController {

    private final TempCerFileService cerFileService;

    @ApiOperation("查询模板下认证信息对应的附件")
    @GetMapping(value = "/byCerId")
    @PreAuthorize("@el.check('plan:edit')")
    public ResponseEntity<Object> getByExample(@RequestParam("cerId") Long cerId) {
        return new ResponseEntity<>(cerFileService.findByCerId(cerId), HttpStatus.OK);
    }

    @Log("上传模板下认证信息相关附件")
    @ApiOperation("上传模板下认证信息相关附件")
    @PostMapping
    @PreAuthorize("@el.check('plan:edit')")
    public ResponseEntity<Object> uploadFile(@RequestParam("cerId") Long cerId,@RequestParam("file") MultipartFile file) {
        cerFileService.uploadFile(cerId,file);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }


    @Log("删除模板下认证信息相关附件")
    @ApiOperation("删除模板下认证信息相关附件")
    @DeleteMapping
    @PreAuthorize("@el.check('plan:edit')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids){
        cerFileService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}