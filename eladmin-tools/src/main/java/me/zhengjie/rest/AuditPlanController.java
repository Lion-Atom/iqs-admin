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

import cn.hutool.core.collection.CollectionUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.zhengjie.annotation.Log;
import me.zhengjie.domain.AuditPlan;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.AuditPlanService;
import me.zhengjie.service.dto.*;
import me.zhengjie.utils.PageUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
* @author Zheng Jie
* @date 2019-03-25
*/
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：审核管理-计划")
@RequestMapping("/api/auditPlan")
public class AuditPlanController {

    private final AuditPlanService auditPlanService;
    private static final String ENTITY_NAME = "auditPlan";

    @ApiOperation("导出审核计划数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('plan:list')")
    public void download(HttpServletResponse response, AuditPlanQueryCriteria criteria) throws Exception {
        auditPlanService.download(auditPlanService.queryAll(criteria), response);
    }

    @ApiOperation("查询审核计划")
    @GetMapping
    @PreAuthorize("@el.check('plan:list')")
    public ResponseEntity<Object> query(AuditPlanQueryCriteria criteria, Pageable pageable){
        return new ResponseEntity<>(auditPlanService.queryAll(criteria, pageable),HttpStatus.OK);
    }

    @ApiOperation("查询进行中的审核计划")
    @PostMapping(value = "/byExample")
    @PreAuthorize("@el.check('plan:list')")
    public ResponseEntity<Object> queryByExample(@RequestBody AuditPlanQueryDto queryDto){
        return new ResponseEntity<>(auditPlanService.findByExample(queryDto),HttpStatus.OK);
    }

    @ApiOperation("查询进行中的审核计划V2")
    @PostMapping(value = "/byExampleV2")
    @PreAuthorize("@el.check('plan:list')")
    public ResponseEntity<Object> queryByExampleV2(@RequestBody AuditPlanV2QueryDto queryDto){
        return new ResponseEntity<>(auditPlanService.findByExampleV2(queryDto),HttpStatus.OK);
    }

    @ApiOperation("查询单个审核计划")
    @GetMapping(value = "/byId")
    @PreAuthorize("@el.check('plan:list')")
    public ResponseEntity<Object> getById(@RequestParam("planId") Long planId) {
        return new ResponseEntity<>(auditPlanService.findById(planId), HttpStatus.OK);
    }

    @ApiOperation("激活审核计划审核流程")
    @GetMapping(value = "/activate")
    @PreAuthorize("@el.check('auditor:edit')")
    public ResponseEntity<Object> activatedById(@RequestParam("planId") Long planId) {
        auditPlanService.activatedById(planId);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @Log("新增审核计划")
    @ApiOperation("新增审核计划")
    @PostMapping
    @PreAuthorize("@el.check('plan:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody AuditPlan resources){
        if (resources.getId() != null) {
            throw new BadRequestException("A new "+ ENTITY_NAME +" cannot already have an ID");
        }
        auditPlanService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改审核计划")
    @ApiOperation("修改审核计划")
    @PutMapping
    @PreAuthorize("@el.check('plan:edit')")
    public ResponseEntity<Object> update(@Validated(AuditPlan.Update.class) @RequestBody AuditPlan resources){
        auditPlanService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ApiOperation("问题结案提交")
    @GetMapping(value = "/submit")
    @PreAuthorize("@el.check('plan:edit')")
    public ResponseEntity submit(@RequestParam("planId") Long planId) {
        auditPlanService.submit(planId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Log("删除审核计划")
    @ApiOperation("删除审核计划")
    @DeleteMapping
    @PreAuthorize("@el.check('plan:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids){
        // 验证是否被关联
        auditPlanService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation("审核计划执行分布")
    @GetMapping(value = "/byStatus")
    @PreAuthorize("@el.check('plan:list')")
    public ResponseEntity<Object> queryAuditPlansByStatus(){
        return new ResponseEntity<>(auditPlanService.queryAuditPlansByStatus(),HttpStatus.OK);
    }

    @ApiOperation("按照年份查询审核计划执行分布")
    @GetMapping(value = "/getByYear")
    @PreAuthorize("@el.check('plan:list')")
    public ResponseEntity<Object> getRtdByYear(){
        return new ResponseEntity<>(auditPlanService.getRtdByYear(),HttpStatus.OK);
    }

    @ApiOperation("按照月份查询审核计划执行分布")
    @GetMapping(value = "/getByMonth")
    @PreAuthorize("@el.check('plan:list')")
    public ResponseEntity<Object> getRtdByMonth(){
        return new ResponseEntity<>(auditPlanService.getRtdByMonth(),HttpStatus.OK);
    }

    @ApiOperation("按照年份/月份查询审核计划执行分布")
    @PostMapping(value = "/byDate")
    @PreAuthorize("@el.check('plan:list')")
    public ResponseEntity<Object> queryAuditPlansByDate(@RequestBody ApQueryDto dto){
        return new ResponseEntity<>(auditPlanService.queryAuditPlansByDate(dto),HttpStatus.OK);
    }
}