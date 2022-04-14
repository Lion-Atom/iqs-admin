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
import me.zhengjie.domain.EquipMaintenance;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.EquipMaintenanceService;
import me.zhengjie.service.dto.EquipMaintainQueryCriteria;
import me.zhengjie.service.dto.EquipMaintenanceDto;
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
 * @date 2022-04-12
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：设备保养")
@RequestMapping("/api/equipMaintenance")
public class EquipMaintenanceController {

    private final EquipMaintenanceService maintenanceService;
    private static final String ENTITY_NAME = "EquipmentAcceptance";

    @ApiOperation("导出设备保养数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('maintain:list')")
    public void download(HttpServletResponse response, EquipMaintainQueryCriteria criteria) throws IOException {
        maintenanceService.download(maintenanceService.queryAll(criteria), response);
    }

    @ApiOperation("查询设备保养信息")
    @GetMapping
    @PreAuthorize("@el.check('maintain:list')")
    public ResponseEntity<Object> query(EquipMaintainQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(maintenanceService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    @ApiOperation("查询单个设备保养信息")
    @GetMapping(value = "/byId")
    @PreAuthorize("@el.check('maintain:list')")
    public ResponseEntity<Object> getById(@RequestParam("id") Long id) {
        return new ResponseEntity<>(maintenanceService.findById(id), HttpStatus.OK);
    }

    @ApiOperation("查询单个设备保养信息")
    @GetMapping(value = "/byEquipId")
    @PreAuthorize("@el.check('maintain:list')")
    public ResponseEntity<Object> getByEquipmentId(@RequestParam("equipId") Long equipId) {
        return new ResponseEntity<>(maintenanceService.findByEquipmentId(equipId), HttpStatus.OK);
    }

    @Log("新增设备保养信息")
    @ApiOperation("新增设备保养信息")
    @PostMapping
    @PreAuthorize("@el.check('maintain:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody EquipMaintenanceDto resource) {
        if (resource.getId() != null) {
            throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
        }
        maintenanceService.create(resource);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改设备保养信息")
    @ApiOperation("修改设备保养信息")
    @PutMapping
    @PreAuthorize("@el.check('maintain:edit')")
    public ResponseEntity<Object> update(@Validated(EquipMaintenance.Update.class) @RequestBody EquipMaintenanceDto resource) {
        maintenanceService.update(resource);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除设备保养信息")
    @ApiOperation("删除设备保养信息")
    @DeleteMapping
    @PreAuthorize("@el.check('maintain:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        // 验证是否被审核计划关联
        // caliOrgService.verification(ids);
        maintenanceService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}