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
import me.zhengjie.domain.Change;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.ChangeService;
import me.zhengjie.service.dto.ChangeQueryCriteria;
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
* @author Zheng Jie
* @date 2019-03-25
*/
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：变更管理-变更信息")
@RequestMapping("/api/change")
public class ChangeController {

    private final ChangeService changeService;
    private static final String ENTITY_NAME = "change";

    @ApiOperation("导出变更信息数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('change:list')")
    public void download(HttpServletResponse response, ChangeQueryCriteria criteria) throws IOException {
        changeService.download(changeService.queryAll(criteria), response);
    }

    @ApiOperation("查询变更信息")
    @GetMapping
    @PreAuthorize("@el.check('change:list')")
    public ResponseEntity<Object> query(ChangeQueryCriteria criteria, Pageable pageable){
        return new ResponseEntity<>(changeService.queryAll(criteria, pageable),HttpStatus.OK);
    }
    

    @ApiOperation("查询单个变更信息")
    @GetMapping(value = "/byId")
    @PreAuthorize("@el.check('change:list')")
    public ResponseEntity<Object> getById(@RequestParam("changeId") Long changeId) {
        return new ResponseEntity<>(changeService.findById(changeId), HttpStatus.OK);
    }
    

    @Log("新增变更信息")
    @ApiOperation("新增变更信息")
    @PostMapping
    @PreAuthorize("@el.check('change:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody Change resources){
        if (resources.getId() != null) {
            throw new BadRequestException("A new "+ ENTITY_NAME +" cannot already have an ID");
        }
        return new ResponseEntity<>(changeService.create(resources),HttpStatus.CREATED);
    }

    @Log("修改变更信息")
    @ApiOperation("修改变更信息")
    @PutMapping
    @PreAuthorize("@el.check('change:edit')")
    public ResponseEntity<Object> update(@Validated(Change.Update.class) @RequestBody Change resources){
        return new ResponseEntity<>(changeService.update(resources),HttpStatus.ACCEPTED);
    }
    

    @Log("删除变更信息")
    @ApiOperation("删除变更信息")
    @DeleteMapping
    @PreAuthorize("@el.check('change:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids){
        // 验证是否被关联
        changeService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}