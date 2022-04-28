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
import me.zhengjie.domain.EquipAcceptanceDetail;
import me.zhengjie.service.EquipAcceptanceDetailService;
import me.zhengjie.service.EquipAcceptanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @author TongMin Jie
 * @date 2022-03-14
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：设备验收明细")
@RequestMapping("/api/equipAcceptanceDetail")
public class EquipAcceptanceDetailController {

    private final EquipAcceptanceDetailService detailService;

    @ApiOperation("查询单个设备验收明细信息")
    @GetMapping(value = "/byAcceptanceId")
    @PreAuthorize("@el.check('acceptance:list')")
    public ResponseEntity<Object> getByEquipmentId(@RequestParam("acceptanceId") Long acceptanceId) {
        return new ResponseEntity<>(detailService.findByAcceptanceId(acceptanceId), HttpStatus.OK);
    }

    @Log("修改设备验收明细信息")
    @ApiOperation("修改设备验收明细信息")
    @PutMapping
    @PreAuthorize("@el.check('acceptance:edit')")
    public ResponseEntity<Object> update(@Validated(EquipAcceptanceDetail.Update.class) @RequestBody List<EquipAcceptanceDetail> resources) {
        detailService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}