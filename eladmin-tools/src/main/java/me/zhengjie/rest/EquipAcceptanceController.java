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
import me.zhengjie.domain.EquipAcceptance;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.EquipAcceptanceService;
import me.zhengjie.service.dto.EquipAcceptanceQueryCriteria;
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
@Api(tags = "工具：设备验收")
@RequestMapping("/api/equipAcceptance")
public class EquipAcceptanceController {

    private final EquipAcceptanceService acceptanceService;
    private static final String ENTITY_NAME = "EquipmentAcceptance";

    @ApiOperation("导出设备验收数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('acceptance:list')")
    public void download(HttpServletResponse response, EquipAcceptanceQueryCriteria criteria) throws IOException {
        acceptanceService.download(acceptanceService.queryAll(criteria), response);
    }

    @ApiOperation("查询设备验收信息")
    @GetMapping
    @PreAuthorize("@el.check('acceptance:list')")
    public ResponseEntity<Object> query(EquipAcceptanceQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(acceptanceService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    @ApiOperation("查询单个设备验收信息")
    @GetMapping(value = "/byId")
    @PreAuthorize("@el.check('acceptance:list')")
    public ResponseEntity<Object> getById(@RequestParam("id") Long id) {
        return new ResponseEntity<>(acceptanceService.findById(id), HttpStatus.OK);
    }

    @ApiOperation("查询单个设备验收信息")
    @GetMapping(value = "/byEquipId")
    @PreAuthorize("@el.check('acceptance:list')")
    public ResponseEntity<Object> getByEquipmentId(@RequestParam("equipId") Long equipId) {
        return new ResponseEntity<>(acceptanceService.findByEquipmentId(equipId), HttpStatus.OK);
    }

    @Log("新增设备验收信息")
    @ApiOperation("新增设备验收信息")
    @PostMapping
    @PreAuthorize("@el.check('acceptance:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody EquipAcceptance resource) {
        if (resource.getId() != null) {
            throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
        }
        acceptanceService.create(resource);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改设备验收信息")
    @ApiOperation("修改设备验收信息")
    @PutMapping
    @PreAuthorize("@el.check('acceptance:edit')")
    public ResponseEntity<Object> update(@Validated(EquipAcceptance.Update.class) @RequestBody EquipAcceptance resource) {
        acceptanceService.update(resource);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除设备验收信息")
    @ApiOperation("删除设备验收信息")
    @DeleteMapping
    @PreAuthorize("@el.check('acceptance:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        // 验证是否被审核计划关联
        // caliOrgService.verification(ids);
        acceptanceService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}