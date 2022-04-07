package me.zhengjie.service.impl;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.config.FileProperties;
import me.zhengjie.domain.ApQuestionFile;
import me.zhengjie.domain.ApReportQuestion;
import me.zhengjie.domain.TempCerFile;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.ApQuestionFileRepository;
import me.zhengjie.repository.ApReportQuestionRepository;
import me.zhengjie.repository.ApTemplateCertificateRepository;
import me.zhengjie.repository.TempCerFileRepository;
import me.zhengjie.service.ApQuestionFileService;
import me.zhengjie.service.AuditPlanService;
import me.zhengjie.service.TempCerFileService;
import me.zhengjie.utils.FileUtil;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/11/4 10:18
 */
@Service
@RequiredArgsConstructor
public class TempCerFileServiceImpl implements TempCerFileService {

    private final FileProperties properties;
    private final AuditPlanService auditPlanService;
    private final TempCerFileRepository fileRepository;
    private final ApTemplateCertificateRepository cerRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadFile(Long cerId, MultipartFile multipartFile) {
        // 校验是否有权限
        Long planId = cerRepository.findPlanIdByCerId(cerId);
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
            TempCerFile cerFile = new TempCerFile(
                    cerId,
                    multipartFile.getOriginalFilename(),
                    file.getName(),
                    suffix,
                    file.getPath(),
                    type,
                    FileUtil.getSize(multipartFile.getSize())
            );

            fileRepository.save(cerFile);

        } catch (Exception e) {
            FileUtil.del(file);
            throw e;
        }
    }

    @Override
    public List<TempCerFile> findByCerId(Long cerId) {
        return fileRepository.findByCerId(cerId);
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
        }
        fileRepository.deleteAllByIdIn(ids);
    }
}
