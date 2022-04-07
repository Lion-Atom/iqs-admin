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
import me.zhengjie.domain.ApQuestionAction;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.ApQuesActionService;
import me.zhengjie.service.ApQuestionFileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2021-11-05
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：审核报告-问题对应的改善对策")
@RequestMapping("/api/apQuesAction")
public class ApQuestionActionController {

    private final ApQuesActionService actionService;
    private static final String ENTITY_NAME = "questionAction";

    @ApiOperation("查询审核报告下问题点对应改善对策")
    @GetMapping(value = "/byCond")
    @PreAuthorize("@el.check('plan:edit')")
    public ResponseEntity<Object> getByExample(@RequestParam("planId") Long planId, @RequestParam("quesId") Long quesId) {
        return new ResponseEntity<>(actionService.findByPlanIdAndQuesId(planId, quesId), HttpStatus.OK);
    }

    @Log("新增改善对策")
    @ApiOperation("新增改善对策")
    @PostMapping
    @PreAuthorize("@el.check('plan:edit')")
    public ResponseEntity<Object> create(@Validated @RequestBody ApQuestionAction resources) {
        if (resources.getId() != null) {
            throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
        }
        actionService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改改善对策")
    @ApiOperation("修改改善对策")
    @PutMapping
    @PreAuthorize("@el.check('plan:edit')")
    public ResponseEntity<Object> update(@Validated(ApQuestionAction.Update.class) @RequestBody ApQuestionAction resources) {
        return new ResponseEntity<>(actionService.update(resources), HttpStatus.OK);
    }

    @Log("删除改善对策")
    @ApiOperation("删除改善对策")
    @DeleteMapping
    @PreAuthorize("@el.check('plan:edit')")
    public ResponseEntity<Object> delete(@Validated(ApQuestionAction.Update.class) @RequestBody ApQuestionAction resources) {
        return new ResponseEntity<>(actionService.delete(resources),HttpStatus.OK);
    }
}