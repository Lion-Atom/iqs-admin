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
import me.zhengjie.domain.TrainMaterialFile;
import me.zhengjie.service.TrExamDepartFileService;
import me.zhengjie.service.TrainMaterialFileService;
import me.zhengjie.service.dto.TrainMaterialFileQueryCriteria;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2022-04-11
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：培训材料信息")
@RequestMapping("/api/trainMaterialFile")
public class TrainMaterialFileController {

    private final TrainMaterialFileService fileService;

    @ApiOperation("导出培训材料信息")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('train:list')")
    public void download(HttpServletResponse response, TrainMaterialFileQueryCriteria criteria) throws IOException {
        fileService.download(fileService.queryAll(criteria), response);
    }

    @ApiOperation("查询培训材料信息")
    @GetMapping
    @PreAuthorize("@el.check('train:list')")
    public ResponseEntity<Object> query(TrainMaterialFileQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(fileService.query(criteria,pageable), HttpStatus.OK);
    }

    @Log("上传培训材料信息")
    @ApiOperation("上传培训材料信息")
    @PostMapping
    @PreAuthorize("@el.check('material:edit')")
    public ResponseEntity<Object> uploadFile(@RequestParam("name") String name,@RequestParam("departId") Long departId,@RequestParam("author") String author,
                                             @RequestParam("version") String version,@RequestParam("isInternal") Boolean isInternal,
                                             @RequestParam("toolType") String toolType,@RequestParam("fileDesc") String fileDesc,@RequestParam("enabled") Boolean enabled,@RequestParam("file") MultipartFile file) {
        fileService.uploadFile(name,departId,author,version,isInternal,toolType,fileDesc,enabled, file);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("更新培训材料内容")
    @ApiOperation("更新培训材料内容")
    @PostMapping("/updateFile")
    @PreAuthorize("@el.check('material:edit')")
    public ResponseEntity<Object> updateFile(@RequestParam("id") Long id,@RequestParam("name") String name,@RequestParam("departId") Long departId,@RequestParam("author") String author,
                                             @RequestParam("version") String version,@RequestParam("isInternal") Boolean isInternal,
                                             @RequestParam("toolType") String toolType,@RequestParam("fileDesc") String fileDesc,@RequestParam("enabled") Boolean enabled,@RequestParam("file") MultipartFile file) {
        fileService.updateFile(id,name,departId,author,version,isInternal,toolType,fileDesc,enabled, file);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改培训材料信息")
    @ApiOperation("修改培训材料信息")
    @PutMapping
    @PreAuthorize("@el.check('material:edit')")
    public ResponseEntity<Object> update(@Validated @RequestBody TrainMaterialFile resources) {
        fileService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除培训材料信息")
    @ApiOperation("删除培训材料信息")
    @DeleteMapping
    @PreAuthorize("@el.check('material:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        fileService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}