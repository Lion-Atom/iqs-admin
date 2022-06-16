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
package me.zhengjie.modules.system.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.zhengjie.annotation.AnonymousAccess;
import me.zhengjie.annotation.Log;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.modules.system.domain.Dict;
import me.zhengjie.modules.system.service.DictService;
import me.zhengjie.modules.system.service.OverviewService;
import me.zhengjie.modules.system.service.dto.DictQueryCriteria;
import me.zhengjie.modules.system.service.dto.OverviewQueryCriteria;
import me.zhengjie.utils.SecurityUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
* @author Zheng Jie
* @date 2019-04-10
*/
@RestController
@RequiredArgsConstructor
@Api(tags = "系统：概览管理")
@RequestMapping("/api/overview")
public class HomeController {

    private final OverviewService overviewService;

    @ApiOperation("查询概览")
    @GetMapping(value = "/all")
    @AnonymousAccess
    public ResponseEntity<Object> queryAll(){
        return new ResponseEntity<>(overviewService.queryAll(),HttpStatus.OK);
    }

    @ApiOperation("根据文件类型查询文件数目信息")
    @GetMapping(value = "/file/byFileType")
    @AnonymousAccess
    public ResponseEntity<Object> queryFilesByType(){
        Boolean isAdmin = SecurityUtils.getIsAdmin();
        return new ResponseEntity<>(overviewService.queryFilesByType(isAdmin),HttpStatus.OK);
    }

    @ApiOperation("根据文件级别查询文件数目信息")
    @GetMapping(value = "/file/byFileLevel")
    @AnonymousAccess
    public ResponseEntity<Object> queryFilesByLevel(){
        Boolean isAdmin = SecurityUtils.getIsAdmin();
        return new ResponseEntity<>(overviewService.queryFilesByLevel(isAdmin),HttpStatus.OK);
    }

    @ApiOperation("根据部门查询文件数目信息")
    @GetMapping(value = "/file/byFileDept")
    @AnonymousAccess
    public ResponseEntity<Object> queryFilesByFileDept(){
        Boolean isAdmin = SecurityUtils.getIsAdmin();
        return new ResponseEntity<>(overviewService.queryFilesByFileDept(isAdmin),HttpStatus.OK);
    }

    @ApiOperation("查询8D执行选择分布信息")
    @GetMapping(value = "/issue/byExecuteType")
    @AnonymousAccess
    public ResponseEntity<Object> queryIssuesByExecuteType(){
        return new ResponseEntity<>(overviewService.queryIssuesByExecuteType(),HttpStatus.OK);
    }

    @ApiOperation("多条件查询指定部门、人员、分类和文件增势信息")
    @PostMapping(value = "/query/byCond")
    @AnonymousAccess
    public ResponseEntity<Object> queryByCond(@RequestBody OverviewQueryCriteria criteria){
        return new ResponseEntity<>(overviewService.queryAllByCond(criteria),HttpStatus.OK);
    }

    @ApiOperation("审核系统类型执行分布")
    @GetMapping(value = "/auditPlan/byType")
//    @PreAuthorize("@el.check('plan:list')")
    public ResponseEntity<Object> queryAuditPlansByType(){
        return new ResponseEntity<>(overviewService.queryAuditPlansByType(),HttpStatus.OK);
    }

    @ApiOperation("审核体系审核员分布")
    @GetMapping(value = "/auditor/bySystem")
//    @PreAuthorize("@el.check('auditor:list')")
    public ResponseEntity<Object> queryAuditorBySystem(){
        return new ResponseEntity<>(overviewService.queryAuditorBySystem(),HttpStatus.OK);
    }

    @ApiOperation("审核体系审核员分布")
    @GetMapping(value = "/auditPlan/byReason")
//    @PreAuthorize("@el.check('plan:list')")
    public ResponseEntity<Object> queryAuditorByReason(){
        return new ResponseEntity<>(overviewService.queryAuditorByReason(),HttpStatus.OK);
    }
}