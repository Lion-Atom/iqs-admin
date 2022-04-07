package me.zhengjie.service.impl;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.config.FileProperties;
import me.zhengjie.domain.TempCerFile;
import me.zhengjie.domain.TempScoreFile;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.ApTemplateCertificateRepository;
import me.zhengjie.repository.TempCerFileRepository;
import me.zhengjie.repository.TempScoreFileRepository;
import me.zhengjie.repository.TemplateScoreRepository;
import me.zhengjie.service.AuditPlanService;
import me.zhengjie.service.TempCerFileService;
import me.zhengjie.service.TempScoreFileService;
import me.zhengjie.utils.FileUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/11/4 10:18
 */
@Service
@RequiredArgsConstructor
public class TempScoreFileServiceImpl implements TempScoreFileService {

    private final FileProperties properties;
    private final AuditPlanService auditPlanService;
    private final TempScoreFileRepository fileRepository;
    private final TemplateScoreRepository scoreRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadFile(Long scoreId, MultipartFile multipartFile) {
        // 校验是否有权限
        Long planId = scoreRepository.findPlanIdById(scoreId);
        // 判断是否有执行改计划的权限
        auditPlanService.checkHasAuthExecute(planId);

        FileUtil.checkSize(properties.getMaxSize(), multipartFile.getSize());
        String suffix = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
        String type = FileUtil.getFileType(suffix);
        File file = FileUtil.upload(multipartFile, properties.getPath().getPath() + type + File.separator);
        if (ObjectUtil.isNull(file)) {
            throw new BadRequestException("上传失败");
        }
        try {
            TempScoreFile scoreFile = new TempScoreFile(
                    scoreId,
                    multipartFile.getOriginalFilename(),
                    file.getName(),
                    suffix,
                    file.getPath(),
                    type,
                    FileUtil.getSize(multipartFile.getSize())
            );

            fileRepository.save(scoreFile);

        } catch (Exception e) {
            FileUtil.del(file);
            throw e;
        }
    }

    @Override
    public List<TempScoreFile> findByScoreId(Long scoreId) {
        return fileRepository.findByScoreId(scoreId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        for (Long id : ids) {
            // 判断是否有执行改计划的权限
            // 校验是否有权限
            Long planId = fileRepository.findPlanIdById(id);
            // 判断是否有执行改计划的权限
            auditPlanService.checkHasAuthExecute(planId);
            // 判断是否有执行改计划的权限
            auditPlanService.checkHasAuthExecute(planId);
        }
        fileRepository.deleteAllByIdIn(ids);
    }
}
