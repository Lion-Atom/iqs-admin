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
import me.zhengjie.service.AuditPlanFileService;
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
@Api(tags = "工具：审核报告-问题对应的附件")
@RequestMapping("/api/apQuestionFile")
public class ApReportQuestionFileController {

    private final ApQuestionFileService apQuestionFileService;

    @ApiOperation("查询审核报告下问题点下相关附件")
    @GetMapping(value = "/byCond")
    @PreAuthorize("@el.check('plan:edit')")
    public ResponseEntity<Object> getByExample(@RequestParam("quesId") Long quesId,@RequestParam("actId") Long actId) {
        return new ResponseEntity<>(apQuestionFileService.findByQuesIdAndActId(quesId,actId), HttpStatus.OK);
    }

    @Log("上传审核报告下问题点相关附件")
    @ApiOperation("上传审核报告下问题点相关附件")
    @PostMapping
    @PreAuthorize("@el.check('plan:edit')")
    public ResponseEntity<Object> uploadFile(@RequestParam("quesId") Long quesId,@RequestParam("actId") Long actId,
                                             @RequestParam("file") MultipartFile file) {
        apQuestionFileService.uploadFile(quesId, actId,file);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }


    @Log("删除审核报告下问题点相关附件")
    @ApiOperation("删除审核报告下问题点相关附件")
    @DeleteMapping
    @PreAuthorize("@el.check('plan:edit')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids){
        apQuestionFileService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}