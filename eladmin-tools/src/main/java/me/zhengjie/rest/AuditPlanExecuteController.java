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
import me.zhengjie.domain.AuditPlan;
import me.zhengjie.domain.AuditPlanExecute;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.AuditPlanExecuteService;
import me.zhengjie.service.AuditPlanFileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2021-07-26
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：审核管理-执行")
@RequestMapping("/api/auditPlanExecute")
public class AuditPlanExecuteController {

    private final AuditPlanExecuteService auditPlanExecuteService;
    private static final String ENTITY_NAME = "AuditPlanExecute";

    @ApiOperation("查询计划执行信息")
    @GetMapping(value = "/byPlanId")
    @PreAuthorize("@el.check('plan:list')")
    public ResponseEntity<Object> getById(@RequestParam("planId") Long planId) {
        return new ResponseEntity<>(auditPlanExecuteService.findByPlanId(planId), HttpStatus.OK);
    }

    @Log("新增执行信息")
    @ApiOperation("新增执行信息")
    @PostMapping
    @PreAuthorize("@el.check('plan:edit')")
    public ResponseEntity<Object> create(@Validated @RequestBody AuditPlanExecute resources) {
        if (resources.getId() != null) {
            throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
        }
        auditPlanExecuteService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}