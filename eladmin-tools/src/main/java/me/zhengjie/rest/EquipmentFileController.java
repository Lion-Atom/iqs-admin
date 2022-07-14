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
import me.zhengjie.service.EquipmentFileService;
import me.zhengjie.service.dto.EquipFileQueryByExample;
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
@Api(tags = "工具：设备相关附件管理")
@RequestMapping("/api/equipFile")
public class EquipmentFileController {

    private final EquipmentFileService fileService;

    @ApiOperation("查询设备相关附件")
    @PostMapping(value = "/byExample")
    @PreAuthorize("@el.check('equip:list')")
    public ResponseEntity<Object> queryByExample(@RequestBody EquipFileQueryByExample queryByExample) {
        return new ResponseEntity<>(fileService.queryByExample(queryByExample), HttpStatus.OK);
    }

    @Log("上传设备相关附件")
    @ApiOperation("上传设备相关附件")
    @PostMapping
    @PreAuthorize("@el.check('equip:edit')")
    public ResponseEntity<Object> uploadFile(@RequestParam("equipId") Long equipId, @RequestParam("fileType") String fileType, @RequestParam("file") MultipartFile file) {
        fileService.uploadFile(equipId, fileType, file);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("删除设备相关附件")
    @ApiOperation("删除设备相关附件")
    @DeleteMapping
    @PreAuthorize("@el.check('equip:edit')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        fileService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}