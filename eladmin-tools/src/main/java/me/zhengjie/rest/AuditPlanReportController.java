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
import me.zhengjie.domain.AuditPlanExecute;
import me.zhengjie.domain.AuditPlanReport;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.AuditPlanExecuteService;
import me.zhengjie.service.AuditPlanReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Tong Minjie
 * @date 2021-07-26
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：审核管理-报告")
@RequestMapping("/api/auditPlanReport")
public class AuditPlanReportController {

    private final AuditPlanReportService auditPlanReportService;
    private static final String ENTITY_NAME = "AuditPlanReport";

    @ApiOperation("查询计划报告信息")
    @GetMapping(value = "/byPlanId")
    @PreAuthorize("@el.check('plan:list')")
    public ResponseEntity<Object> getById(@RequestParam("planId") Long planId) {
        return new ResponseEntity<>(auditPlanReportService.findByPlanId(planId), HttpStatus.OK);
    }

    @ApiOperation("导出VDA6.3模板报告信息")
    @GetMapping(value = "/exportByPlanId")
    @PreAuthorize("@el.check('plan:list')")
    public void exportByReportId(HttpServletResponse response, @RequestParam("planId") Long planId) throws IOException {
        auditPlanReportService.download(auditPlanReportService.getInfoByPlanId(planId), response);
    }

    @Log("新增报告信息")
    @ApiOperation("新增报告信息")
    @PostMapping
    @PreAuthorize("@el.check('plan:edit')")
    @Deprecated
    public ResponseEntity<Object> create(@Validated @RequestBody AuditPlanReport resources) {
        if (resources.getId() != null) {
            throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
        }
        auditPlanReportService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改报告信息")
    @ApiOperation("修改报告信息")
    @PutMapping
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> update(@Validated(AuditPlanReport.Update.class) @RequestBody AuditPlanReport resources){
        auditPlanReportService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}