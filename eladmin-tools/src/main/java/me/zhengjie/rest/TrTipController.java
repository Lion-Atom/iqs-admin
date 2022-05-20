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
import me.zhengjie.domain.FileDept;
import me.zhengjie.domain.TrainCertification;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.FileDeptService;
import me.zhengjie.service.TrainCertificationService;
import me.zhengjie.service.TrainTipService;
import me.zhengjie.service.dto.TrainCertificationDto;
import me.zhengjie.service.dto.TrainCertificationQueryCriteria;
import me.zhengjie.service.dto.TrainTipQueryCriteria;
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
 * @date 2022-05-07
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：培训-认证提醒信息")
@RequestMapping("/api/train/tip")
public class TrTipController {
    
    private final TrainTipService tipService;
    
    @ApiOperation("查询认证提醒信息")
    @GetMapping
    @PreAuthorize("@el.check('train:list')")
    public ResponseEntity<Object> query(TrainTipQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(tipService.queryAll(criteria, pageable), HttpStatus.OK);
    }
}