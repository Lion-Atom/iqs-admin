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
import me.zhengjie.domain.ConAction;
import me.zhengjie.service.ConActionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


/**
* @author Tong Minjie
* @date 2021-07-26
*/
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：8D-围堵措施")
@RequestMapping("/api/conAction")
public class ConActionController {

    private final ConActionService conActionService;

    @ApiOperation("查询围堵措施")
    @GetMapping(value = "/byIssueId")
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> getByIssueId(@RequestParam("issueId") Long issueId) {
        return new ResponseEntity<>(conActionService.findByIssueId(issueId), HttpStatus.OK);
    }

    @Log("修改围堵措施")
    @ApiOperation("修改围堵措施")
    @PutMapping
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> update(@Validated(ConAction.Update.class) @RequestBody ConAction resources){
        conActionService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("清空围堵措施")
    @ApiOperation("清空围堵措施")
    @PutMapping(value="/clear")
    @PreAuthorize("@el.check('d:edit')")
    public ResponseEntity<Object> clear(@Validated(ConAction.Update.class) @RequestBody ConAction resources){
        conActionService.clear(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


}