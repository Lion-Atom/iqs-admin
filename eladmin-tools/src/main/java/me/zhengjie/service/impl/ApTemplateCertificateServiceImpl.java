package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.ApTemplateCertificate;
import me.zhengjie.domain.PlanTemplate;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.repository.ApTemplateCertificateRepository;
import me.zhengjie.repository.PlanTemplateRepository;
import me.zhengjie.repository.TempCerFileRepository;
import me.zhengjie.service.ApTemplateCertificateService;
import me.zhengjie.service.AuditPlanService;
import me.zhengjie.service.mapstruct.ApTemplateMapper;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/11/17 11:25
 */
@Service
@RequiredArgsConstructor
public class ApTemplateCertificateServiceImpl implements ApTemplateCertificateService {

    private final ApTemplateCertificateRepository certificateRepository;
    private final PlanTemplateRepository templateRepository;
    private final AuditPlanService auditPlanService;
    private final TempCerFileRepository fileRepository;

    @Override
    public List<ApTemplateCertificate> findByTemplateId(Long templateId) {
        return certificateRepository.findByTemplateId(templateId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(ApTemplateCertificate resources) {
        Long templateId = resources.getTemplateId();
        PlanTemplate template = templateRepository.findById(templateId).orElseGet(PlanTemplate::new);
        ValidationUtil.isNull(template.getId(), "PlanTemplate", "id", templateId);
        // 判断是否有执行改计划的权限
        auditPlanService.checkHasAuthExecute(template.getPlanId());
        // 问题重名校验
        ApTemplateCertificate certificate = certificateRepository.findByName(resources.getName());
        if (certificate != null && certificate.getTemplateId().equals(resources.getTemplateId())) {
            throw new EntityExistException(ApTemplateCertificate.class, "认证系统name", resources.getName());
        }
        certificateRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ApTemplateCertificate resources) {
        Long templateId = resources.getTemplateId();
        PlanTemplate template = templateRepository.findById(templateId).orElseGet(PlanTemplate::new);
        ValidationUtil.isNull(template.getId(), "PlanTemplate", "id", templateId);
        // 判断是否有执行改计划的权限
        auditPlanService.checkHasAuthExecute(template.getPlanId());
        // 问题重名校验
        ApTemplateCertificate certificate = certificateRepository.findByName(resources.getName());
        if (certificate != null && !certificate.getId().equals(resources.getId()) && certificate.getTemplateId().equals(resources.getTemplateId())) {
            throw new EntityExistException(ApTemplateCertificate.class, "认证系统name", resources.getName());
        }
        certificateRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        // 判断是否有执行改计划的权限
        for (Long id : ids) {
            Long planId = certificateRepository.findPlanIdByCerId(id);
            // 判断是否有执行改计划的权限
            auditPlanService.checkHasAuthExecute(planId);
        }
        certificateRepository.deleteAllByIdIn(ids);
        // 删除对应的附件
        fileRepository.deleteByCerIdIn(ids);
    }
}
