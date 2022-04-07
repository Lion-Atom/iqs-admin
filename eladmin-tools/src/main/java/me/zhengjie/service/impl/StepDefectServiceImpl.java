package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.Issue;
import me.zhengjie.domain.IssueNum;
import me.zhengjie.domain.StepDefect;
import me.zhengjie.domain.TimeManagement;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.IssueNumRepository;
import me.zhengjie.repository.IssueRepository;
import me.zhengjie.repository.StepDefectRepository;
import me.zhengjie.repository.TimeManagementRepository;
import me.zhengjie.service.IssueNumService;
import me.zhengjie.service.StepDefectService;
import me.zhengjie.service.TeamMemberService;
import me.zhengjie.service.dto.IssueNumDto;
import me.zhengjie.service.mapstruct.IssueNumMapper;
import me.zhengjie.utils.RedisUtils;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/21 18:08
 */
@Service
@RequiredArgsConstructor
public class StepDefectServiceImpl implements StepDefectService {

    private final TeamMemberService teamMemberService;
    private final TimeManagementRepository timeMangeRepository;
    private final IssueRepository issueRepository;
    private final StepDefectRepository stepDefectRepository;

    @Override
    public List<StepDefect> findByIssueId(Long issueId) {
        return stepDefectRepository.findByIssueId(issueId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(List<StepDefect> resources) {
        if (ValidationUtil.isNotEmpty(resources)) {
            Long issueId = resources.get(0).getIssueId();
            // 权限判断
            teamMemberService.checkEditAuthorized(issueId);
            // 查询问题判null
            Issue issue = issueRepository.findById(issueId).orElseGet(Issue::new);
            ValidationUtil.isNull(issue.getId(), "Issue", "id", issueId);
            stepDefectRepository.saveAll(resources);

            // 修改问题状态为：进行中
            issue.setStatus(CommonConstants.D_STATUS_IN_PROCESS);
            issue.setCloseTime(null);
            // issue.setScore(null);
            issue.setDuration(null);
            issueRepository.save(issue);
            TimeManagement timeManagement = timeMangeRepository.findByIssueId(issueId);
            // 此处是D4，后续步骤设置为false
            timeManagement.setD4Status(false);
            timeManagement.setD5Status(false);
            timeManagement.setD6Status(false);
            timeManagement.setD7Status(false);
            timeManagement.setD8Status(false);
            timeMangeRepository.save(timeManagement);
        }
    }

}
