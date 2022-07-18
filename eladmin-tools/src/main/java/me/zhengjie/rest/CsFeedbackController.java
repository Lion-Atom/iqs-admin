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
import me.zhengjie.domain.CsFeedback;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.CsFeedbackService;
import me.zhengjie.service.dto.CsFeedbackDto;
import me.zhengjie.service.dto.CsFeedbackQueryCriteria;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * @author TongMin Jie
 * @date 2022-07-15
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：客户反馈信息")
@RequestMapping("/api/csFeedback")
public class CsFeedbackController {
    
    private final CsFeedbackService feedbackService;
    private static final String ENTITY_NAME = "CustomerFeedback";

    @ApiOperation("导出客户反馈数据")
    @GetMapping(value = "/download")
    public void download(HttpServletResponse response, CsFeedbackQueryCriteria criteria) throws IOException {
        feedbackService.download(feedbackService.queryAll(criteria), response);
    }

    @ApiOperation("查询客户反馈信息")
    @GetMapping
    public ResponseEntity<Object> query(CsFeedbackQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(feedbackService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    @Log("新增客户反馈信息")
    @ApiOperation("新增客户反馈信息")
    @PostMapping
    public ResponseEntity<Object> create(@Validated @RequestBody CsFeedbackDto resource) {
        if (resource.getId() != null) {
            throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
        }
        feedbackService.create(resource);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改客户反馈信息")
    @ApiOperation("修改客户反馈信息")
    @PutMapping
    public ResponseEntity<Object> update(@Validated(CsFeedback.Update.class) @RequestBody CsFeedback resource) {
        feedbackService.update(resource);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除客户反馈信息")
    @ApiOperation("删除客户反馈信息")
    @DeleteMapping
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        feedbackService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}