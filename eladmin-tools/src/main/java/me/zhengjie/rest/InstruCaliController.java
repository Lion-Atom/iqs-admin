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
import me.zhengjie.domain.InstruCali;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.InstruCaliService;
import me.zhengjie.service.dto.InstruCaliDto;
import me.zhengjie.service.dto.InstruCaliQueryCriteria;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * @author TongMin Jie
 * @date 2022-03-14
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：仪器校准")
@RequestMapping("/api/instruCali")
public class InstruCaliController {

    private final InstruCaliService instruCaliService;
    private static final String ENTITY_NAME = "InstruCali";

    @ApiOperation("导出仪器校准数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('calibration:list')")
    public void download(HttpServletResponse response, InstruCaliQueryCriteria criteria) throws IOException {
        instruCaliService.download(instruCaliService.queryAll(criteria), response);
    }

    @ApiOperation("查询仪器校准信息")
    @GetMapping
    @PreAuthorize("@el.check('calibration:list')")
    public ResponseEntity<Object> query(InstruCaliQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(instruCaliService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    @ApiOperation("查询单个仪器校准信息")
    @GetMapping(value = "/byId")
    @PreAuthorize("@el.check('calibration:list')")
    public ResponseEntity<Object> getById(@RequestParam("id") Long id) {
        return new ResponseEntity<>(instruCaliService.findById(id), HttpStatus.OK);
    }

    @Log("新增仪器校准信息")
    @ApiOperation("新增仪器校准信息")
    @PostMapping
    @PreAuthorize("@el.check('calibration:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody InstruCaliDto resource) {
        if (resource.getId() != null) {
            throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
        }
        instruCaliService.create(resource);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改仪器校准信息")
    @ApiOperation("修改仪器校准信息")
    @PutMapping
    @PreAuthorize("@el.check('calibration:edit')")
    public ResponseEntity<Object> update(@Validated(InstruCali.Update.class) @RequestBody InstruCali resource) {
        instruCaliService.update(resource);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除仪器校准信息")
    @ApiOperation("删除仪器校准信息")
    @DeleteMapping
    @PreAuthorize("@el.check('calibration:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        // 验证是否被审核计划关联
        // caliOrgService.verification(ids);
        instruCaliService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}