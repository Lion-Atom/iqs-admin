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
import me.zhengjie.domain.EquipRepair;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.EquipRepairService;
import me.zhengjie.service.dto.EquipRepairDto;
import me.zhengjie.service.dto.EquipRepairQueryCriteria;
import me.zhengjie.utils.ValidationUtil;
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
 * @date 2022-04-08
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：设备维修")
@RequestMapping("/api/equipRepair")
public class EquipRepairController {

    private final EquipRepairService repairService;
    private static final String ENTITY_NAME = "EquipmentAcceptance";

    @ApiOperation("导出设备维修数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('repair:list')")
    public void download(HttpServletResponse response, EquipRepairQueryCriteria criteria) throws IOException {
        repairService.download(repairService.queryAll(criteria), response);
    }

    @ApiOperation("查询设备维修信息")
    @GetMapping
    @PreAuthorize("@el.check('repair:list')")
    public ResponseEntity<Object> query(EquipRepairQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(repairService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    @ApiOperation("查询单个设备维修信息")
    @GetMapping(value = "/byId")
    @PreAuthorize("@el.check('repair:list')")
    public ResponseEntity<Object> getById(@RequestParam("id") Long id) {
        return new ResponseEntity<>(repairService.findById(id), HttpStatus.OK);
    }

    @ApiOperation("查询单个设备维修信息")
    @GetMapping(value = "/byEquipId")
    @PreAuthorize("@el.check('repair:list')")
    public ResponseEntity<Object> getByEquipmentId(@RequestParam("equipId") Long equipId) {
        return new ResponseEntity<>(repairService.findByEquipmentId(equipId), HttpStatus.OK);
    }

    @Log("新增设备维修信息")
    @ApiOperation("新增设备维修信息")
    @PostMapping
    @PreAuthorize("@el.check('repair:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody EquipRepairDto resource) {
        if (resource.getId() != null) {
            throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
        }
        repairService.create(resource);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation("自动生成设备维修单号")
    @GetMapping(value = "/initRepairNum")
    @PreAuthorize("@el.check('repair:list')")
    public ResponseEntity<Object> initRepairNum() {
        return new ResponseEntity<>(repairService.initRepairNum(), HttpStatus.OK);
    }


    @Log("修改设备维修信息")
    @ApiOperation("修改设备维修信息")
    @PutMapping
    @PreAuthorize("@el.check('repair:edit')")
    public ResponseEntity<Object> update(@Validated(EquipRepair.Update.class) @RequestBody EquipRepairDto resource) {
        repairService.update(resource);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除设备维修信息")
    @ApiOperation("删除设备维修信息")
    @DeleteMapping
    @PreAuthorize("@el.check('acceptance:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        // 验证是否被审核计划关联
        // caliOrgService.verification(ids);
        repairService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}