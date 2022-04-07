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
import me.zhengjie.domain.TeamMember;
import me.zhengjie.domain.TimeManagement;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.TeamMemberService;
import me.zhengjie.service.TimeManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2021-07-23
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：8D-小组成员管理")
@RequestMapping("/api/teamMember")
public class TeamMembersController {

    private final TeamMemberService teamMemberService;
    private static final String ENTITY_NAME = "teamMember";

    @ApiOperation("查询小组成员")
    @GetMapping(value = "/byIssueId")
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> getById(@RequestParam("issueId") Long issueId) {
        return new ResponseEntity<>(teamMemberService.findByIssueId(issueId), HttpStatus.OK);
    }

    @Log("新增小组成员")
    @ApiOperation("新增小组成员")
    @PostMapping
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> create(@Validated @RequestBody TeamMember resources){
        if (resources.getId() != null) {
            throw new BadRequestException("A new "+ ENTITY_NAME +" cannot already have an ID");
        }
        teamMemberService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改小组成员角色")
    @ApiOperation("修改小组成员角色")
    @PutMapping
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> update(@Validated(TeamMember.Update.class) @RequestBody TeamMember resources){
        teamMemberService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除小组成员")
    @ApiOperation("删除小组成员")
    @DeleteMapping
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids){
        teamMemberService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}