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
import me.zhengjie.service.InstruCalibrationServiceV2;
import me.zhengjie.service.dto.EquipMaintainQueryCriteria;
import me.zhengjie.service.dto.EquipMaintenanceDto;
import me.zhengjie.service.dto.InstruCalibrationDto;
import me.zhengjie.service.dto.InstruCalibrationQueryCriteria;
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
@Api(tags = "工具：仪器校准V2")
@RequestMapping("/api/instruCalibrationV2")
public class InstruCalibrationV2Controller {

    private final InstruCalibrationServiceV2 calibrationServiceV2;
    private static final String ENTITY_NAME = "InstruCalibration";

    @ApiOperation("导出仪器校准数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('instrument:list')")
    public void download(HttpServletResponse response, InstruCalibrationQueryCriteria criteria) throws IOException {
        calibrationServiceV2.download(calibrationServiceV2.queryAll(criteria), response);
    }

    @ApiOperation("查询仪器校准信息")
    @GetMapping
    @PreAuthorize("@el.check('instrument:list')")
    public ResponseEntity<Object> query(InstruCalibrationQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(calibrationServiceV2.queryAll(criteria, pageable), HttpStatus.OK);
    }

    @ApiOperation("查询单个仪器校准信息")
    @GetMapping(value = "/byId")
    @PreAuthorize("@el.check('instrument:list')")
    public ResponseEntity<Object> getById(@RequestParam("id") Long id) {
        return new ResponseEntity<>(calibrationServiceV2.findById(id), HttpStatus.OK);
    }

    @ApiOperation("查询单个仪器校准信息")
    @GetMapping(value = "/byInstruId")
    @PreAuthorize("@el.check('instrument:list')")
    public ResponseEntity<Object> getByEquipmentId(@RequestParam("instruId") Long instruId) {
        return new ResponseEntity<>(calibrationServiceV2.findByInstruId(instruId), HttpStatus.OK);
    }

    @Log("新增仪器校准信息")
    @ApiOperation("新增仪器校准信息")
    @PostMapping
    @PreAuthorize("@el.check('instrument:edit')")
    public ResponseEntity<Object> create(@Validated @RequestBody InstruCalibrationDto resource) {
        if (resource.getId() != null) {
            throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
        }
        calibrationServiceV2.create(resource);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改仪器校准信息")
    @ApiOperation("修改仪器校准信息")
    @PutMapping
    @PreAuthorize("@el.check('instrument:edit')")
    public ResponseEntity<Object> update(@Validated(EquipMaintenance.Update.class) @RequestBody InstruCalibrationDto resource) {
        calibrationServiceV2.update(resource);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除仪器校准信息")
    @ApiOperation("删除仪器校准信息")
    @DeleteMapping
    @PreAuthorize("@el.check('instrument:edit')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        // 验证是否被审核计划关联
        // caliOrgService.verification(ids);
        calibrationServiceV2.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}