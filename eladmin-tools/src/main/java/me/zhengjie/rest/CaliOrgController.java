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
import me.zhengjie.domain.CalibrationOrg;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.CaliOrgService;
import me.zhengjie.service.dto.CaliOrgQueryByExample;
import me.zhengjie.service.dto.CaliOrgQueryCriteria;
import me.zhengjie.service.dto.CalibrationOrgDto;
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
 * @date 2022-03-11
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：仪校机构")
@RequestMapping("/api/caliOrg")
public class CaliOrgController {

    private final CaliOrgService caliOrgService;
    private static final String ENTITY_NAME = "CalibrationOrg";

    @ApiOperation("导出仪校机构数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('caliorg:list')")
    public void download(HttpServletResponse response, CaliOrgQueryCriteria criteria) throws IOException {
        caliOrgService.download(caliOrgService.queryAll(criteria), response);
    }

    @ApiOperation("查询仪校机构信息")
    @GetMapping
    @PreAuthorize("@el.check('caliorg:list')")
    public ResponseEntity<Object> query(CaliOrgQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(caliOrgService.queryAll(criteria, pageable), HttpStatus.OK);
    }
    @ApiOperation("条件查询校准机构信息")
    @PostMapping(value = "/queryByExample")
    @PreAuthorize("@el.check('equip:list')")
    public ResponseEntity<Object> queryByExample(@RequestBody CaliOrgQueryByExample queryByExample) {
        return new ResponseEntity<>(caliOrgService.queryByExample(queryByExample), HttpStatus.OK);
    }


    @ApiOperation("查询单个仪校机构信息")
    @GetMapping(value = "/byId")
    @PreAuthorize("@el.check('caliorg:list')")
    public ResponseEntity<Object> getById(@RequestParam("caliOrgId") Long caliOrgId) {
        return new ResponseEntity<>(caliOrgService.findById(caliOrgId), HttpStatus.OK);
    }

    @Log("新增仪校机构信息")
    @ApiOperation("新增仪校机构信息")
    @PostMapping
    @PreAuthorize("@el.check('caliorg:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody CalibrationOrgDto resource) {
        if (resource.getId() != null) {
            throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
        }
        caliOrgService.create(resource);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改仪校机构信息")
    @ApiOperation("修改仪校机构信息")
    @PutMapping
    @PreAuthorize("@el.check('caliorg:edit')")
    public ResponseEntity<Object> update(@Validated(CalibrationOrg.Update.class) @RequestBody CalibrationOrg resource) {
        caliOrgService.update(resource);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除仪校机构信息")
    @ApiOperation("删除仪校机构信息")
    @DeleteMapping
    @PreAuthorize("@el.check('caliorg:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        // 验证是否被审核计划关联
        // caliOrgService.verification(ids);
        caliOrgService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}