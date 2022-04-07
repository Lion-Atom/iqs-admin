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
import me.zhengjie.modules.system.domain.ApprovalProcess;
import me.zhengjie.modules.system.service.ApprovalProcessService;
import me.zhengjie.modules.system.service.ToolsTaskService;
import me.zhengjie.modules.system.service.dto.ApprovalProcessQueryCriteria;
import me.zhengjie.modules.system.service.dto.TaskQueryCriteria;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author TongMinjie
 * @date 2021-06-23
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "系统-审批流程管理")
@RequestMapping("/api/approvalProcess")
public class ApprovalProcessController {

    private final ApprovalProcessService approvalProcessService;

    @GetMapping
    @ApiOperation("查询审批进程")
    public ResponseEntity<Object> query(ApprovalProcessQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(approvalProcessService.queryAll(criteria,pageable), HttpStatus.OK);
    }

    @Log("修改审批流程信息")
    @ApiOperation("修改审批流程信息")
    @PutMapping
    @PreAuthorize("@el.check('storage:edit')")
    // 目前审批流程权限：包含文件编辑
    public ResponseEntity<Object> update(@Validated @RequestBody ApprovalProcess process) {
        approvalProcessService.update(process);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
