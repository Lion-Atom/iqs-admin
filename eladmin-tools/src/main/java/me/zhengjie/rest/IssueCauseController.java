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

import cn.hutool.core.collection.CollectionUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.zhengjie.annotation.Log;
import me.zhengjie.domain.IssueCause;
import me.zhengjie.domain.IssueCause;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.IssueCauseService;
import me.zhengjie.service.IssueCauseService;
import me.zhengjie.service.dto.IssueCauseDto;
import me.zhengjie.service.dto.IssueCauseQueryCriteria;
import me.zhengjie.service.dto.IssueCauseQueryCriteria;
import me.zhengjie.service.dto.IssueCauseQueryDto;
import me.zhengjie.utils.PageUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
* @author Zheng Jie
* @date 2019-03-25
*/
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：8D-D4原因分析")
@RequestMapping("/api/issueCause")
public class IssueCauseController {

    private final IssueCauseService issueCauseService;
    private static final String ENTITY_NAME = "issueCause";

    @ApiOperation("查询原因")
    @GetMapping
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> query(IssueCauseQueryCriteria criteria) throws Exception {
        List<IssueCauseDto> issueCauseDtos = issueCauseService.queryAll(criteria, true);
        return new ResponseEntity<>(PageUtil.toPage(issueCauseDtos, issueCauseDtos.size()),HttpStatus.OK);
    }

    @ApiOperation("条件查询原因")
    @PostMapping("/byExample")
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> queryByExample(@RequestBody IssueCauseQueryDto queryDto) {
        return new ResponseEntity<>(issueCauseService.findByExample(queryDto), HttpStatus.OK);
    }

    @ApiOperation("根据问题标识查询原因")
    @GetMapping("/byIssueId")
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> getByIssueId(@RequestParam("issueId") Long issueId) {
        return new ResponseEntity<>(issueCauseService.findByIssueId(issueId), HttpStatus.OK);
    }

    @ApiOperation("生成原因树")
    @GetMapping("/createTree/byIssueId")
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> createTree(@RequestParam("issueId") Long issueId) {
        return new ResponseEntity<>(issueCauseService.createTree(issueId), HttpStatus.OK);
    }

    @ApiOperation("查询原因:根据ID获取同级与上级数据")
    @PostMapping("/superior")
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> getSuperior(@RequestBody List<Long> ids) {
        Set<IssueCauseDto> issueCauseDtos  = new LinkedHashSet<>();
        for (Long id : ids) {
            IssueCauseDto issueCauseDto = issueCauseService.findById(id);
            List<IssueCauseDto> causes = issueCauseService.getSuperior(issueCauseDto, new ArrayList<>());
            issueCauseDtos.addAll(causes);
        }
        return new ResponseEntity<>(issueCauseService.buildTree(new ArrayList<>(issueCauseDtos)),HttpStatus.OK);
    }

    @Log("新增原因")
    @ApiOperation("新增原因")
    @PostMapping
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> create(@Validated @RequestBody IssueCause resources){
        if (resources.getId() != null) {
            throw new BadRequestException("A new "+ ENTITY_NAME +" cannot already have an ID");
        }
        issueCauseService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改原因")
    @ApiOperation("修改原因")
    @PutMapping
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> update(@Validated(IssueCause.Update.class) @RequestBody IssueCause resources){
        issueCauseService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除原因")
    @ApiOperation("删除原因")
    @DeleteMapping
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids){
        Set<IssueCauseDto> IssueCauseDtos = new HashSet<>();
        for (Long id : ids) {
            List<IssueCause> causeList = issueCauseService.findByPid(id);
            IssueCauseDtos.add(issueCauseService.findById(id));
            if(CollectionUtil.isNotEmpty(causeList)){
                IssueCauseDtos = issueCauseService.getDeleteIssueCauses(causeList, IssueCauseDtos);
            }
        }
        issueCauseService.delete(IssueCauseDtos);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}