package me.zhengjie.service;

import me.zhengjie.domain.AuditPlanFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/9/28 15:02
 */
public interface AuditPlanFileService {

    List<AuditPlanFile> findByPlanIdAndTemplateId(Long planId,Long templateId);

    void uploadFile(Long planId, Long templateId, MultipartFile file);

    void delete(Set<Long> ids);
}
