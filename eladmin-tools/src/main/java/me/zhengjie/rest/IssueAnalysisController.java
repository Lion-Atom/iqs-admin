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
import me.zhengjie.base.BaseEntity;
import me.zhengjie.domain.IssueAnalysis;
import me.zhengjie.domain.IssueConclusion;
import me.zhengjie.service.IssueAnalysisService;
import me.zhengjie.service.IssueConclusionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
* @author Tong Minjie
* @date 2021-07-26
*/
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：8D-D7-过程分析")
@RequestMapping("/api/issueAnalysis")
public class IssueAnalysisController {

    private final IssueAnalysisService analysisService;

    @ApiOperation("查询D7过程分析")
    @GetMapping(value = "/byIssueId")
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> getByIssueId(@RequestParam("issueId") Long issueId) {
        return new ResponseEntity<>(analysisService.findByIssueId(issueId), HttpStatus.OK);
    }

    @Log("修改D7过程分析")
    @ApiOperation("修改D7过程分析")
    @PutMapping
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> update(@Validated(IssueAnalysis.Update.class) @RequestBody IssueAnalysis resource){
        analysisService.update(resource);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}