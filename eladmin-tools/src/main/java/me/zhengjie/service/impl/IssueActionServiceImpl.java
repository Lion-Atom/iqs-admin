package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.*;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.repository.*;
import me.zhengjie.service.IssueActionService;
import me.zhengjie.service.TeamMemberService;
import me.zhengjie.service.dto.ActionQueryCriteria;
import me.zhengjie.service.dto.IssueActionDto;
import me.zhengjie.service.dto.IssueActionQueryDto;
import me.zhengjie.service.dto.IssueCauseQueryDto;
import me.zhengjie.service.mapstruct.IssueActionMapper;
import me.zhengjie.utils.FileUtil;
import me.zhengjie.utils.QueryHelp;
import me.zhengjie.utils.SecurityUtils;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import javax.swing.*;
import java.io.IOException;
import java.util.*;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/30 16:17
 */
@Service
@RequiredArgsConstructor
public class IssueActionServiceImpl implements IssueActionService {

    private final TeamMemberService teamMemberService;
    private final TimeManagementRepository timeMangeRepository;
    private final IssueRepository issueRepository;
    private final IssueActionRepository issueActionRepository;
    private final IssueCauseRepository issueCauseRepository;
    private final ApproverRepository approverRepository;
    private final IssueActionMapper issueActionMapper;
    private final AnalysisActionRepository analysisActionRepository;

