package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.*;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.repository.*;
import me.zhengjie.service.ConActionService;
import me.zhengjie.service.TeamMemberService;
import me.zhengjie.service.dto.ConActionDto;
import me.zhengjie.service.mapstruct.ConActionMapper;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/30 13:39
 */
@Service
@RequiredArgsConstructor
public class ConActionServiceImpl implements ConActionService {

    private final TeamMemberService teamMemberService;
    private final IssueRepository issueRepository;
    private final ConActionRepository conActionRepository;
    private final IssueActionRepository issueActionRepository;
    private final ApproverRepository approverRepository;
    private final ConActionMapper conActionMapper;
    private final TimeManagementRepository managementRepository;

    @Override
    public List<ConActionDto> findByIssueId(Long issueId) {
        // 查询问题判null
        List<ConActionDto> dtoList = new ArrayList<>();
        Issue issue = issueRepository.findById(issueId).orElseGet(Issue::new);
        ValidationUtil.isNull(issue.getId(), "Issue", "id", issueId);
        List<ConAction> list = conActionRepository.findByIssueId(issueId);
        if (ValidationUtil.isNotEmpty(list)) {
            dtoList = conActionMapper.toDto(list);
            dtoList.forEach(dto -> {
                if (dto.getResponsibleId() != null) {
                    Approver approver = approverRepository.findById(dto.getResponsibleId()).orElseGet(Approver::new);
                    ValidationUtil.isNull(approver.getId(), "Approver", "id", dto.getResponsibleId());
                    dto.setResponsibleName(approver.getUsername());
                }
            });
        }
        return dtoList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ConAction resources) {

        Long issueId = resources.getIssueId();
        // 权限判断
        teamMemberService.checkEditAuthorized(issueId);
        // 查询问题判null
        Issue issue = issueRepository.findById(issueId).orElseGet(Issue::new);
        ValidationUtil.isNull(issue.getId(), "Issue", "id", issueId);

        ConAction conAct = conActionRepository.findById(resources.getId()).orElseGet(ConAction::new);
        ValidationUtil.isNull(conAct.getId(), "ConAction", "id", resources.getId());

        // 初始化Action数据
        IssueAction issueAction = new IssueAction();
        // 围堵措施名称校验
        IssueAction action = new IssueAction();
        if (resources.getActionId() != null) {
            // 修改措施
            issueAction = issueActionRepository.findById(resources.getActionId()).orElseGet(IssueAction::new);
            ValidationUtil.isNull(issueAction.getId(), "IssueAction", "id", resources.getActionId());
            // 编辑措施重名校验
            // 重名校验
            IssueAction old = issueActionRepository.findByNameAndIssueId(resources.getActionName(),issueId);
            if (old != null && !old.getId().equals(resources.getActionId())) {
                throw new EntityExistException(IssueCause.class, "name", resources.getActionName());
            }
            action.setId(resources.getActionId());
        } else {
            //新增措施重名校验
            issueAction = issueActionRepository.findByNameAndIssueId(resources.getActionName(),issueId);
            if (issueAction != null) {
                throw new EntityExistException(IssueAction.class, "name", resources.getActionName());
            }
        }

        action.setIssueId(issueId);
        action.setName(resources.getActionName());
        action.setResponsibleId(resources.getResponsibleId());
        action.setEfficiency(resources.getEfficiency());
        action.setIsCon(true);
        action.setStatus(CommonConstants.ACTION_OPEN);
        action.setType(CommonConstants.D_STEP_D3);
        action.setPlannedTime(resources.getPlannedTime());
        IssueAction newAct = issueActionRepository.save(action);
        resources.setActionId(newAct.getId());

        // 修改问题状态为：进行中、清空关闭时间、时长等记录
        issue.setStatus(CommonConstants.D_STATUS_IN_PROCESS);
        issue.setCloseTime(null);
        // issue.setScore(null);
        issue.setDuration(null);
        issueRepository.save(issue);

        // 此处是D3，后续步骤设置为false
        TimeManagement timeManagement = managementRepository.findByIssueId(issue.getId());
        timeManagement.setD3Status(false);
        timeManagement.setD4Status(false);
        timeManagement.setD5Status(false);
        timeManagement.setD6Status(false);
        timeManagement.setD7Status(false);
        timeManagement.setD8Status(false);
        managementRepository.save(timeManagement);

        conActionRepository.save(resources);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clear(ConAction resources) {
        // 清空并删除相关Action数据
        Long issueId = resources.getIssueId();
        // 权限判断
        teamMemberService.checkEditAuthorized(issueId);
        // 查询问题判null
        Issue issue = issueRepository.findById(issueId).orElseGet(Issue::new);
        ValidationUtil.isNull(issue.getId(), "Issue", "id", issueId);

        ConAction conAct = conActionRepository.findById(resources.getId()).orElseGet(ConAction::new);
        ValidationUtil.isNull(conAct.getId(), "ConAction", "id", resources.getId());

        // 清空conAction的措施数据
        ConAction initAct = new ConAction();
        initAct.setId(conAct.getId());
        initAct.setIssueId(issueId);
        initAct.setTitle(conAct.getTitle());
        initAct.setCreateBy(conAct.getCreateBy());
        initAct.setCreateTime(conAct.getCreateTime());

        // 删除action对应的数据
        if (resources.getActionId() != null) {
            // 修改措施
            issueActionRepository.deleteById(resources.getActionId());
        }

        // 修改问题状态为：进行中、清空关闭时间、时长等记录
        issue.setStatus(CommonConstants.D_STATUS_IN_PROCESS);
        issue.setCloseTime(null);
        // issue.setScore(null);
        issue.setDuration(null);
        issueRepository.save(issue);

        // 此处是D3，后续步骤设置为false
        TimeManagement timeManagement = managementRepository.findByIssueId(issue.getId());
        timeManagement.setD3Status(false);
        timeManagement.setD4Status(false);
        timeManagement.setD5Status(false);
        timeManagement.setD6Status(false);
        timeManagement.setD7Status(false);
        timeManagement.setD8Status(false);
        managementRepository.save(timeManagement);

        conActionRepository.save(initAct);
    }
}
