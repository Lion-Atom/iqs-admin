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
import me.zhengjie.domain.TrExamDepartFile;
import me.zhengjie.service.TrExamDepartFileService;
import me.zhengjie.service.TrNewStaffFileService;
import me.zhengjie.service.dto.TrExamDepartFileQueryCriteria;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2022-04-11
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：部门考试题库")
@RequestMapping("/api/trExamDepartFile")
public class TrExamDepartFileController {

    private final TrExamDepartFileService fileService;

    @ApiOperation("导出认证证书数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('train:list')")
    public void download(HttpServletResponse response, TrExamDepartFileQueryCriteria criteria) throws IOException {
        fileService.download(fileService.queryAll(criteria), response);
    }

    @ApiOperation("查询部门考试题库")
    @GetMapping
    @PreAuthorize("@el.check('train:list')")
    public ResponseEntity<Object> query(TrExamDepartFileQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(fileService.query(criteria, pageable), HttpStatus.OK);
    }

    @Log("上传部门考试题库")
    @ApiOperation("上传部门考试题库")
    @PostMapping
    @PreAuthorize("@el.check('exam:edit')")
    public ResponseEntity<Object> uploadFile(@RequestParam("departId") Long departId, @RequestParam("name") String name, @RequestParam("enabled") Boolean enabled,
                                             @RequestParam("fileDesc") String fileDesc, @RequestParam("file") MultipartFile file) {
        fileService.uploadFile(departId, name, enabled, fileDesc, file);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Deprecated
    @Log("上传培训考试题库")
    @ApiOperation("上传培训考试题库")
    @PostMapping(value = "/uploadScheduleFile")
    @PreAuthorize("@el.check('exam:edit')")
    public ResponseEntity<Object> uploadFile(@RequestParam("trScheduleId") Long trScheduleId,@RequestParam("departIds") Set<Long> departIds, @RequestParam("name") String name, @RequestParam("enabled") Boolean enabled,
                                             @RequestParam("fileDesc") String fileDesc, @RequestParam("file") MultipartFile file) {
        fileService.uploadScheduleFile(trScheduleId,departIds, name, enabled, fileDesc, file);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改部门考试题库")
    @ApiOperation("修改部门考试题库")
    @PutMapping
    @PreAuthorize("@el.check('exam:edit')")
    public ResponseEntity<Object> update(@Validated @RequestBody TrExamDepartFile resources) {
        fileService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除部门考试题库")
    @ApiOperation("删除部门考试题库")
    @DeleteMapping
    @PreAuthorize("@el.check('exam:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        fileService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}