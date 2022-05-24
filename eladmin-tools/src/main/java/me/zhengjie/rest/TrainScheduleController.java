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
import me.zhengjie.domain.TrainSchedule;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.TrainScheduleService;
import me.zhengjie.service.dto.TrainScheduleDto;
import me.zhengjie.service.dto.TrainScheduleQueryCriteria;
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
 * @author TongMin Jie
 * @date 2022-05-18
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：培训-日程安排")
@RequestMapping("/api/train/schedule")
public class TrainScheduleController {

    private final TrainScheduleService scheduleService;
    private static final String ENTITY_NAME = "TrainSchedule";

    @ApiOperation("导出培训日程安排数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('train:list')")
    public void download(HttpServletResponse response, TrainScheduleQueryCriteria criteria) throws IOException {
        scheduleService.download(scheduleService.queryAll(criteria), response);
    }

    @ApiOperation("查询培训日程安排信息")
    @GetMapping
    @PreAuthorize("@el.check('train:list')")
    public ResponseEntity<Object> query(TrainScheduleQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(scheduleService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    @ApiOperation("查询单条培训日程安排信息")
    @GetMapping(value = "/byId")
    @PreAuthorize("@el.check('train:list')")
    public ResponseEntity<Object> getById(@RequestParam("id") Long id) {
        return new ResponseEntity<>(scheduleService.findById(id), HttpStatus.OK);
    }

    @Log("新增培训日程安排信息")
    @ApiOperation("新增培训日程安排信息")
    @PostMapping
    @PreAuthorize("@el.check('schedule:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody TrainScheduleDto resource) {
        if (resource.getId() != null) {
            throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
        }
        scheduleService.create(resource);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改培训日程安排信息")
    @ApiOperation("修改培训日程安排信息")
    @PutMapping
    @PreAuthorize("@el.check('schedule:edit')")
    public ResponseEntity<Object> update(@Validated(TrainSchedule.Update.class) @RequestBody TrainSchedule resource) {
        scheduleService.update(resource);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除培训日程安排信息")
    @ApiOperation("删除培训日程安排信息")
    @DeleteMapping
    @PreAuthorize("@el.check('schedule:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        scheduleService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}