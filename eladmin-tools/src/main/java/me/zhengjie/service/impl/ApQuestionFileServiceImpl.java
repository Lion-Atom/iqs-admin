package me.zhengjie.service.impl;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.config.FileProperties;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.ApQuestionFile;
import me.zhengjie.domain.ApReportQuestion;
import me.zhengjie.domain.AuditPlanFile;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.ApQuestionFileRepository;
import me.zhengjie.repository.ApReportQuestionRepository;
import me.zhengjie.service.ApQuestionFileService;
import me.zhengjie.service.AuditPlanService;
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
public class ApQuestionFileServiceImpl implements ApQuestionFileService {

    private final ApReportQuestionRepository questionRepository;
    private final ApQuestionFileRepository fileRepository;
    private final FileProperties properties;
    private final AuditPlanService auditPlanService;

    @Override
    public List<ApQuestionFile> findByQuesIdAndActId(Long quesId, Long actId) {
        List<ApQuestionFile> list = new ArrayList<>();
        if (actId == 0L) {
            list = fileRepository.findByQuesIdAndActIdIsNull(quesId);
        } else {
            list = fileRepository.findByQuesIdAndActId(quesId, actId);
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadFile(Long quesId, Long actId, MultipartFile multipartFile) {
        // 校验是否有权限
        Long planId = questionRepository.findPlanIdByQuesId(quesId);
        // 判断是否有执行改计划的权限
        auditPlanService.checkHasAuthExecute(planId);
        if (actId == 0L) {
            actId = null;
        }
        // 查询问题判null
        ApReportQuestion question = questionRepository.findById(quesId).orElseGet(ApReportQuestion::new);
        ValidationUtil.isNull(question.getId(), "ApReportQuestion", "id", quesId);
        FileUtil.checkSize(properties.getMaxSize(), multipartFile.getSize());
        String suffix = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
        String type = FileUtil.getFileType(suffix);
        File file = FileUtil.upload(multipartFile, properties.getPath().getPath() + type + File.separator);
        if (ObjectUtil.isNull(file)) {
            throw new BadRequestException("上传失败");
        }
        try {
            ApQuestionFile planFile = new ApQuestionFile(
                    quesId,
                    actId,
                    multipartFile.getOriginalFilename(),
                    file.getName(),
                    suffix,
                    file.getPath(),
                    type,
                    FileUtil.getSize(multipartFile.getSize())
            );

            fileRepository.save(planFile);

        } catch (Exception e) {
            FileUtil.del(file);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        for (Long id : ids) {
            // 判断是否有执行改计划的权限
            Long planId = fileRepository.findPlanIdByQuesFileId(id);
            // 判断是否有执行改计划的权限
            auditPlanService.checkHasAuthExecute(planId);
        }
        fileRepository.deleteAllByIdIn(ids);
    }
}
