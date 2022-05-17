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
import me.zhengjie.service.TrExamStaffTranscriptionService;
import me.zhengjie.utils.DateUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2022-05-16
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：员工培训考试试卷信息")
@RequestMapping("/api/trExamStaffTranscript")
public class TrExamStaffTranscriptController {

    private final TrExamStaffTranscriptionService fileService;

    @ApiOperation("查询员工培训考试试卷信息")
    @GetMapping(value = "/byTrExamStaffId")
    @PreAuthorize("@el.check('train:list')")
    public ResponseEntity<Object> getByTrExamStaffId(@RequestParam("trExamStaffId") Long trExamStaffId) {
        return new ResponseEntity<>(fileService.getByTrExamStaffId(trExamStaffId), HttpStatus.OK);
    }

    @Log("上传员工培训考试试卷信息")
    @ApiOperation("上传员工培训考试试卷信息")
    @PostMapping
    @PreAuthorize("@el.check('exam:edit')")
    public ResponseEntity<Object> uploadFile(@RequestParam("trExamStaffId") Long trExamStaffId, @RequestParam("examContent") String examContent, @RequestParam("examDate") String examDate,
                                             @RequestParam("examScore") Integer examScore, @RequestParam("examPassed") Boolean examPassed, @RequestParam("examType") String examType, @RequestParam("nextDate") String nextDate,
                                             @RequestParam("resitSort") Integer resitSort, @RequestParam("examDesc") String examDesc, @RequestParam("name") String name, @RequestParam("file") MultipartFile file) throws ParseException {
        fileService.uploadFile(trExamStaffId, examContent, examDate, examScore, examPassed, examType, nextDate, resitSort, examDesc, name, file);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("删除员工培训考试试卷信息")
    @ApiOperation("删除员工培训考试试卷信息")
    @DeleteMapping
    @PreAuthorize("@el.check('exam:edit')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        fileService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}