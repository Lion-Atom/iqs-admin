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
import me.zhengjie.domain.IssueScore;
import me.zhengjie.domain.TemplateScore;
import me.zhengjie.service.IssueScoreService;
import me.zhengjie.service.TemplateScoreService;
import me.zhengjie.service.dto.TemplateScoreQueryDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Tong Minjie
 * @date 2021-07-26
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：审核计划-VDA6.3模板问题清单")
@RequestMapping("/api/templateScore")
public class TemplateScoreController {

    private final TemplateScoreService templateScoreService;

    @ApiOperation("查询VDA模板分数分布")
    @PostMapping(value = "/byCond")
    @PreAuthorize("@el.check('plan:list')")
    public ResponseEntity<Object> getByTemplateIdAndTypes(@RequestBody @Validated TemplateScoreQueryDto dto) {
        return new ResponseEntity<>(templateScoreService.getByTemplateIdAndTypes(dto), HttpStatus.OK);
    }

    @Log("修改VDA模板分数")
    @ApiOperation("修改VDA模板分数")
    @PutMapping
    @PreAuthorize("@el.check('plan:edit')")
    public ResponseEntity<Object> update(@Validated(TemplateScore.Update.class) @RequestBody List<TemplateScore> resources) {
        templateScoreService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}