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
import me.zhengjie.domain.TimeManagement;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.TimeManagementService;
import me.zhengjie.service.dto.TimeManagementDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


/**
* @author Zheng Jie
* @date 2019-03-29
*/
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：8D-时间进程管理")
@RequestMapping("/api/timeManagement")
public class TimeManagementController {

    private final TimeManagementService timeService;
    private static final String ENTITY_NAME = "timeManagement";

    @ApiOperation("查询单个时间进程")
    @GetMapping(value = "/byIssueId")
    @PreAuthorize("@el.check('d:list')")
    public ResponseEntity<Object> getById(@RequestParam("issueId") Long issueId) {
        return new ResponseEntity<>(timeService.findByIssueId(issueId), HttpStatus.OK);
    }

    @Log("新增时间进程")
    @ApiOperation("新增时间进程")
    @PostMapping
    @PreAuthorize("@el.check('d:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody TimeManagement resources){
        if (resources.getId() != null) {
            throw new BadRequestException("A new "+ ENTITY_NAME +" cannot already have an ID");
        }
        timeService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改时间进程")
    @ApiOperation("修改时间进程")
    @PutMapping
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> update(@Validated(TimeManagement.Update.class) @RequestBody TimeManagementDto resources){
        timeService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}