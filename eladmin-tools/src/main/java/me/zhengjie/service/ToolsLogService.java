package me.zhengjie.service;

import me.zhengjie.domain.ToolsLog;
import me.zhengjie.service.dto.ToolsLogDelCond;
import me.zhengjie.service.dto.ToolsLogQueryCriteria;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/5/31 15:48
 */
public interface ToolsLogService {

    /**
     * 分页查询
     *
     * @param criteria 查询条件
     * @param pageable 分页参数
     * @return /
     */
    Object queryAll(ToolsLogQueryCriteria criteria, Pageable pageable);

    /**
     * 查询全部数据
     *
     * @param criteria 查询条件
     * @return /
     */
    List<ToolsLog> queryAll(ToolsLogQueryCriteria criteria);

    /**
     * 导出日志
     *
     * @param toolsLogs     待导出的数据
     * @param response /
     * @throws IOException /
     */
    void download(List<ToolsLog> toolsLogs, HttpServletResponse response) throws IOException;


    /**
     * @param cond 删除条件
     */
    void delLogByCond(ToolsLogDelCond cond);
}
