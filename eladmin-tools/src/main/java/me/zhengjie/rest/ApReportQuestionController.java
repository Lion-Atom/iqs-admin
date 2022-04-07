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
import me.zhengjie.domain.ApReportQuestion;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.ApReportQuestionService;
import me.zhengjie.service.dto.ApQuestionQueryCriteria;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2021-07-26
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：审核管理-报告下问题点")
@RequestMapping("/api/planReportQuestion")
public class ApReportQuestionController {

    private final ApReportQuestionService apReportQuestionService;
    private static final String ENTITY_NAME = "ApReportQuestion";

    @ApiOperation("导出审核问题数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('plan:list')")
    public void download(HttpServletResponse response, ApQuestionQueryCriteria criteria) throws Exception {
        apReportQuestionService.download(apReportQuestionService.queryAll(criteria), response);
    }

    @ApiOperation("导出计划报告下问题信息")
    @GetMapping(value = "/exportByReportId")
    @PreAuthorize("@el.check('plan:list')")
    public void exportByReportId(HttpServletResponse response,@RequestParam("reportId") Long reportId) throws IOException {
        apReportQuestionService.download(apReportQuestionService.findByReportId(reportId), response);
    }

    @ApiOperation("查询审核问题数据")
    @GetMapping
    @PreAuthorize("@el.check('plan:list')")
    public ResponseEntity<Object> query(ApQuestionQueryCriteria criteria, Pageable pageable){
        return new ResponseEntity<>(apReportQuestionService.queryAll(criteria, pageable),HttpStatus.OK);
    }

    @ApiOperation("查询计划报告下问题信息")
    @GetMapping(value = "/byReportId")
    @PreAuthorize("@el.check('plan:list')")
    public ResponseEntity<Object> getById(@RequestParam("reportId") Long reportId) {
        return new ResponseEntity<>(apReportQuestionService.findByReportId(reportId), HttpStatus.OK);
    }

    @Log("新增报告问题信息")
    @ApiOperation("新增报告问题信息")
    @PostMapping
    @PreAuthorize("@el.check('plan:edit')")
    public ResponseEntity<Object> create(@Validated @RequestBody ApReportQuestion resources) {
        if (resources.getId() != null) {
            throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
        }
        apReportQuestionService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改报告问题信息")
    @ApiOperation("修改报告问题信息")
    @PutMapping
    @PreAuthorize("@el.check('plan:edit')")
    public ResponseEntity<Object> update(@Validated(ApReportQuestion.Update.class) @RequestBody ApReportQuestion resources) {
        apReportQuestionService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除问题信息")
    @ApiOperation("删除问题信息")
    @DeleteMapping
    @PreAuthorize("@el.check('plan:edit')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        // 验证是否被关联
        apReportQuestionService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation("查询计划报告下问题信息")
    @GetMapping(value = "/completedById")
    @PreAuthorize("@el.check('plan:edit')")
    public ResponseEntity completedById(@RequestParam("id") Long id) {
        apReportQuestionService.completedById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}