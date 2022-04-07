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
import me.zhengjie.domain.ApTemplateCertificate;
import me.zhengjie.domain.ApTemplateCertificate;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.ApTemplateCertificateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2021-11-17
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：审核管理-VDA6.3模板下认证信息")
@RequestMapping("/api/templateCertificate")
public class ApTemplateCertificateController {

    private final ApTemplateCertificateService certificateService;
    private static final String ENTITY_NAME = "ApTemplateCertificate";

    @ApiOperation("查询模板下认证信息")
    @GetMapping(value = "/byTemplateId")
    @PreAuthorize("@el.check('plan:list')")
    public ResponseEntity<Object> getById(@RequestParam("templateId") Long templateId) {
        return new ResponseEntity<>(certificateService.findByTemplateId(templateId), HttpStatus.OK);
    }

    @Log("新增认证信息")
    @ApiOperation("新增认证信息")
    @PostMapping
    @PreAuthorize("@el.check('plan:edit')")
    public ResponseEntity<Object> create(@Validated @RequestBody ApTemplateCertificate resources) {
        if (resources.getId() != null) {
            throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
        }
        certificateService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改认证信息")
    @ApiOperation("修改认证信息")
    @PutMapping
    @PreAuthorize("@el.check('plan:edit')")
    public ResponseEntity<Object> update(@Validated(ApTemplateCertificate.Update.class) @RequestBody ApTemplateCertificate resources) {
        certificateService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除认证信息")
    @ApiOperation("删除认证信息")
    @DeleteMapping
    @PreAuthorize("@el.check('plan:edit')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        // 验证是否被关联
        certificateService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}