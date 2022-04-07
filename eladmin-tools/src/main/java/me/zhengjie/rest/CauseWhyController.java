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
import me.zhengjie.domain.TeamMember;
import me.zhengjie.domain.Why;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.TeamMemberService;
import me.zhengjie.service.WhyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2021-07-23
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：8D-5why分析")
@RequestMapping("/api/why")
public class CauseWhyController {

    private final WhyService whyService;
    private static final String ENTITY_NAME = "causeWhy";

    @ApiOperation("查询原因-5Whys")
    @GetMapping(value = "/byIssueId")
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> getByIssueId(@RequestParam("issueId") Long issueId) {
        return new ResponseEntity<>(whyService.findByIssueId(issueId), HttpStatus.OK);
    }

    @ApiOperation("查询5why")
    @GetMapping(value = "/byCauseId")
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> getByCauseId(@RequestParam("causeId") Long causeId) {
        return new ResponseEntity<>(whyService.findByCauseId(causeId), HttpStatus.OK);
    }

    @Log("修改5why")
    @ApiOperation("修改5why")
    @PutMapping
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> update(@Validated(Why.Update.class) @RequestBody List<Why> resources){
        whyService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}