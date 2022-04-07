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
import me.zhengjie.domain.IssueScore;
import me.zhengjie.service.IssueScoreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
* @author Tong Minjie
* @date 2021-07-26
*/
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：8D-8D分数分部")
@RequestMapping("/api/issueScore")
public class IssueScoreController {

    private final IssueScoreService issueScoreService;

    @ApiOperation("查询8D分数分布")
    @GetMapping(value = "/byIssueId")
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> getByIssueId(@RequestParam("issueId") Long issueId) {
        return new ResponseEntity<>(issueScoreService.findByIssueId(issueId), HttpStatus.OK);
    }

    @Log("修改8D分数")
    @ApiOperation("修改8D分数")
    @PutMapping
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> update(@Validated(IssueScore.Update.class) @RequestBody List<IssueScore> resources){
        issueScoreService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}