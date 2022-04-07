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
import me.zhengjie.domain.IssueSpecial;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.IssueSpecialService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
* @author Tong Minjie
* @date 2021-07-26
*/
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：8D-特殊事件")
@RequestMapping("/api/issueSpecial")
public class IssueSpecialController {

    private final IssueSpecialService issueSpecialService;
    private static final String ENTITY_NAME = "issueSpecial";

    @ApiOperation("查询特殊事件")
    @GetMapping(value = "/byIssueId")
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> getByIssueId(@RequestParam("issueId") Long issueId, @RequestParam("type") String type) {
        return new ResponseEntity<>(issueSpecialService.findByIssueIdAndType(issueId,type), HttpStatus.OK);
    }

    @Log("新增特殊事件")
    @ApiOperation("新增特殊事件")
    @PostMapping
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> create(@Validated @RequestBody IssueSpecial resources){
        if (resources.getId() != null) {
            throw new BadRequestException("A new "+ ENTITY_NAME +" cannot already have an ID");
        }
        issueSpecialService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改特殊事件")
    @ApiOperation("修改特殊事件")
    @PutMapping
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> update(@Validated(IssueSpecial.Update.class) @RequestBody IssueSpecial resource){
        issueSpecialService.update(resource);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ApiOperation("删除特殊事件")
    @GetMapping(value = "/delByIssueId")
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> delByIssueId(@RequestParam("issueId") Long issueId) {
        issueSpecialService.delByIssueId(issueId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}