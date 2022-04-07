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
import me.zhengjie.domain.FileCategory;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.FileCategoryService;
import me.zhengjie.service.dto.FileCategoryDto;
import me.zhengjie.service.dto.FileCategoryQueryCriteria;
import me.zhengjie.utils.PageUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
* @author Tong Minjie
* @date 2021-04-28
*/
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：文件分类管理")
@RequestMapping("/api/fileCategory")
public class FileCategoryController {

    private final FileCategoryService fileCategoryService;
    private static final String ENTITY_NAME = "FileCategory";

    @ApiOperation("导出文件分类数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('category:list')")
    public void download(HttpServletResponse response, FileCategoryQueryCriteria criteria) throws Exception {
        fileCategoryService.download(fileCategoryService.queryAll(criteria, false), response);
    }

    @ApiOperation("查询文件分类")
    @GetMapping
    @PreAuthorize("@el.check('category:list')")
    public ResponseEntity<Object> query(FileCategoryQueryCriteria criteria) throws Exception {
        List<FileCategoryDto> FileCategoryDtos = fileCategoryService.queryAll(criteria, true);
        return new ResponseEntity<>(PageUtil.toPage(FileCategoryDtos, FileCategoryDtos.size()),HttpStatus.OK);
    }

    @ApiOperation("查询文件分类:根据ID获取同级与上级数据")
    @PostMapping("/superior")
    @PreAuthorize("@el.check('category:list')")
    public ResponseEntity<Object> getSuperior(@RequestBody List<Long> ids) {
        Set<FileCategoryDto> FileCategoryDtos  = new LinkedHashSet<>();
        for (Long id : ids) {
            FileCategoryDto FileCategoryDto = fileCategoryService.findById(id);
            List<FileCategoryDto> depts = fileCategoryService.getSuperior(FileCategoryDto, new ArrayList<>());
            FileCategoryDtos.addAll(depts);
        }
        return new ResponseEntity<>(fileCategoryService.buildTree(new ArrayList<>(FileCategoryDtos)),HttpStatus.OK);
    }

    @Log("新增文件分类")
    @ApiOperation("新增文件分类")
    @PostMapping
    @PreAuthorize("@el.check('category:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody FileCategory resources){
        if (resources.getId() != null) {
            throw new BadRequestException("A new "+ ENTITY_NAME +" cannot already have an ID");
        }
        fileCategoryService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改文件分类")
    @ApiOperation("修改文件分类")
    @PutMapping
    @PreAuthorize("@el.check('category:edit')")
    public ResponseEntity<Object> update(@Validated(FileCategory.Update.class) @RequestBody FileCategory resources){
        fileCategoryService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除文件分类")
    @ApiOperation("删除文件分类")
    @DeleteMapping
    @PreAuthorize("@el.check('category:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids){
        Set<FileCategoryDto> FileCategoryDtos = new HashSet<>();
        for (Long id : ids) {
            List<FileCategory> deptList = fileCategoryService.findByPid(id);
            FileCategoryDtos.add(fileCategoryService.findById(id));
            if(CollectionUtil.isNotEmpty(deptList)){
                FileCategoryDtos = fileCategoryService.getDeleteFileCategorys(deptList, FileCategoryDtos);
            }
        }
        // 验证是否被文件关联
        fileCategoryService.verification(FileCategoryDtos);
        fileCategoryService.delete(FileCategoryDtos);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}