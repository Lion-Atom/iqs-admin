package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.Issue;
import me.zhengjie.domain.IssueSpecial;
import me.zhengjie.domain.TimeManagement;
import me.zhengjie.repository.IssueRepository;
import me.zhengjie.repository.IssueSpecailRepository;
import me.zhengjie.repository.TimeManagementRepository;
import me.zhengjie.service.IssueSpecialService;
import me.zhengjie.service.TeamMemberService;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/8/16 14:02
 */
@Service
@RequiredArgsConstructor
public class IssueSpecialServiceImpl implements IssueSpecialService {

    private final TeamMemberService teamMemberService;
    private final TimeManagementRepository timeMangeRepository;
    private final IssueRepository issueRepository;
    private final IssueSpecailRepository issueSpecailRepository;

    @Override
    public IssueSpecial findByIssueIdAndType(Long issueId, String type) {
        return issueSpecailRepository.findByIssueIdAndType(issueId, type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(IssueSpecial resource) {
        Long issueId = resource.getIssueId();
        // 权限校验
        teamMemberService.checkEditAuthorized(issueId);

        // 查询问题判null
        Issue issue = issueRepository.findById(issueId).orElseGet(Issue::new);
        ValidationUtil.isNull(issue.getId(), "Issue", "id", issueId);
        // 判断特殊事件是否已存在
        issueSpecailRepository.deleteByIssueId(issueId);
        issueSpecailRepository.save(resource);
        // 修改问题状态为：进行中
        issue.setStatus(CommonConstants.D_STATUS_IN_PROCESS);
        issue.setCloseTime(null);
        // issue.setScore(null);
        issue.setDuration(null);
        issue.setSpecialEvent(resource.getType());
        issueRepository.save(issue);
        TimeManagement timeManagement = timeMangeRepository.findByIssueId(issueId);
        // 此处是D8，后续步骤设置为false
        timeManagement.setD4Status(false);
        timeManagement.setD8Status(false);
        timeMangeRepository.save(timeManagement);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(IssueSpecial resource) {

        Long issueId = resource.getIssueId();
        // 权限校验
        teamMemberService.checkEditAuthorized(issueId);

        // 查询问题判null
        Issue issue = issueRepository.findById(issueId).orElseGet(Issue::new);
        ValidationUtil.isNull(issue.getId(), "Issue", "id", issueId);
        IssueSpecial special = issueSpecailRepository.findByIssueIdAndType(issueId, resource.getType());
        ValidationUtil.isNull(special.getId(), "IssueSpecial", "id", resource.getId());
        resource.setId(special.getId());
        issueSpecailRepository.save(resource);

        // 修改问题状态为：进行中
        issue.setStatus(CommonConstants.D_STATUS_IN_PROCESS);
        issue.setCloseTime(null);
        // issue.setScore(null);
        issue.setDuration(null);
        issueRepository.save(issue);
        TimeManagement timeManagement = timeMangeRepository.findByIssueId(issueId);
        // 此处是D8，后续步骤设置为false
        timeManagement.setD4Status(false);
        timeManagement.setD8Status(false);
        timeMangeRepository.save(timeManagement);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delByIssueId(Long issueId) {
        Issue issue = issueRepository.findById(issueId).orElseGet(Issue::new);
        ValidationUtil.isNull(issue.getId(), "Issue", "id", issueId);
        issueSpecailRepository.deleteByIssueId(issueId);
        // 修改问题状态为：进行中
        issue.setStatus(CommonConstants.D_STATUS_IN_PROCESS);
        issue.setSpecialEvent(null);
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
