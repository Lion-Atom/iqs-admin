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
import me.zhengjie.base.BaseEntity;
import me.zhengjie.domain.SelfTemplate;
import me.zhengjie.domain.TemplateScore;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.SelfTemplateService;
import me.zhengjie.service.TemplateScoreService;
import me.zhengjie.service.dto.SelfTemplateDto;
import me.zhengjie.service.dto.SelfTemplateQueryDto;
import me.zhengjie.service.dto.TemplateScoreQueryDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static org.hibernate.id.IdentifierGenerator.ENTITY_NAME;

/**
 * @author Tong Minjie
 * @date 2021-07-26
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：审核计划-自定义模板")
@RequestMapping("/api/selfTemplate")
public class SelfTemplateController {

    private final SelfTemplateService selfTemplateService;

    @Log("查询审核计划自定义模板数据")
    @ApiOperation("查询审核计划自定义模板数据")
    @GetMapping(value = "/byTemplateId")
    @PreAuthorize("@el.check('plan:list')")
    public ResponseEntity<Object> getByTemplateId(@RequestParam("templateId") Long templateId) {
        return new ResponseEntity<>(selfTemplateService.findByTemplateId(templateId), HttpStatus.OK);
    }

    @Log("根据模板ID查询审核计划自定义树形模板数据")
    @ApiOperation("查询审核计划自定义树形模板数据")
    @GetMapping(value = "/tree/byTemplateId")
    @PreAuthorize("@el.check('plan:list')")
    public ResponseEntity<Object> getTreeByTemplateId(@RequestParam("templateId") Long templateId) {
        return new ResponseEntity<>(selfTemplateService.getTreeByTemplateId(templateId), HttpStatus.OK);
    }

    @Log("根据ID查询审核计划自定义模板数据")
    @ApiOperation("根据ID查询审核计划自定义模板数据")
    @GetMapping(value = "/byId")
    @PreAuthorize("@el.check('plan:list')")
    public ResponseEntity<Object> getById(@RequestParam("id") Long id) {
        return new ResponseEntity<>(selfTemplateService.getById(id), HttpStatus.OK);
    }

    @ApiOperation("条件查询自定义模板模块数据")
    @PostMapping(value = "/byExample")
    @PreAuthorize("@el.check('plan:edit')")
    public ResponseEntity<Object> getByExample(@RequestBody SelfTemplateQueryDto queryDto) {
        return new ResponseEntity<>(selfTemplateService.findByExample(queryDto), HttpStatus.OK);
    }

    @Log("新增自定义模板数据")
    @ApiOperation("新增自定义模板数据")
    @PostMapping
    @PreAuthorize("@el.check('plan:edit')")
    public ResponseEntity<Object> create(@Validated @RequestBody SelfTemplateDto resources) {
        selfTemplateService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改自定义模板数据")
    @ApiOperation("修改自定义模板数据")
    @PostMapping(value = "/update")
    @PreAuthorize("@el.check('plan:edit')")
    public ResponseEntity<Object> update(@Validated(SelfTemplate.Update.class) @RequestBody SelfTemplate resource) {
        selfTemplateService.update(resource);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("修改自定义模板数据")
    @ApiOperation("修改自定义模板数据")
    @PutMapping
    @PreAuthorize("@el.check('plan:edit')")
    public ResponseEntity<Object> update(@Validated(SelfTemplate.Update.class) @RequestBody List<SelfTemplate> resources) {
        selfTemplateService.batchUpdate(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除自定义模块数据")
    @ApiOperation("删除自定义模块数据")
    @DeleteMapping
    @PreAuthorize("@el.check('plan:edit')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids){
        selfTemplateService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}