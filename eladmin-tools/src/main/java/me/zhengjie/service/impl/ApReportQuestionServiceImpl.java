package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.ApQuestionAction;
import me.zhengjie.domain.ApReportQuestion;
import me.zhengjie.domain.AuditPlan;
import me.zhengjie.domain.AuditPlanReport;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.repository.*;
import me.zhengjie.service.ApReportQuestionService;
import me.zhengjie.service.AuditPlanService;
import me.zhengjie.service.dto.ApQuestionQueryCriteria;
import me.zhengjie.service.dto.ApReportQuestionDto;
import me.zhengjie.service.mapstruct.ApReportQuestionMapper;
import me.zhengjie.utils.FileUtil;
import me.zhengjie.utils.QueryHelp;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/11/3 16:11
 */

@Service
@RequiredArgsConstructor
public class ApReportQuestionServiceImpl implements ApReportQuestionService {

    private final AuditPlanReportRepository reportRepository;
    private final ApReportQuestionRepository questionRepository;
    private final AuditPlanService auditPlanService;
    private final AuditPlanRepository planRepository;
    private final ApReportQuestionMapper apReportQuestionMapper;
    private final ApQuestionActionRepository actionRepository;
    private final ApQuestionFileRepository fileRepository;

    @Override
    public List<ApReportQuestionDto> findByReportId(Long reportId) {
        List<ApReportQuestionDto> list = new ArrayList<>();
        // 报告判空
        AuditPlanReport report = reportRepository.findById(reportId).orElseGet(AuditPlanReport::new);
        ValidationUtil.isNull(report.getId(), "AuditPlanReport", "id", reportId);
        // 计划判空
        AuditPlan plan = planRepository.findById(report.getPlanId()).orElseGet(AuditPlan::new);
        ValidationUtil.isNull(plan.getId(), "AuditPlan", "id", report.getPlanId());
        list = apReportQuestionMapper.toDto(questionRepository.findByReportId(reportId));
        if (ValidationUtil.isNotEmpty(list)) {
            list.forEach(dto -> {
                dto.setPlanName(plan.getRealName());
                List<ApQuestionAction> actions = actionRepository.findByQuesId(dto.getId());
                List<String> names = new ArrayList<>();
                if (ValidationUtil.isNotEmpty(actions)) {
                    actions.forEach(action -> {
                        names.add(action.getTitle());
                    });
                } else {
                    names.add("--");
                }
                dto.setActionNames(names);
            });

        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(ApReportQuestion resources) {
        Long reportId = resources.getPlanReportId();
        AuditPlanReport report = reportRepository.findById(reportId).orElseGet(AuditPlanReport::new);
        ValidationUtil.isNull(report.getId(), "AuditPlanReport", "id", reportId);
        // 判断是否有执行改计划的权限
        auditPlanService.checkHasAuthExecute(report.getPlanId());
        // 问题重名校验
        ApReportQuestion question = questionRepository.findByName(resources.getName());
        if (question != null) {
            throw new EntityExistException(ApReportQuestion.class, "name", resources.getName());
        }
        questionRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ApReportQuestion resources) {
        Long reportId = resources.getPlanReportId();
        AuditPlanReport report = reportRepository.findById(reportId).orElseGet(AuditPlanReport::new);
        ValidationUtil.isNull(report.getId(), "AuditPlanReport", "id", reportId);
        // 判断是否有执行改计划的权限
        auditPlanService.checkHasAuthExecute(report.getPlanId());
        // 重名校验
        // 问题重名校验
        ApReportQuestion question = questionRepository.findByName(resources.getName());
        if (question != null && !question.getId().equals(resources.getId())) {
            throw new EntityExistException(ApReportQuestion.class, "name", resources.getName());
        }
        questionRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        // 判断是否有执行改计划的权限
        for (Long id : ids) {
            Long planId = questionRepository.findPlanIdByQuesId(id);
            // 判断是否有执行改计划的权限
            auditPlanService.checkHasAuthExecute(planId);
        }
        questionRepository.deleteAllByIdIn(ids);
        // 删除对应的对策
        actionRepository.deleteAllByQuesIdIn(ids);
        // 删除对应的对策
        fileRepository.deleteAllByQuesIdIn(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completedById(Long id) {
        ApReportQuestion question = questionRepository.findById(id).orElseGet(ApReportQuestion::new);
        ValidationUtil.isNull(question.getId(), "ApReportQuestion", "id", id);
        question.setIsCompleted(true);
        questionRepository.save(question);
        // todo 是否是最后一个未完成的问题，若是则自动更新此次审核已结案
    }

    @Override
    public Map<String, Object> queryAll(ApQuestionQueryCriteria criteria, Pageable pageable) {
        Page<ApReportQuestion> page = questionRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        Map<String, Object> map = new HashMap<>();
        List<ApReportQuestionDto> list = new ArrayList<>();
        long total = 0L;
        if (ValidationUtil.isNotEmpty(page.getContent())) {
            list = apReportQuestionMapper.toDto(page.getContent());
            // 获取审核计划信息
            getPlanInfo(list);
            total = page.getTotalElements();
        }
        map.put("content", list);
        map.put("totalElements", total);
        return map;
    }

    @Override
    public List<ApReportQuestionDto> queryAll(ApQuestionQueryCriteria criteria) {
        List<ApReportQuestionDto> list = new ArrayList<>();
        List<ApReportQuestion> plans = questionRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
        if (ValidationUtil.isNotEmpty(plans)) {
            list = apReportQuestionMapper.toDto(plans);
            // 获取审核计划信息
            getPlanInfo(list);
        }
        return list;
    }

    private void getPlanInfo(List<ApReportQuestionDto> list) {
        list.forEach(ques -> {
            AuditPlan plan = planRepository.findByReportId(ques.getPlanReportId());
            if (plan != null) {
                ques.setPlanId(plan.getId());
                ques.setPlanName(plan.getRealName());
            }
        });
    }

    @Override
    public void download(List<ApReportQuestionDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (ApReportQuestionDto quesDto : queryAll) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("审核计划名称", quesDto.getPlanName());
            map.put("问题标题", quesDto.getName());
            map.put("是否重复", quesDto.getIsRepeat() ? "是" : "否");
            map.put("是否已完成", quesDto.getIsCompleted() ? "是" : "否");
            map.put("实施对策", quesDto.getActionNames());
            map.put("创建日期", quesDto.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }
}
