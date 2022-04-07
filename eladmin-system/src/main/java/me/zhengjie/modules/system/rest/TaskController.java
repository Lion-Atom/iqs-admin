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
package me.zhengjie.modules.system.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.zhengjie.annotation.AnonymousAccess;
import me.zhengjie.annotation.Log;
import me.zhengjie.domain.LocalStorage;
import me.zhengjie.modules.system.domain.ToolsTask;
import me.zhengjie.modules.system.domain.User;
import me.zhengjie.modules.system.service.ToolsTaskService;
import me.zhengjie.modules.system.service.dto.TaskQueryCriteria;
import me.zhengjie.utils.SecurityUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author TongMinjie
 * @date 2021-06-23
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "系统-个人任务监控管理")
@RequestMapping("/api/toolsTask")
public class TaskController {

    private final ToolsTaskService toolsTaskService;


    @GetMapping
    @ApiOperation("查询个人任务")
    public ResponseEntity<Object> query(TaskQueryCriteria criteria, @PageableDefault(sort = {"createTime"}, direction = Sort.Direction.ASC) Pageable pageable) {
        // 联动查询
        if(criteria.getIsDone() == null){
            criteria.setApproveResult(null);
        }
        return new ResponseEntity<>(toolsTaskService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    @Log("修改任务信息")
    @ApiOperation("修改任务信息")
    @PutMapping
    @PreAuthorize("@el.check('storage:edit')")
    // 目前任务权限：包含文件编辑
    public ResponseEntity<Object> update(@Validated @RequestBody ToolsTask task) {
        toolsTaskService.update(task);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("提交任务")
    @ApiOperation("提交任务")
    @PostMapping(value = "/submit")
    @PreAuthorize("@el.check('storage:edit')")
    // 目前任务权限：包含文件编辑
    public ResponseEntity<Object> submitTask(@Validated @RequestBody ToolsTask task) {
        toolsTaskService.submit(task);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("批量处理任务")
    @ApiOperation("批量处理任务")
    @PostMapping(value = "/batch_submit")
    @PreAuthorize("@el.check('storage:edit')")
    // 目前任务权限：包含文件编辑
    public ResponseEntity<Object> submitTask(@Validated @RequestBody List<ToolsTask> tasks) {
        toolsTaskService.batchSubmit(tasks);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ApiOperation("查询任务数目")
    @GetMapping(value = "/count")
    @AnonymousAccess
    public ResponseEntity<Object> queryFilesByType() {
        return new ResponseEntity<>(toolsTaskService.queryTaskCount(), HttpStatus.OK);
    }
}
