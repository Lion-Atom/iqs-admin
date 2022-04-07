package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.*;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.*;
import me.zhengjie.service.AuditPlanReportService;
import me.zhengjie.service.AuditPlanService;
import me.zhengjie.service.dto.AuditPlanReportDto;
import me.zhengjie.service.mapstruct.AuditPlanReportMapper;
import me.zhengjie.utils.FileUtil;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * @author tmj
 * @version 1.0
 * @date 2021/11/2 16:04
 */
@Service
@RequiredArgsConstructor
public class AuditPlanReportServiceImpl implements AuditPlanReportService {

    private final AuditPlanReportRepository auditPlanReportRepository;
    private final AuditPlanService auditPlanService;
    private final AuditPlanRepository auditPlanRepository;
    private final AuditPlanReportMapper reportMapper;
    private final ApTempContentRepository contentRepository;

    @Override
    public AuditPlanReport findByPlanId(Long planId) {
        AuditPlan plan = auditPlanRepository.findById(planId).orElseGet(AuditPlan::new);
        ValidationUtil.isNull(plan.getId(), "AuditPlan", "id", planId);
        return auditPlanReportRepository.findByPlanId(planId);
    }

    @Override
    @Deprecated
    @Transactional(rollbackFor = Exception.class)
    public void create(AuditPlanReport resources) {
        AuditPlan plan = auditPlanRepository.findById(resources.getPlanId()).orElseGet(AuditPlan::new);
        ValidationUtil.isNull(plan.getId(), "AuditPlan", "id", resources.getPlanId());
        // 判断是否有执行改计划的权限
        auditPlanService.checkHasAuthExecute(plan.getId());
        AuditPlanReport report = auditPlanReportRepository.findByPlanId(resources.getPlanId());
        if (report != null) {
            throw new BadRequestException("Report has Existed!不要重复加执行者！");
        }
        // 判断料号是否已存在
        auditPlanReportRepository.save(resources);
        plan.setStatus(CommonConstants.AUDIT_PLAN_STATUS_TRACE);
        auditPlanRepository.save(plan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(AuditPlanReport resources) {
        AuditPlan plan = auditPlanRepository.findById(resources.getPlanId()).orElseGet(AuditPlan::new);
        ValidationUtil.isNull(plan.getId(), "AuditPlan", "id", resources.getPlanId());
        // 判断是否有执行改计划的权限
        auditPlanService.checkHasAuthExecute(plan.getId());
        // 添加问题点单独实现
        // 首次更新成功则需要将审核计划更改;如不是追踪状态则更改/确保审核计划状态是追踪,但是否存在没有问题的审核计划呢
        if (!plan.getStatus().equals(CommonConstants.AUDIT_PLAN_STATUS_TRACE)) {
            plan.setStatus(CommonConstants.AUDIT_PLAN_STATUS_TRACE);
            auditPlanRepository.save(plan);
        }
        auditPlanReportRepository.save(resources);
    }

    @Override
    public AuditPlanReportDto getInfoByPlanId(Long planId) {
        AuditPlan plan = auditPlanRepository.findById(planId).orElseGet(AuditPlan::new);
        ValidationUtil.isNull(plan.getId(), "AuditPlan", "id", planId);
        AuditPlanReport report = auditPlanReportRepository.findByPlanId(planId);
        AuditPlanReportDto dto = reportMapper.toDto(report);
        if (dto != null) {
            dto.setPlanName(plan.getRealName());
            dto.setProduct(plan.getProduct());
            dto.setReason(plan.getReason());
            dto.setScope(plan.getScope());
            ApTemplateContent content = contentRepository.findById(plan.getTemplateId()).orElseGet(ApTemplateContent::new);
            ValidationUtil.isNull(content.getId(), "ApTemplateContent", "id", plan.getTemplateId());
            dto.setAuditTime(content.getAuditTime());
            dto.setAddress(content.getAddress());
        }
        return dto;
    }

    @Override
    public void download(AuditPlanReportDto reportDto, HttpServletResponse response) throws IOException {
        // 导出报告信息
        List<Map<String, Object>> list = new ArrayList<>();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("审核计划名称", reportDto.getPlanName());
        map.put("地点", reportDto.getAddress());
        map.put("时间", reportDto.getAuditTime());
        map.put("审核原因", reportDto.getReason());
        map.put("审核范围", reportDto.getScope());
        map.put("审核打分", (reportDto.getScore() / 10) * 100 + "%");
        map.put("等级", reportDto.getResult());
        list.add(map);

        FileUtil.downloadExcel(list, response);
    }
}
