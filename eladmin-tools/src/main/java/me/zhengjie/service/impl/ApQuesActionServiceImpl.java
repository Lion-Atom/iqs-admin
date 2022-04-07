package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.ApQuestionAction;
import me.zhengjie.domain.ApReportQuestion;
import me.zhengjie.domain.AuditPlan;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.repository.ApQuestionActionRepository;
import me.zhengjie.repository.ApQuestionFileRepository;
import me.zhengjie.repository.ApReportQuestionRepository;
import me.zhengjie.repository.AuditPlanRepository;
import me.zhengjie.service.ApQuesActionService;
import me.zhengjie.service.AuditPlanService;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/11/5 9:39
 */
@Service
@RequiredArgsConstructor
public class ApQuesActionServiceImpl implements ApQuesActionService {

    private final ApReportQuestionRepository questionRepository;
    private final ApQuestionActionRepository actionRepository;
    private final AuditPlanRepository planRepository;
    private final AuditPlanService auditPlanService;
    private final ApQuestionFileRepository fileRepository;

    @Override
    public List<ApQuestionAction> findByPlanIdAndQuesId(Long planId, Long quesId) {

        List<ApQuestionAction> list = new ArrayList<>();

        // 查询审核计划判null
        AuditPlan plan = planRepository.findById(planId).orElseGet(AuditPlan::new);
        if (plan == null) {
            throw new BadRequestException("未查到该审核计划信息");
        }
        if (quesId == 0L) {
            list = actionRepository.findByPlanId(planId);
        } else {
            list = actionRepository.findByQuesId(quesId);
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> update(ApQuestionAction resources) {
        Map<String, Object> map = new HashMap<>();
        ApQuestionAction action = actionRepository.findById(resources.getId()).orElseGet(ApQuestionAction::new);
        ValidationUtil.isNull(action.getId(), "AuditPlan", "id", resources.getId());
        Long quesId = action.getReportQuestionId();
        ApReportQuestion question = questionRepository.findById(quesId).orElseGet(ApReportQuestion::new);
        // 判断是否具备修改权限
        if (resources.getPlanId().equals(action.getPlanId())) {
            auditPlanService.checkHasAuthExecute(action.getPlanId());
        } else {
            throw new BadRequestException("所传审核计划ID与库中审核计划ID不匹配！");
        }
        //重名校验
        ApQuestionAction old = actionRepository.findByTitle(resources.getTitle());
        if (old != null && !old.getId().equals(resources.getId())) {
            throw new EntityExistException(ApQuestionAction.class, "title", resources.getTitle());
        }
        actionRepository.save(resources);
        // 若是执行完成或者停用，则进入判断流程
        if (CommonConstants.ACTION_STATUS_LIST.contains(resources.getStatus())) {
            // 判断是否是最后一条未完成的,如果是则提示是否更改问题改善完成
            List<ApQuestionAction> actions = actionRepository.findByQuesIdButNotId(quesId, resources.getId());
            if (ValidationUtil.isNotEmpty(actions)) {
                List<String> list = new ArrayList<>();
                actions.forEach(act -> {
                    if (CommonConstants.ACTION_STATUS_LIST.contains(act.getStatus())) {
                        list.add(act.getStatus());
                    }
                });
                if (actions.size() == list.size()) {
                    // 如果数目相同，说明问题对应的改善措施都完成了
                    map.put("content", "问题是否确认改善完成？");
                }

            } else {
                map.put("content", "问题是否确认改善完成？");
            }
        } else {
            if (question.getIsCompleted()) {
                question.setIsCompleted(false);
                questionRepository.save(question);
            }
        }
        // 同步设置审核计划不可为结案状态
        AuditPlan plan = planRepository.findById(action.getPlanId()).orElseGet(AuditPlan::new);
        plan.setStatus(CommonConstants.AUDIT_PLAN_STATUS_TRACE);
        plan.setCloseTime(null);
        planRepository.save(plan);
        map.put("result", "更新成功！");
        return map;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(ApQuestionAction resources) {
        // 判断是否具备修改审核计划权限
        auditPlanService.checkHasAuthExecute(resources.getPlanId());
        //重名校验
        ApQuestionAction old = actionRepository.findByTitle(resources.getTitle());
        if (old != null) {
            throw new EntityExistException(ApQuestionAction.class, "title", resources.getTitle());
        }
        // 判断是否需要重置问题是否改善完成
        Long quesId = resources.getReportQuestionId();
        ApReportQuestion question = questionRepository.findById(quesId).orElseGet(ApReportQuestion::new);
        if (question.getIsCompleted()) {
            question.setIsCompleted(false);
            questionRepository.save(question);
            // 同步设置审核计划不可为结案状态
            AuditPlan plan = planRepository.findById(resources.getPlanId()).orElseGet(AuditPlan::new);
            plan.setStatus(CommonConstants.AUDIT_PLAN_STATUS_TRACE);
            plan.setCloseTime(null);
            planRepository.save(plan);
        }
        actionRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> delete(ApQuestionAction resources) {
        Map<String, Object> map = new HashMap<>();
        Long planId = resources.getPlanId();
        // 权限判断
        auditPlanService.checkHasAuthExecute(resources.getPlanId());
        // 查询问题判null
        AuditPlan plan = planRepository.findById(planId).orElseGet(AuditPlan::new);
        ValidationUtil.isNull(plan.getId(), "AuditPlan", "id", planId);
        actionRepository.deleteById(resources.getId());
        // 删除对策下附件信息
        fileRepository.deleteAllByActIdIn(resources.getId());
        Long quesId = resources.getReportQuestionId();
        ApReportQuestion question = questionRepository.findById(quesId).orElseGet(ApReportQuestion::new);
        // 判断是否是最后一条未完成的,如果是则提示是否更改问题改善完成
        List<ApQuestionAction> actions = actionRepository.findByQuesIdButNotId(quesId, resources.getId());
        if (ValidationUtil.isNotEmpty(actions)) {
            List<String> list = new ArrayList<>();
            actions.forEach(act -> {
                if (CommonConstants.ACTION_STATUS_LIST.contains(act.getStatus())) {
                    list.add(act.getStatus());
                }
            });
            if (actions.size() == list.size()) {
                // 如果数目相同，说明问题对应的改善措施都完成了
                map.put("content", "问题是否确认改善完成？");
            }
        } else {
            map.put("result", "问题需要添加有效的改善方案方可结案");
            if (question.getIsCompleted()) {
                question.setIsCompleted(false);
                questionRepository.save(question);
                // 同步设置审核计划不可为结案状态
                plan.setStatus(CommonConstants.AUDIT_PLAN_STATUS_TRACE);
                plan.setCloseTime(null);
                planRepository.save(plan);
            }
        }
        return map;
    }
}
