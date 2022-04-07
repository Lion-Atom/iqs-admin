package me.zhengjie.service;

import me.zhengjie.domain.AuditPlanReport;
import me.zhengjie.service.dto.AuditPlanReportDto;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/23 10:10
 */
public interface AuditPlanReportService {

    /**
     * 根据计划ID查询报告信息
     *
     * @param planId /计划标识
     * @return /
     */
    AuditPlanReport findByPlanId(Long planId);

    /**
     * 创建审核报告信息
     *
     * @param resources /
     */
    void create(AuditPlanReport resources);

    /**
     * 更新审核报告信息
     *
     * @param resources /
     */
    void update(AuditPlanReport resources);

    /**
     * 根据审核计划ID查询审核报告信息
     *
     * @param planId 审核计划ID
     * @return 审核计划报告信息
     */
    AuditPlanReportDto getInfoByPlanId(Long planId);

    /**
     * 导出VDA6.3报告信息
     *
     * @param reportDto 报告信息
     * @param response  /
     * @throws IOException /
     */
    void download(AuditPlanReportDto reportDto, HttpServletResponse response) throws IOException;

}
