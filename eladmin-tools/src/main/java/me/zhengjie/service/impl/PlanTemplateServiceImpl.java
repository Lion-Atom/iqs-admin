package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.ApTemplateContent;
import me.zhengjie.domain.PlanTemplate;
import me.zhengjie.repository.ApTempContentRepository;
import me.zhengjie.repository.AuditPlanRepository;
import me.zhengjie.repository.PlanTemplateRepository;
import me.zhengjie.service.AuditPlanService;
import me.zhengjie.service.PlanTemplateService;
import me.zhengjie.service.dto.ApTemplateDto;
import me.zhengjie.service.mapstruct.ApTemplateMapper;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/10/8 11:16
 */
@Service
@RequiredArgsConstructor
public class PlanTemplateServiceImpl implements PlanTemplateService {

    private final PlanTemplateRepository planTemplateRepository;
    private final ApTemplateMapper apTemplateMapper;
    private final ApTempContentRepository contentRepository;
    private final AuditPlanService auditPlanService;

    @Override
    public ApTemplateDto findByPlanId(Long planId) {
        ApTemplateDto dto = null;
        PlanTemplate template = planTemplateRepository.findByPlanId(planId);
        dto = apTemplateMapper.toDto(template);
        // 查询模板内容
        ApTemplateContent content = contentRepository.findById(dto.getId()).orElseGet(ApTemplateContent::new);
        dto.setContent(content);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(PlanTemplate resource) {

    }

    @Override
    public List<ApTemplateDto> findTempByTempType(String templateType) {
        List<ApTemplateDto> list = new ArrayList<>();
        List<PlanTemplate> templates = planTemplateRepository.findByTempTypeAndDisEnabled(templateType, false);
        if (ValidationUtil.isNotEmpty(templates)) {
            list = apTemplateMapper.toDto(templates);
            list.forEach(dto -> {
                // 查询模板内容
                ApTemplateContent content = contentRepository.findById(dto.getId()).orElseGet(ApTemplateContent::new);
                dto.setContent(content);
            });
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateContent(ApTemplateContent content) {
        // 权限判断
        Long templateId = content.getId();
        // 权限判断
        PlanTemplate template = planTemplateRepository.findById(templateId).orElseGet(PlanTemplate::new);
        ValidationUtil.isNull(template.getId(), "PlanTemplate", "id", templateId);
        // 判断是否具备修改审核计划权限
        auditPlanService.checkHasAuthExecute(template.getPlanId());
        contentRepository.save(content);
    }

}
