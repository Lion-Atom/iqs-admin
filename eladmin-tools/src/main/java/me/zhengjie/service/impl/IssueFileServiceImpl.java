package me.zhengjie.service.impl;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.config.FileProperties;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.*;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.IssueFileRepository;
import me.zhengjie.repository.IssueRepository;
import me.zhengjie.repository.LocalStorageSmallRepository;
import me.zhengjie.repository.TimeManagementRepository;
import me.zhengjie.service.IssueFileService;
import me.zhengjie.service.TeamMemberService;
import me.zhengjie.service.dto.IssueBindFileDto;
import me.zhengjie.service.dto.IssueBindFileQueryDto;
import me.zhengjie.service.dto.IssueFileQueryDto;
import me.zhengjie.utils.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/28 9:35
 */
@Service
@RequiredArgsConstructor
public class IssueFileServiceImpl implements IssueFileService {

    private final TeamMemberService teamMemberService;
    private final TimeManagementRepository timeMangeRepository;
    private final IssueRepository issueRepository;
    private final IssueFileRepository issueFileRepository;
    private final FileProperties properties;
    private final LocalStorageSmallRepository storageSmallRepository;

    @Override
    public List<IssueFile> findByCond(IssueFileQueryDto criteria) {
        return issueFileRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
    }

    @Override
    public List<IssueFile> findByCondV2(IssueBindFileQueryDto criteria) {
        return issueFileRepository.findComFileByStepNameAndIssueId(criteria.getIssueId(), criteria.getStepName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IssueFile create(Long issueId, String stepName, MultipartFile multipartFile) {
        Issue issue = new Issue();
        // 权限校验
        if (!stepName.equals("D0")) {
            // D0时刻是创建待审批文件
            teamMemberService.checkEditAuthorized(issueId);
            // 查询问题判null
            issue = issueRepository.findById(issueId).orElseGet(Issue::new);
            ValidationUtil.isNull(issue.getId(), "Issue", "id", issueId);
        }
        FileUtil.checkSize(properties.getMaxSize(), multipartFile.getSize());
        String suffix = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
        String type = FileUtil.getFileType(Objects.requireNonNull(suffix));
        File file = FileUtil.uploadFile(multipartFile, issue.getEncodeNum(), stepName, properties.getPath().getPath() + type + File.separator);
        if (ObjectUtil.isNull(file)) {
            throw new BadRequestException("上传失败");
        }
        try {

            IssueFile issueFile = new IssueFile(
                    issueId,
                    stepName,
                    multipartFile.getOriginalFilename(),
                    file.getName(),
                    suffix,
                    file.getPath(),
                    type,
                    FileUtil.getSize(multipartFile.getSize()),
                    null
            );
            // 此处是D?，后续步骤设置为false,如果不走8D则时间进程为null
            TimeManagement timeManagement = timeMangeRepository.findByIssueId(issue.getId());
            resetTimeMangeAndSingleReport(issue, stepName, timeManagement);
            return issueFileRepository.save(issueFile);
        } catch (Exception e) {
            FileUtil.del(file);
            throw e;
        }
    }

    private void resetManagement(String stepName, TimeManagement timeManagement) {
        switch (stepName) {
            case CommonConstants.D_STEP_D2:
                timeManagement.setD2Status(false);
                timeManagement.setD3Status(false);
                timeManagement.setD4Status(false);
                timeManagement.setD5Status(false);
                timeManagement.setD6Status(false);
                timeManagement.setD7Status(false);
                timeManagement.setD8Status(false);
                break;
            case CommonConstants.D_STEP_D3:
                timeManagement.setD3Status(false);
                timeManagement.setD4Status(false);
                timeManagement.setD5Status(false);
                timeManagement.setD6Status(false);
                timeManagement.setD7Status(false);
                timeManagement.setD8Status(false);
                break;
            case CommonConstants.D_STEP_D4:
                timeManagement.setD4Status(false);
                timeManagement.setD5Status(false);
                timeManagement.setD6Status(false);
                timeManagement.setD7Status(false);
                timeManagement.setD8Status(false);
                break;
            case CommonConstants.D_STEP_D5:
                timeManagement.setD5Status(false);
                timeManagement.setD6Status(false);
                timeManagement.setD7Status(false);
                timeManagement.setD8Status(false);
                break;
            case CommonConstants.D_STEP_D6:
                timeManagement.setD6Status(false);
                timeManagement.setD7Status(false);
                timeManagement.setD8Status(false);
                break;
            case CommonConstants.D_STEP_D7:
                timeManagement.setD7Status(false);
                timeManagement.setD8Status(false);
                break;
            case CommonConstants.D_STEP_REPORT:
                break;
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        // 权限校验
        ids.forEach(teamMemberService::checkEditAuthorized);
        //修改问题状态
        List<Long> idList = new ArrayList<>(ids);
        IssueFile file = issueFileRepository.findById(idList.get(0)).orElseGet(IssueFile::new);
        ValidationUtil.isNull(file.getId(), "IssueFile", "id", idList.get(0));
        Issue issue = issueRepository.findById(file.getIssueId()).orElseGet(Issue::new);
        // 锁定当前所在步骤,当前的每次删除附件操作只可能存在于一个步骤下

        // stepName 当前附件只存在于2/3/5/6/7
        String stepName = file.getStepName();

        // 修改问题状态为：进行中、清空关闭时间、时长等记录
        issue.setStatus(CommonConstants.D_STATUS_IN_PROCESS);
        issue.setCloseTime(null);
        // issue.setScore(null);
        issue.setDuration(null);
        issueRepository.save(issue);

        // 此处是D?，后续步骤设置为false
        TimeManagement timeManagement = timeMangeRepository.findByIssueId(issue.getId());
        resetTimeMangeAndSingleReport(issue, stepName, timeManagement);
        issueFileRepository.deleteAllByIdIn(ids);
    }

    @Override
    public List<IssueFile> getBindFilesByExample(IssueBindFileQueryDto queryDto) {
        return issueFileRepository.findTempFileByStepNameAndIssueId(queryDto.getIssueId(), queryDto.getStepName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncTempFiles(List<IssueBindFileDto> resources) {
        // 删除原有数据
        issueFileRepository.deleteTempByIssueIdAndStepName(resources.get(0).getIssueId(), resources.get(0).getStepName());
        // 新增当前传入的临时文件
        List<IssueFile> issueFiles = new ArrayList<>();
        resources.forEach(dto -> {
            LocalStorageSmall storage = storageSmallRepository.findById(dto.getStorageId()).orElseGet(LocalStorageSmall::new);
            if (storage.getId() != null) {
                IssueFile issueFile = new IssueFile(
                        dto.getIssueId(),
                        dto.getStepName(),
                        storage.getRealName(),
                        storage.getName(),
                        storage.getSuffix(),
                        storage.getPath(),
                        storage.getType(),
                        storage.getSize(),
                        storage.getId()
                );
                issueFiles.add(issueFile);
            }
        });
        issueFileRepository.saveAll(issueFiles);
    }

    private void resetTimeMangeAndSingleReport(Issue issue, String stepName, TimeManagement timeManagement) {
        if (timeManagement != null) {
            resetManagement(stepName, timeManagement);
            timeMangeRepository.save(timeManagement);
        } else if (stepName.equals(CommonConstants.D_STEP_REPORT)) {
            // 单独报告
            issue.setStatus(CommonConstants.D_STATUS_IN_PROCESS);
            issue.setDuration(null);
            issue.setCloseTime(null);
            issueRepository.save(issue);
        }
    }
}

