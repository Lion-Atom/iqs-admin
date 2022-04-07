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
import me.zhengjie.domain.Equipment;
import me.zhengjie.domain.FileDept;
import me.zhengjie.domain.InstruCali;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.EquipmentService;
import me.zhengjie.service.FileDeptService;
import me.zhengjie.service.InstruCaliService;
import me.zhengjie.service.dto.EquipmentQueryByExample;
import me.zhengjie.service.dto.EquipmentQueryCriteria;
import me.zhengjie.service.dto.InstruCaliDto;
import me.zhengjie.service.dto.InstruCaliQueryCriteria;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author TongMin Jie
 * @date 2022-03-14
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：设备维护")
@RequestMapping("/api/equipment")
public class EquipmentController {

    private final FileDeptService fileDeptService;
    private final EquipmentService equipmentService;
    private static final String ENTITY_NAME = "Equipment";

    @ApiOperation("导出设备基础数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('equip:list')")
    public void download(HttpServletResponse response, EquipmentQueryCriteria criteria) throws IOException {
        //部门查询
        initDepartChildren(criteria);
        equipmentService.download(equipmentService.queryAll(criteria), response);
    }

    @ApiOperation("查询设备基础信息")
    @GetMapping
    @PreAuthorize("@el.check('equip:list')")
    public ResponseEntity<Object> query(EquipmentQueryCriteria criteria, Pageable pageable) {
        //部门查询
        initDepartChildren(criteria);
        return new ResponseEntity<>(equipmentService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    @ApiOperation("查询设备基础信息")
    @PostMapping(value = "/queryByExample")
    @PreAuthorize("@el.check('equip:list')")
    public ResponseEntity<Object> queryByExample(@RequestBody EquipmentQueryByExample queryByExample) {
        return new ResponseEntity<>(equipmentService.queryByExample(queryByExample), HttpStatus.OK);
    }

    @ApiOperation("查询单个设备基础信息")
    @GetMapping(value = "/byId")
    @PreAuthorize("@el.check('equip:list')")
    public ResponseEntity<Object> getById(@RequestParam("id") Long id) {
        return new ResponseEntity<>(equipmentService.findById(id), HttpStatus.OK);
    }

    @Log("新增设备基础信息")
    @ApiOperation("新增设备基础信息")
    @PostMapping
    @PreAuthorize("@el.check('equip:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody Equipment resource) {
        if (resource.getId() != null) {
            throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
        }
        equipmentService.create(resource);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改设备基础信息")
    @ApiOperation("修改设备基础信息")
    @PutMapping
    @PreAuthorize("@el.check('equip:edit')")
    public ResponseEntity<Object> update(@Validated(Equipment.Update.class) @RequestBody Equipment resource) {
        equipmentService.update(resource);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除设备基础信息")
    @ApiOperation("删除设备基础信息")
    @DeleteMapping
    @PreAuthorize("@el.check('equip:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        // 验证是否被审核计划关联
        // caliOrgService.verification(ids);
        equipmentService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private void initDepartChildren(EquipmentQueryCriteria criteria) {
        if (!ObjectUtils.isEmpty(criteria.getUseDepart())) {
            criteria.getUseDepartIds().add(criteria.getUseDepart());
            // 先查找是否存在子节点
            List<FileDept> data = fileDeptService.findByPid(criteria.getUseDepart());
            // 然后把子节点的ID都加入到集合中
            criteria.getUseDepartIds().addAll(fileDeptService.getDeptChildren(data));
        }
    }
}