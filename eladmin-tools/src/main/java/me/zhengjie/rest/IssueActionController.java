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
import me.zhengjie.domain.IssueAction;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.IssueActionService;
import me.zhengjie.service.dto.ActionQueryCriteria;
import me.zhengjie.service.dto.IssueActionDto;
import me.zhengjie.service.dto.IssueActionQueryDto;
import me.zhengjie.utils.SecurityUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
* @date 2021-07-30
*/
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：8D-措施")
@RequestMapping("/api/issueAction")
public class IssueActionController {

    private final IssueActionService issueActionService;
    private static final String ENTITY_NAME = "issueAction";

    @ApiOperation("导出任务数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('d:list')")
    public void download(HttpServletResponse response, ActionQueryCriteria criteria) throws IOException {
        issueActionService.download(issueActionService.queryAll(criteria), response);
    }

    @GetMapping
    @ApiOperation("查询任务")
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> query(ActionQueryCriteria criteria, Pageable pageable) {

        return new ResponseEntity<>(issueActionService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    @ApiOperation("查询措施")
    @PostMapping(value = "/byExample")
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> getByExample(@RequestBody IssueActionQueryDto queryDto) {
        return new ResponseEntity<>(issueActionService.findByExample(queryDto), HttpStatus.OK);
    }

    @ApiOperation("查询措施")
    @GetMapping(value = "/getCanRemove")
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> getCanRemove(@RequestParam("issueId") Long issueId) {
        return new ResponseEntity<>(issueActionService.findCanRemoveByIssueId(issueId), HttpStatus.OK);
    }

    @ApiOperation("查询个人8D任务")
    @GetMapping(value = "/getUserAction")
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> getUserAction() {
        return new ResponseEntity<>(issueActionService.findActionByUserId(SecurityUtils.getCurrentUserId()), HttpStatus.OK);
    }

    @ApiOperation("根据ID查询措施")
    @GetMapping(value = "/byId")
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> getById(@RequestParam("id") Long id) {
        return new ResponseEntity<>(issueActionService.findById(id), HttpStatus.OK);
    }

    @Log("新增措施")
    @ApiOperation("新增措施")
    @PostMapping
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> create(@Validated @RequestBody IssueActionDto resources){
        if (resources.getId() != null) {
            throw new BadRequestException("A new "+ ENTITY_NAME +" cannot already have an ID");
        }
        issueActionService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改措施")
    @ApiOperation("修改措施")
    @PutMapping
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> update(@Validated(IssueAction.Update.class) @RequestBody IssueActionDto resources){
        issueActionService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除措施")
    @ApiOperation("删除措施")
    @DeleteMapping
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> delete(@Validated(IssueAction.Update.class) @RequestBody IssueAction resources){
        issueActionService.delete(resources);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}