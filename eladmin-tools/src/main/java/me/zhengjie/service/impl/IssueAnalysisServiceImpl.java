package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.Issue;
import me.zhengjie.domain.IssueAction;
import me.zhengjie.domain.IssueAnalysis;
import me.zhengjie.domain.TimeManagement;
import me.zhengjie.repository.*;
import me.zhengjie.service.IssueAnalysisService;
import me.zhengjie.service.TeamMemberService;
import me.zhengjie.service.dto.IssueAnalysisDto;
import me.zhengjie.service.mapstruct.IssueAnalysisMapper;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/8/3 18:08
 */
@Service
@RequiredArgsConstructor
public class IssueAnalysisServiceImpl implements IssueAnalysisService {

    private final TeamMemberService teamMemberService;
    private final TimeManagementRepository timeMangeRepository;
    private final IssueRepository issueRepository;
    private final IssueAnalysisRepository issueAnalysisRepository;
    private final IssueAnalysisMapper analysisActionMapper;
    private final IssueActionRepository actionRepository;


    @Override
    public List<IssueAnalysisDto> findByIssueId(Long issueId) {

        // 查询问题判null
        Issue issue = issueRepository.findById(issueId).orElseGet(Issue::new);
        ValidationUtil.isNull(issue.getId(), "Issue", "id", issueId);
        List<IssueAnalysisDto> list = new ArrayList<>();
        list = analysisActionMapper.toDto(issueAnalysisRepository.findByIssueId(issueId));
        if (ValidationUtil.isNotEmpty(list)) {
            list.forEach(dto -> {
                List<IssueAction> actions = actionRepository.findByAnalysisId(dto.getId());
                List<String> names = new ArrayList<>();
                if (ValidationUtil.isNotEmpty(actions)) {
                    actions.forEach(action -> {
                        names.add(action.getName());
                    });
                }
                dto.setActionNames(names);
            });
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(IssueAnalysis resource) {
        Long issueId = resource.getIssueId();
        // 权限判断
        teamMemberService.checkEditAuthorized(issueId);
        // 查询问题判null
        Issue issue = issueRepository.findById(issueId).orElseGet(Issue::new);
        ValidationUtil.isNull(issue.getId(), "Issue", "id", issueId);
        issueAnalysisRepository.save(resource);

        // 修改问题状态为：进行中
        issue.setStatus(CommonConstants.D_STATUS_IN_PROCESS);
        issue.setCloseTime(null);
        // issue.setScore(null);
        issue.setDuration(null);
        issueRepository.save(issue);
        TimeManagement timeManagement = timeMangeRepository.findByIssueId(issueId);

        // 此处是D7，自身及后续步骤设置为false
        timeManagement.setD7Status(false);
        timeManagement.setD8Status(false);
        timeMangeRepository.save(timeManagement);

    }

}
