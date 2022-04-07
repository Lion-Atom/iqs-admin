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
import me.zhengjie.domain.ApTemplateContent;
import me.zhengjie.domain.PlanTemplate;
import me.zhengjie.service.PlanTemplateService;
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
@Api(tags = "工具：8D-8D分数分部")
@RequestMapping("/api/auditPlanTemplate")
public class PlanTemplateController {

    private final PlanTemplateService planTemplateService;

    @Log("查询审核计划模板")
    @ApiOperation("查询审核计划模板")
    @GetMapping(value = "/byPlanId")
    @PreAuthorize("@el.check('plan:edit')")
    public ResponseEntity<Object> getByIssueId(@RequestParam("planId") Long planId) {
        return new ResponseEntity<>(planTemplateService.findByPlanId(planId), HttpStatus.OK);
    }

    @Log("修改审核计划模板")
    @ApiOperation("修改审核计划模板")
    @PutMapping
    @PreAuthorize("@el.check('plan:edit')")
    public ResponseEntity<Object> update(@Validated(PlanTemplate.Update.class) @RequestBody PlanTemplate resource) {
        planTemplateService.update(resource);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ApiOperation("查询系统模板信息")
    @GetMapping(value = "/byTemplateType")
    @PreAuthorize("@el.check('auditor:list')")
    public ResponseEntity<Object> byTemplateType(@RequestParam("templateType") String templateType) {
        return new ResponseEntity<>(planTemplateService.findTempByTempType(templateType), HttpStatus.OK);
    }

    @Log("修改审核计划模板基础内容")
    @ApiOperation("修改审核计划模板基础内容")
    @PutMapping(value = "/content")
    @PreAuthorize("@el.check('plan:edit')")
    public ResponseEntity<Object> updateContent(@Validated(PlanTemplate.Update.class) @RequestBody ApTemplateContent content) {
        planTemplateService.updateContent(content);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}