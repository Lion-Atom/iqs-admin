package me.zhengjie.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.zhengjie.annotation.AnonymousAccess;
import me.zhengjie.annotation.Log;
import me.zhengjie.service.ToolsLogService;
import me.zhengjie.service.dto.ToolsLogDelCond;
import me.zhengjie.service.dto.ToolsLogQueryCriteria;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/5/31 16:28
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/toolsLogs")
@Api(tags = "工具：日志管理")
public class ToolsLogController {

    private final ToolsLogService toolsLogService;

    @Log("导出数据")
    @ApiOperation("导出数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check()")
    public void download(HttpServletResponse response, ToolsLogQueryCriteria criteria) throws IOException {
        toolsLogService.download(toolsLogService.queryAll(criteria), response);
    }

    @Log("查询工具日志")
    @GetMapping
    @ApiOperation("查询工具日志")
//    @PreAuthorize("@el.check()")
    @AnonymousAccess
    public ResponseEntity<Object> query(ToolsLogQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(toolsLogService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    @PostMapping(value = "/del/byCond")
    @Log("根据条件删除工具日志")
    @ApiOperation("删除指定工具型日志")
    @PreAuthorize("@el.check()")
    public ResponseEntity<Object> delInfoByCond(@RequestBody @Validated ToolsLogDelCond cond) {
        toolsLogService.delLogByCond(cond);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
