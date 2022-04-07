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
import me.zhengjie.service.GridFileService;
import me.zhengjie.service.dto.InstruGridFileQueryCriteria;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * @author TongMin Jie
 * @date 2022-03-14
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：仪器台账")
@RequestMapping("/api/gridFile")
public class GridController {

    private final GridFileService gridFileService;

    @PostMapping
    @ApiOperation("上传仪器台账")
    @PreAuthorize("@el.check('calibration:edit')")
    public ResponseEntity<Object> uploadGridFile(@RequestParam("fileType") String fileType,@RequestParam("file") MultipartFile file){
        gridFileService.uploadGridFile(fileType,file);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation("查询仪器台账")
    @PostMapping(value = "/getByExample")
    @PreAuthorize("@el.check('calibration:list')")
    public ResponseEntity<Object> getGridFile(@RequestBody InstruGridFileQueryCriteria criteria) {
        return new ResponseEntity<>(gridFileService.queryByExample(criteria),HttpStatus.OK);
    }

    @Log("删除仪器台账")
    @ApiOperation("删除仪器台账")
    @DeleteMapping
    @PreAuthorize("@el.check('calibration:edit')")
    public ResponseEntity<Object> deleteGridFiles(@RequestBody Set<Long> ids) {
        gridFileService.deleteGridFiles(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}