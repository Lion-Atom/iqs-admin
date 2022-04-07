package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.IssueQuestion;
import me.zhengjie.domain.Issue;
import me.zhengjie.domain.TimeManagement;
import me.zhengjie.repository.IssueQuestionRepository;
import me.zhengjie.repository.IssueRepository;
import me.zhengjie.repository.TimeManagementRepository;
import me.zhengjie.service.IssueQuestionService;
import me.zhengjie.service.TeamMemberService;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * @author tmj
 * @version 1.0
 * @date 2021/8/11 10:43
 */
@Service
@RequiredArgsConstructor
public class IssueQuestionServiceImpl implements IssueQuestionService {

    private final TeamMemberService teamMemberService;
    private final TimeManagementRepository timeMangeRepository;
    private final IssueRepository issueRepository;
    private final IssueQuestionRepository issueQuestionRepository;

    @Override
    public List<IssueQuestion> findByIssueIdAndType(Long issueId, String type) {
        return issueQuestionRepository.findByIssueIdAndType(issueId,type);
    }

    @Override
    public List<IssueQuestion> findByIssueId(Long issueId) {
        return issueQuestionRepository.findByIssueId(issueId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(List<IssueQuestion> resources) {
        if (ValidationUtil.isNotEmpty(resources)) {
            Long issueId = resources.get(0).getIssueId();
            // 权限判断
            teamMemberService.checkEditAuthorized(issueId);
            // 查询问题判null
            Issue issue = issueRepository.findById(issueId).orElseGet(Issue::new);
            ValidationUtil.isNull(issue.getId(), "Issue", "id", issueId);
            issueQuestionRepository.saveAll(resources);

            // 修改问题状态为：进行中
            issue.setStatus(CommonConstants.D_STATUS_IN_PROCESS);
            issue.setCloseTime(null);
            // issue.setScore(null);
            issue.setDuration(null);
            issueRepository.save(issue);
            TimeManagement timeManagement = timeMangeRepository.findByIssueId(issueId);
            // 此处是D2，后续步骤设置为false
            timeManagement.setD2Status(false);
            timeManagement.setD3Status(false);
            timeManagement.setD4Status(false);
            timeManagement.setD5Status(false);
            timeManagement.setD6Status(false);
            timeManagement.setD7Status(false);
            timeManagement.setD8Status(false);
            timeMangeRepository.save(timeManagement);
        }
    }

}
