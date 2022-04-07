package me.zhengjie.service;

import me.zhengjie.domain.ApTemplateCertificate;

import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/11/17 11:22
 */
public interface ApTemplateCertificateService {

    /**
     * 查询模板下认证有效性信息
     *
     * @param templateId 模板ID
     * @return 认证有效性信息
     */
    List<ApTemplateCertificate> findByTemplateId(Long templateId);

    /**
     * 创建审核报告信息
     *
     * @param resources /
     */
    void create(ApTemplateCertificate resources);

    /**
     * 更新审核报告信息
     *
     * @param resources /
     */
    void update(ApTemplateCertificate resources);

    /**
     * 删除
     *
     * @param ids /
     */
    void delete(Set<Long> ids);
}