    @Override
    public List<IssueActionDto> findByIssueId(Long issueId) {
        // 查询问题判null
        List<IssueActionDto> dtoList = new ArrayList<>();
        Issue issue = issueRepository.findById(issueId).orElseGet(Issue::new);
        ValidationUtil.isNull(issue.getId(), "Issue", "id", issueId);
        List<IssueAction> list = issueActionRepository.findByIssueId(issueId);
        if (ValidationUtil.isNotEmpty(list)) {
            dtoList = issueActionMapper.toDto(list);
            dtoList.forEach(dto -> {
                // 获取负责人姓名
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
    public void update(IssueActionDto resources) {
        Long issueId = resources.getIssueId();
        // 权限判断
        teamMemberService.checkEditAuthorized(issueId);
        // 查询问题判null
        Issue issue = issueRepository.findById(issueId).orElseGet(Issue::new);
        ValidationUtil.isNull(issue.getId(), "Issue", "id", issueId);

        //重名校验
        IssueAction action = issueActionRepository.findById(resources.getId()).orElseGet(IssueAction::new);
        // 重名校验
        IssueAction old = issueActionRepository.findByNameAndIssueId(resources.getName(),issueId);
        if (old != null && !old.getId().equals(resources.getId())) {
            throw new EntityExistException(IssueCause.class, "name", resources.getName());
        }
        ValidationUtil.isNull(action.getId(), "IssueAction", "id", resources.getId());
        resources.setId(action.getId());

        issueActionRepository.save(issueActionMapper.toEntity(resources));

        // 修改时间进程和问题状态
        String type = resources.getType();
        // 修改问题状态为：进行中、清空关闭时间、时长等记录
        issue.setStatus(CommonConstants.D_STATUS_IN_PROCESS);
        issue.setCloseTime(null);
        // issue.setScore(null);
        issue.setDuration(null);
        issueRepository.save(issue);

        // 此处是至少D3，后续步骤设置为false
        TimeManagement timeManagement = timeMangeRepository.findByIssueId(issue.getId());
        if (resources.getStepName() != null && resources.getStepName().equals(CommonConstants.D_STEP_D7)) {
            // 如果是D7修改，则阻断对D3-D6的影响
            timeManagement.setD7Status(false);
            timeManagement.setD8Status(false);
            timeMangeRepository.save(timeManagement);
        } else {
            resetTimeMangeAndSingleReport(issue, type, timeManagement);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(IssueActionDto resources) {

        Long issueId = resources.getIssueId();
        // 权限判断
        teamMemberService.checkEditAuthorized(issueId);
        // 查询问题判null
        Issue issue = issueRepository.findById(issueId).orElseGet(Issue::new);
        ValidationUtil.isNull(issue.getId(), "Issue", "id", issueId);

        //重名校验
        IssueAction action = issueActionRepository.findByNameAndIssueId(resources.getName(),issueId);
        if (action != null) {
            throw new EntityExistException(IssueAction.class, "name", resources.getName());
        }

        IssueAction newAction = issueActionRepository.save(issueActionMapper.toEntity(resources));

        // todo关联分析-永久信息数据同步
        if (resources.getAnalysisId() != null) {
            AnalysisAction analysisAction = new AnalysisAction();
            analysisAction.setAnalysisId(resources.getAnalysisId());
            analysisAction.setActionId(newAction.getId());
            analysisActionRepository.save(analysisAction);
        }

        // 修改时间进程和问题状态
        String type = resources.getType();
        // 修改问题状态为：进行中、清空关闭时间、时长等记录
        issue.setStatus(CommonConstants.D_STATUS_IN_PROCESS);
        issue.setCloseTime(null);
        // issue.setScore(null);
        issue.setDuration(null);
        issueRepository.save(issue);

        // 此处是D3，后续步骤设置为false
        TimeManagement timeManagement = timeMangeRepository.findByIssueId(issue.getId());
        resetTimeMangeAndSingleReport(issue, type, timeManagement);
    }

    private void resetTimeMangeAndSingleReport(Issue issue, String type, TimeManagement timeManagement) {
        if (timeManagement != null) {
            resetTimeManagement(type, timeManagement);
            timeMangeRepository.save(timeManagement);
        } else if (type.equals(CommonConstants.D_STEP_REPORT)) {
            // 单独报告
            issue.setStatus(CommonConstants.D_STATUS_IN_PROCESS);
            issue.setDuration(null);
            issue.setCloseTime(null);
            issueRepository.save(issue);
        }
    }

    private void resetTimeManagement(String type, TimeManagement timeManagement) {
        switch (type) {
            case CommonConstants.D_STEP_D3:
                timeManagement.setD3Status(false);
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
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(IssueAction resources) {

        Long issueId = resources.getIssueId();
        // 权限判断
        teamMemberService.checkEditAuthorized(issueId);
        // 查询问题判null
        Issue issue = issueRepository.findById(issueId).orElseGet(Issue::new);
        ValidationUtil.isNull(issue.getId(), "Issue", "id", issueId);

        // 修改时间进程和问题状态
        String type = resources.getType();
        // 修改问题状态为：进行中、清空关闭时间、时长等记录
        issue.setStatus(CommonConstants.D_STATUS_IN_PROCESS);
        issue.setCloseTime(null);
        // issue.setScore(null);
        issue.setDuration(null);
        issueRepository.save(issue);

        // 此处是D3，后续步骤设置为false
        TimeManagement timeManagement = timeMangeRepository.findByIssueId(issue.getId());

        resetTimeMangeAndSingleReport(issue, type, timeManagement);

        issueActionRepository.deleteById(resources.getId());

        // 删除分析-永久措施绑定项
        analysisActionRepository.deleteByActionId(resources.getId());
    }

    @Override
    public List<IssueActionDto> findByExample(IssueActionQueryDto criteria) {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Boolean isImCorrectAct = criteria.getIsImCorrectAct();
        IssueCauseQueryDto queryDto = new IssueCauseQueryDto();
        queryDto.setIssueId(criteria.getIssueId());
        queryDto.setIsExact(true);
        List<IssueActionDto> list = new ArrayList<>();
        if (criteria.getType() != null && criteria.getType().equals(CommonConstants.D_STEP_D5)) {
            List<IssueCause> causeList = issueCauseRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, queryDto, criteriaBuilder), sort);
            List<IssueActionDto> tempList = issueActionMapper.toDto(issueActionRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), sort));
            if (ValidationUtil.isNotEmpty(causeList)) {
                List<Long> planIdList = new ArrayList<>();
                List<Long> diffIdList = new ArrayList<>();
                causeList.forEach(cause -> {
                    planIdList.add(cause.getId());
                });
                // 根本原因列表不为空
                if (ValidationUtil.isNotEmpty(tempList)) {
                    Set<Long> causeIdList = new HashSet<>();
                    tempList.forEach(temp -> {
                        // D5时候必须绑定causeId
                        causeIdList.add(temp.getCauseId());
                        IssueCause cause = issueCauseRepository.findById(temp.getCauseId()).orElseGet(IssueCause::new);
                        ValidationUtil.isNull(cause.getId(), "IssueCause", "id", temp.getCauseId());
                        temp.setCauseName(cause.getName());
                        temp.setJudgeResult(cause.getJudgeResult());
                        // 获取负责人姓名
                        if (temp.getResponsibleId() != null) {
                            Approver approver = approverRepository.findById(temp.getResponsibleId()).orElseGet(Approver::new);
                            ValidationUtil.isNull(approver.getId(), "Approver", "id", temp.getResponsibleId());
                            temp.setResponsibleName(approver.getUsername());
                        }
                    });
                    for (Long id : planIdList) {
                        if (!causeIdList.contains(id)) {
                            // 获取差异原因数据
                            diffIdList.add(id);
                        }
                    }
                    // 初始化额外的原因数据
                    if (ValidationUtil.isNotEmpty(diffIdList) && !isImCorrectAct) {
                        for (Long difId : diffIdList) {
                            IssueCause cause = issueCauseRepository.findById(difId).orElseGet(IssueCause::new);
                            ValidationUtil.isNull(cause.getId(), "IssueCause", "id", difId);
                            IssueActionDto dto = new IssueActionDto();
                            dto.setCauseId(cause.getId());
                            dto.setCauseName(cause.getName());
                            dto.setJudgeResult(cause.getJudgeResult());
                            tempList.add(dto);
                        }
                    }
                    list.addAll(tempList);
                } else {
                    // D5无措施,则只显示根本原因列表，且前端无法编辑和删除；若是D6-则不处理，返回空数组
                    if (!isImCorrectAct) {
                        for (IssueCause cause : causeList) {
                            IssueActionDto actionDto = new IssueActionDto();
                            actionDto.setCauseId(cause.getId());
                            actionDto.setCauseName(cause.getName());
                            actionDto.setJudgeResult(cause.getJudgeResult());
                            list.add(actionDto);
                        }
                    }
                }
            }
        } else {
            list = issueActionMapper.toDto(issueActionRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), sort));
        }
        getResponsibleName(list);
        return list;
    }

    @Override
    public List<IssueActionDto> findCanRemoveByIssueId(Long issueId) {
        // 查询问题判null
        Issue issue = issueRepository.findById(issueId).orElseGet(Issue::new);
        ValidationUtil.isNull(issue.getId(), "Issue", "id", issueId);
        List<IssueActionDto> list = issueActionMapper.toDto(issueActionRepository.findCanRemoveByIssueId(issueId, CommonConstants.ACTION_REMOVED, CommonConstants.D_STEP_D5, false));
        getResponsibleName(list);
        return list;
    }

    private void getResponsibleName(List<IssueActionDto> list) {
        if (ValidationUtil.isNotEmpty(list)) {
            list.forEach(dto -> {
                // 获取负责人姓名
                if (dto.getResponsibleId() != null) {
                    Approver approver = approverRepository.findById(dto.getResponsibleId()).orElseGet(Approver::new);
                    ValidationUtil.isNull(approver.getId(), "Approver", "id", dto.getResponsibleId());
                    dto.setResponsibleName(approver.getUsername());
                }
            });
        }
    }

    @Override
    public IssueActionDto findById(Long id) {
        IssueActionDto dto;
        IssueAction action = issueActionRepository.findById(id).orElseGet(IssueAction::new);
        ValidationUtil.isNull(action.getId(), "IssueAction", "id", id);
        dto = issueActionMapper.toDto(action);
        Approver approver = approverRepository.findById(dto.getResponsibleId()).orElseGet(Approver::new);
        ValidationUtil.isNull(approver.getId(), "Approver", "id", dto.getResponsibleId());
        dto.setResponsibleName(approver.getUsername());
        return dto;
    }

    @Override
    public List<IssueActionDto> findActionByUserId(Long currentUserId) {
        List<IssueActionDto> issueActionDtoList = new ArrayList<>();
        List<IssueAction> actionList = issueActionRepository.findByResponsibleId(currentUserId);
        if (ValidationUtil.isNotEmpty(actionList)) {
            issueActionDtoList = issueActionMapper.toDto(actionList);
            issueActionDtoList.forEach(action -> {
                Issue issue = issueRepository.findById(action.getIssueId()).orElseGet(Issue::new);
                ValidationUtil.isNull(issue.getId(), "Issue", "id", action.getIssueId());
                action.setIssueTitle(issue.getIssueTitle());
                action.setHasReport(issue.getHasReport());
            });
        }
        return issueActionDtoList;
    }

    @Override
    public List<IssueActionDto> queryAll(ActionQueryCriteria criteria) {
        // todo 查询个人任务
        if (!SecurityUtils.getIsAdmin()) {
            criteria.setResponsibleId(SecurityUtils.getCurrentUserId());
        } else {
            if(criteria.getSelfFlag()){
                criteria.setResponsibleId(SecurityUtils.getCurrentUserId());
            }
        }
        List<IssueAction> issueList = issueActionRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
        List<IssueActionDto> list = new ArrayList<>();
        if (ValidationUtil.isNotEmpty(issueList)) {
            list = issueActionMapper.toDto(issueList);
            addMoreProperies(list);
        }
        return list;
    }

    private void addMoreProperies(List<IssueActionDto> list) {
        list.forEach(actionDto -> {
            // 获取问题标题和问题执行选择
            Issue issue = issueRepository.findById(actionDto.getIssueId()).orElseGet(Issue::new);
            ValidationUtil.isNull(issue.getId(), "Issue", "id", actionDto.getIssueId());
            actionDto.setIssueTitle(issue.getIssueTitle());
            actionDto.setHasReport(issue.getHasReport());
            // 获取执行人姓名
            Approver approver = approverRepository.findById(actionDto.getResponsibleId()).orElseGet(Approver::new);
            ValidationUtil.isNull(approver.getId(), "Approver", "id", actionDto.getResponsibleId());
            actionDto.setResponsibleName(approver.getUsername());
        });
    }

    @Override
    public Map<String, Object> queryAll(ActionQueryCriteria criteria, Pageable pageable) {
        if (!SecurityUtils.getIsAdmin()) {
            criteria.setResponsibleId(SecurityUtils.getCurrentUserId());
        } else {
            if(criteria.getSelfFlag()){
                criteria.setResponsibleId(SecurityUtils.getCurrentUserId());
            }
        }
        Page<IssueAction> page = issueActionRepository.findAll((root, query, cb) -> QueryHelp.getPredicate(root, criteria, cb), pageable);
        Map<String, Object> map = new HashMap<>();
        List<IssueActionDto> list = new ArrayList<>();
        long total = 0L;
        if (ValidationUtil.isNotEmpty(page.getContent())) {
            list = issueActionMapper.toDto(page.getContent());
            addMoreProperies(list);
            total = page.getTotalElements();
        }

        map.put("content", list);
        map.put("totalElements", total);
        return map;
    }

    @Override
    public void download(List<IssueActionDto> issueActionList, HttpServletResponse response) throws IOException {
        // 导出个人任务
        List<Map<String, Object>> list = new ArrayList<>();
        for (IssueActionDto dto : issueActionList) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("措施标题", dto.getName());
            map.put("问题标题", dto.getIssueTitle());
            map.put("状态", dto.getStatus());
            map.put("执行选择", dto.getHasReport());
            map.put("所属进程", dto.getType());
            map.put("负责人", dto.getResponsibleName());
            map.put("有效性", dto.getEfficiency());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }
}
