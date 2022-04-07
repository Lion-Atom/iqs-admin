package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.IssueConclusion;
import me.zhengjie.domain.Issue;
import me.zhengjie.domain.TimeManagement;
import me.zhengjie.repository.IssueConclusionRepository;
import me.zhengjie.repository.IssueRepository;
import me.zhengjie.repository.TimeManagementRepository;
import me.zhengjie.service.IssueConclusionService;
import me.zhengjie.service.TeamMemberService;
import me.zhengjie.utils.RedisUtils;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/23 18:08
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "issueConclusion")
public class IssueConclusionServiceImpl implements IssueConclusionService {

    private final TeamMemberService teamMemberService;
    private final TimeManagementRepository timeMangeRepository;
    private final IssueRepository issueRepository;
    private final IssueConclusionRepository issueConclusionRepository;
    private final RedisUtils redisUtils;

    @Override
    // @Cacheable(key = "'issueId:' + #p0")
    public IssueConclusion findByIssueId(Long issueId) {
        return issueConclusionRepository.findByIssueId(issueId);
    }

    @Override
//    @CacheEvict(key = "'issueId:' + #p0.issueId")
    @Transactional(rollbackFor = Exception.class)
    public void update(IssueConclusion resource) {

        Long issueId = resource.getIssueId();
        // 权限校验
        teamMemberService.checkEditAuthorized(issueId);
        // 是否是管理员或组长
        IssueConclusion conclusion = issueConclusionRepository.findByIssueId(issueId);

        if (!resource.getLeaderConclusion().equals(conclusion.getLeaderConclusion()) ||
                !resource.getManagerConclusion().equals(conclusion.getManagerConclusion())) {
            teamMemberService.checkSubmitAuthorized(issueId);
        }

        // 查询问题判null
        Issue issue = issueRepository.findById(issueId).orElseGet(Issue::new);
        ValidationUtil.isNull(issue.getId(), "Issue", "id", issueId);
        issueConclusionRepository.save(resource);

        // 修改问题状态为：进行中
        issue.setStatus(CommonConstants.D_STATUS_IN_PROCESS);
        issue.setCloseTime(null);
        // issue.setScore(null);
        issue.setDuration(null);
        issueRepository.save(issue);
        TimeManagement timeManagement = timeMangeRepository.findByIssueId(issueId);
        // 此处是D8，后续步骤设置为false
        timeManagement.setD8Status(false);
        timeMangeRepository.save(timeManagement);

    }

}
