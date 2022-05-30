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
import me.zhengjie.domain.InstruCali;
import me.zhengjie.domain.Instrument;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.InstruCaliService;
import me.zhengjie.service.InstrumentService;
import me.zhengjie.service.dto.InstruCaliDto;
import me.zhengjie.service.dto.InstruCaliQueryCriteria;
import me.zhengjie.service.dto.InstrumentDto;
import me.zhengjie.service.dto.InstrumentQueryCriteria;
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
 * @author TongMin Jie
 * @date 2022-03-14
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：仪器仪表信息")
@RequestMapping("/api/instrument")
public class InstrumentController {

    private final InstrumentService instrumentService;
    private static final String ENTITY_NAME = "Instrument";

    @ApiOperation("导出仪器仪表数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('instrument:list')")
    public void download(HttpServletResponse response, InstrumentQueryCriteria criteria) throws IOException {
        instrumentService.download(instrumentService.queryAll(criteria), response);
    }

    @ApiOperation("查询仪器仪表信息")
    @GetMapping
    @PreAuthorize("@el.check('instrument:list')")
    public ResponseEntity<Object> query(InstrumentQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(instrumentService.queryByPage(criteria, pageable), HttpStatus.OK);
    }

    @ApiOperation("查询单个仪器仪表信息")
    @GetMapping(value = "/byId")
    @PreAuthorize("@el.check('instrument:list')")
    public ResponseEntity<Object> getById(@RequestParam("id") Long id) {
        return new ResponseEntity<>(instrumentService.findById(id), HttpStatus.OK);
    }

    @Log("新增仪器仪表信息")
    @ApiOperation("新增仪器仪表信息")
    @PostMapping
    @PreAuthorize("@el.check('instrument:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody InstrumentDto resource) {
        if (resource.getId() != null) {
            throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
        }
        instrumentService.create(resource);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改仪器仪表信息")
    @ApiOperation("修改仪器仪表信息")
    @PutMapping
    @PreAuthorize("@el.check('instrument:edit')")
    public ResponseEntity<Object> update(@Validated(Instrument.Update.class) @RequestBody Instrument resource) {
        instrumentService.update(resource);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除仪器仪表信息")
    @ApiOperation("删除仪器仪表信息")
    @DeleteMapping
    @PreAuthorize("@el.check('instrument:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        // 验证是否被审核计划关联
        // caliOrgService.verification(ids);
        instrumentService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}