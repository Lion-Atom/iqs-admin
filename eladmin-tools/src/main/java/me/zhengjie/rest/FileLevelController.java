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
import me.zhengjie.domain.FileLevel;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.FileLevelService;
import me.zhengjie.service.dto.FileLevelDto;
import me.zhengjie.service.dto.FileLevelQueryCriteria;
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
@Api(tags = "工具：文件等级管理")
@RequestMapping("/api/fileLevel")
public class FileLevelController {

    private final FileLevelService fileLevelService;
    private static final String ENTITY_NAME = "fileLevel";

    @ApiOperation("导出文件等级数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('level:list')")
    public void download(HttpServletResponse response, FileLevelQueryCriteria criteria) throws Exception {
        fileLevelService.download(fileLevelService.queryAll(criteria, false), response);
    }

    @ApiOperation("查询文件等级")
    @GetMapping
    @PreAuthorize("@el.check('level:list')")
    public ResponseEntity<Object> query(FileLevelQueryCriteria criteria) throws Exception {
        List<FileLevelDto> fileLevelDtos = fileLevelService.queryAll(criteria, true);
        return new ResponseEntity<>(PageUtil.toPage(fileLevelDtos, fileLevelDtos.size()),HttpStatus.OK);
    }

    @ApiOperation("查询文件等级:根据ID获取同级与上级数据")
    @PostMapping("/superior")
    @PreAuthorize("@el.check('level:list')")
    public ResponseEntity<Object> getSuperior(@RequestBody List<Long> ids) {
        Set<FileLevelDto> FileLevelDtos  = new LinkedHashSet<>();
        for (Long id : ids) {
            FileLevelDto FileLevelDto = fileLevelService.findById(id);
            List<FileLevelDto> depts = fileLevelService.getSuperior(FileLevelDto, new ArrayList<>());
            FileLevelDtos.addAll(depts);
        }
        return new ResponseEntity<>(fileLevelService.buildTree(new ArrayList<>(FileLevelDtos)),HttpStatus.OK);
    }

    @Log("新增文件等级")
    @ApiOperation("新增文件等级")
    @PostMapping
    @PreAuthorize("@el.check('level:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody FileLevel resources){
        if (resources.getId() != null) {
            throw new BadRequestException("A new "+ ENTITY_NAME +" cannot already have an ID");
        }
        fileLevelService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改文件等级")
    @ApiOperation("修改文件等级")
    @PutMapping
    @PreAuthorize("@el.check('level:edit')")
    public ResponseEntity<Object> update(@Validated(FileLevel.Update.class) @RequestBody FileLevel resources){
        fileLevelService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除文件等级")
    @ApiOperation("删除文件等级")
    @DeleteMapping
    @PreAuthorize("@el.check('level:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids){
        Set<FileLevelDto> FileLevelDtos = new HashSet<>();
        for (Long id : ids) {
            List<FileLevel> levelList = fileLevelService.findByPid(id);
            FileLevelDtos.add(fileLevelService.findById(id));
            if(CollectionUtil.isNotEmpty(levelList)){
                FileLevelDtos = fileLevelService.getDeleteFileLevels(levelList, FileLevelDtos);
            }
        }
        // 验证是否被文件关联
        fileLevelService.verification(FileLevelDtos);
        fileLevelService.delete(FileLevelDtos);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}