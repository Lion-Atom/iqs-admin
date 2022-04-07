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
import me.zhengjie.domain.IssueNum;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.IssueNumService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
* @author Tong Minjie
* @date 2021-07-26
*/
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：8D-D2相关记录")
@RequestMapping("/api/issueNum")
public class IssueNumController {

    private final IssueNumService issueNumService;
    private static final String ENTITY_NAME = "issueNumber";

    @ApiOperation("查询相关记录")
    @GetMapping(value = "/byIssueId")
    @PreAuthorize("@el.check('d:list')")
    public ResponseEntity<Object> getById(@RequestParam("issueId") Long issueId) {
        return new ResponseEntity<>(issueNumService.findByIssueId(issueId), HttpStatus.OK);
    }

    @Log("新增相关记录")
    @ApiOperation("新增相关记录")
    @PostMapping
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> create(@Validated @RequestBody IssueNum resources){
        if (resources.getId() != null) {
            throw new BadRequestException("A new "+ ENTITY_NAME +" cannot already have an ID");
        }
        issueNumService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改相关记录")
    @ApiOperation("修改相关记录")
    @PutMapping
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> update(@Validated(IssueNum.Update.class) @RequestBody IssueNum resources){
        issueNumService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除相关记录")
    @ApiOperation("删除相关记录")
    @DeleteMapping
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids){
        issueNumService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}