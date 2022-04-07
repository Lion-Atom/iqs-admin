package me.zhengjie.service;

import me.zhengjie.domain.ApTemplateContent;
import me.zhengjie.domain.PlanTemplate;
import me.zhengjie.service.dto.ApTemplateDto;

import java.util.List;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/10/8 11:14
 */
public interface PlanTemplateService {

    /**
     * 根据审核计划查询审核计划模板
     *
     * @param planId 审核计划标识
     * @return 审核计划模板
     */
    ApTemplateDto findByPlanId(Long planId);

    /**
     * 更新审核计划模板
     *
     * @param resource 审核计划模板信息
     */
    void update(PlanTemplate resource);

    /**
     * 查询对应的模板信息
     *
     * @param templateType 模板类型
     * @return 模板信息
     */
    List<ApTemplateDto> findTempByTempType(String templateType);

    /**
     * 更新审核计划模板内容
     *
     * @param content 审核计划模板内容
     */
    void updateContent(ApTemplateContent content);
}
