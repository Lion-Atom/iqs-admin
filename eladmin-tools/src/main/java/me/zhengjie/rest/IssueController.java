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
import me.zhengjie.domain.Issue;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.IssueService;
import me.zhengjie.service.dto.IssueDto;
import me.zhengjie.service.dto.IssueQueryCriteria;
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
* @author Zheng Jie
* @date 2019-03-29
*/
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：8D-问题管理")
@RequestMapping("/api/issue")
public class IssueController {

    private final IssueService issueService;
    private static final String ENTITY_NAME = "issue";

    @ApiOperation("导出问题数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('d:list')")
    public void download(HttpServletResponse response, IssueQueryCriteria criteria) throws IOException {
        issueService.download(issueService.queryAll(criteria), response);
    }

    @ApiOperation("查询问题")
    @GetMapping
    @PreAuthorize("@el.check('d:list')")
    public ResponseEntity<Object> query(IssueQueryCriteria criteria, Pageable pageable){
        return new ResponseEntity<>(issueService.queryAll(criteria, pageable),HttpStatus.OK);
    }

    @ApiOperation("查询单个问题")
    @GetMapping(value = "/byId")
    @PreAuthorize("@el.check('d:list')")
    public ResponseEntity<Object> getById(@RequestParam("issueId") Long issueId) {
        return new ResponseEntity<>(issueService.findById(issueId), HttpStatus.OK);
    }

    @Log("新增问题")
    @ApiOperation("新增问题")
    @PostMapping
    @PreAuthorize("@el.check('d:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody IssueDto resources){
        if (resources.getId() != null) {
            throw new BadRequestException("A new "+ ENTITY_NAME +" cannot already have an ID");
        }
        issueService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改问题")
    @ApiOperation("修改问题")
    @PutMapping
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> update(@Validated(Issue.Update.class) @RequestBody Issue resources){
        issueService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("问题再递交审批")
    @ApiOperation("问题再递交审批")
    @GetMapping(value = "/reactiveById")
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> reactiveById(@RequestParam("issueId") Long issueId){
        issueService.reactiveTaskById(issueId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除问题")
    @ApiOperation("删除问题")
    @DeleteMapping
    @PreAuthorize("@el.check('d:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids){
        // 验证是否被用户关联
        issueService.verification(ids);
        issueService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}