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
import me.zhengjie.domain.Auditor;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.AuditorService;
import me.zhengjie.service.dto.AuditorQueryCriteria;
import me.zhengjie.service.dto.AuditorQueryDto;
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
 * @date 2021-03-29
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：审核人员管理")
@RequestMapping("/api/auditor")
public class AuditorController {

    private final AuditorService auditorService;
    private static final String ENTITY_NAME = "auditor";

    @ApiOperation("导出审核人员数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('auditor:list')")
    public void download(HttpServletResponse response, AuditorQueryCriteria criteria) throws IOException {
        auditorService.download(auditorService.queryAll(criteria), response);
    }

    @ApiOperation("查询审核人员信息")
    @GetMapping
    @PreAuthorize("@el.check('auditor:list')")
    public ResponseEntity<Object> query(AuditorQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(auditorService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    @ApiOperation("查询审核人员信息")
    @PostMapping(value = "/byExample")
    @PreAuthorize("@el.check('auditor:list')")
    public ResponseEntity<Object> getByExample(@RequestBody AuditorQueryDto queryDto) {
        return new ResponseEntity<>(auditorService.findByExample(queryDto), HttpStatus.OK);
    }

    @ApiOperation("查询单个审核人员信息")
    @GetMapping(value = "/byId")
    @PreAuthorize("@el.check('auditor:list')")
    public ResponseEntity<Object> getById(@RequestParam("auditorId") Long auditorId) {
        return new ResponseEntity<>(auditorService.findById(auditorId), HttpStatus.OK);
    }

    @ApiOperation("查询单个审核人员信息")
    @GetMapping(value = "/byUserId")
    @PreAuthorize("@el.check('auditor:list')")
    public ResponseEntity<Object> getByUserId(@RequestParam("userId") Long userId) {
        return new ResponseEntity<>(auditorService.findByUserId(userId), HttpStatus.OK);
    }

    @ApiOperation("激活审核人员审核流程")
    @GetMapping(value = "/activate")
    @PreAuthorize("@el.check('auditor:edit')")
    public ResponseEntity<Object> activatedById(@RequestParam("auditorId") Long auditorId) {
        auditorService.activatedById(auditorId);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @Log("新增审核人员信息")
    @ApiOperation("新增审核人员信息")
    @PostMapping
    @PreAuthorize("@el.check('auditor:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody Auditor resources) {
        if (resources.getId() != null) {
            throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
        }
        auditorService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改审核人员信息")
    @ApiOperation("修改审核人员信息")
    @PutMapping
    @PreAuthorize("@el.check('auditor:edit')")
    public ResponseEntity<Object> update(@Validated(Auditor.Update.class) @RequestBody Auditor resources) {
        auditorService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除审核人员信息")
    @ApiOperation("删除审核人员信息")
    @DeleteMapping
    @PreAuthorize("@el.check('auditor:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        // 验证是否被审核计划关联
        auditorService.verification(ids);
        auditorService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation("审核员认证有效期分布")
    @GetMapping(value = "/byStatus")
    @PreAuthorize("@el.check('auditor:list')")
    public ResponseEntity<Object> queryAuditorsByStatus(){
        return new ResponseEntity<>(auditorService.queryAuditorsByStatus(),HttpStatus.OK);
    }

    @ApiOperation("审核员部门分布")
    @GetMapping(value = "/byDept")
    @PreAuthorize("@el.check('auditor:list')")
    public ResponseEntity<Object> queryAuditorsByDept(){
        return new ResponseEntity<>(auditorService.queryAuditorsByDept(),HttpStatus.OK);
    }
}