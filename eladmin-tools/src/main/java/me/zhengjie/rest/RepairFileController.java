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
import me.zhengjie.service.RepairFileService;
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
@Api(tags = "工具：设备维修验确认单管理")
@RequestMapping("/api/repairFile")
public class RepairFileController {

    private final RepairFileService fileService;

    @ApiOperation("查询设备维修验确认单")
    @GetMapping(value = "/byRepairId")
    @PreAuthorize("@el.check('repair:list')")
    public ResponseEntity<Object> getByRepairId(@RequestParam("repairId") Long repairId) {
        return new ResponseEntity<>(fileService.getByRepairId(repairId), HttpStatus.OK);
    }

    @Log("上传设备维修相关附件")
    @ApiOperation("上传设备维修相关附件")
    @PostMapping
    @PreAuthorize("@el.check('repair:edit')")
    public ResponseEntity<Object> uploadFile(@RequestParam("repairId") Long repairId, @RequestParam("file") MultipartFile file) {
        fileService.uploadFile(repairId, file);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("删除设备维修相关附件")
    @ApiOperation("删除设备维修相关附件")
    @DeleteMapping
    @PreAuthorize("@el.check('repair:edit')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        fileService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}