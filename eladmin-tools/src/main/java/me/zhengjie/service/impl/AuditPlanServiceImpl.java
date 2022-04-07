package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.base.CommonDTO;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.*;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.repository.*;
import me.zhengjie.service.AuditPlanService;
import me.zhengjie.service.dto.*;
import me.zhengjie.service.mapstruct.AuditPlanMapper;
import me.zhengjie.utils.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author tmj
 * @version 1.0
 * @date 2021/4/23 15:37
 */
@Service
@RequiredArgsConstructor
public class AuditPlanServiceImpl implements AuditPlanService {

    private final AuditPlanMapper auditPlanMapper;
    private final AuditPlanRepository auditPlanRepository;
    private final ApproverRepository approverRepository;
    private final PreTrailRepository preTrailRepository;
    private final AuditorRepository auditorRepository;
    private final CommonUtils commonUtils;
    private final PlanTemplateRepository planTemplateRepository;
    private final ApTempContentRepository contentRepository;
    private final PlanFileRepository planFileRepository;
    private final AuditPlanExecuteRepository executeRepository;
    private final AuditPlanReportRepository reportRepository;
    private final ApReportQuestionRepository questionRepository;
    private final TemplateScoreRepository scoreRepository;

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public Map<String, Object> queryAll(AuditPlanQueryCriteria criteria, Pageable pageable) {
        Page<AuditPlan> page = auditPlanRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        Map<String, Object> map = new HashMap<>();
        List<AuditPlanDto> list = new ArrayList<>();
        long total = 0L;
        if (ValidationUtil.isNotEmpty(page.getContent())) {
            list = auditPlanMapper.toDto(page.getContent());
            Map<Long, String> userMap = new HashMap<>();
            Set<Long> userIds = new HashSet<>();
            Map<Long, String> auditorMap = new HashMap<>();
            Map<Long, Long> auditorIdMap = new HashMap<>();
            Set<Long> oUserIds = new HashSet<>();
            Set<Long> auditorIds = new HashSet<>();
            Set<Long> planIds = new HashSet<>();
            Map<Long, Timestamp> deadLineMap = new HashMap<>();
            // 获取报告信息
            list.forEach(plan -> {
                AuditPlanReport report = reportRepository.findByPlanId(plan.getId());
                if (report != null) {
                    plan.setReport(report);
                }
                userIds.add(plan.getApprovedBy());
                userIds.add(plan.getChargeBy());
                if (!plan.getAuditors().isEmpty()) {
                    plan.getAuditors().forEach(aud -> {
                        auditorIds.add(aud.getId());
                    });
                }
                planIds.add(plan.getId());
            });
            List<Approver> approvers = approverRepository.findByIdIn(userIds);
            List<Auditor> auditors = auditorRepository.findByIdIn(auditorIds);
            List<AuditPlanReport> reports = reportRepository.findByPlanIdIn(planIds);
            if (ValidationUtil.isNotEmpty(approvers)) {
                approvers.forEach(a -> {
                    userMap.put(a.getId(), a.getUsername());
                });
            }
            if (ValidationUtil.isNotEmpty(auditors)) {
                auditors.forEach(a -> {
                    auditorIdMap.put(a.getId(), a.getUserId());
                    oUserIds.add(a.getUserId());
                });
            }
            if (ValidationUtil.isNotEmpty(reports)) {
                reports.forEach(a -> {
                    deadLineMap.put(a.getPlanId(), a.getFinalDeadline());
                });
            }
            List<Approver> oApprovers = approverRepository.findByIdIn(oUserIds);
            if (ValidationUtil.isNotEmpty(oApprovers)) {
                oApprovers.forEach(a -> {
                    auditorMap.put(a.getId(), a.getUsername());
                });
            }
            list.forEach(planDto -> {
                planDto.setApprover(userMap.get(planDto.getApprovedBy()));
                planDto.setChargeman(userMap.get(planDto.getChargeBy()));
                if (!planDto.getAuditors().isEmpty()) {
                    planDto.getAuditors().forEach(aud -> {
                        aud.setUsername(auditorMap.get(auditorIdMap.get(aud.getId())));
                    });
                }
                planDto.setFinalDeadline(deadLineMap.get(planDto.getId()));
            });
            // 获取计划审批人信息等-- 已使用批量查询代替for循环调用
//            getApproverName(list);
            total = page.getTotalElements();
        }
        map.put("content", list);
        map.put("totalElements", total);
        return map;
    }

    @Override
    public List<AuditPlanDto> queryAll(AuditPlanQueryCriteria criteria) {
        List<AuditPlanDto> list = new ArrayList<>();
        List<AuditPlan> plans = auditPlanRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
        if (ValidationUtil.isNotEmpty(plans)) {
            list = auditPlanMapper.toDto(plans);
            // 获取计划审批人信息等
            getApproverName(list);
        }
        return list;
    }

    private void getApproverName(List<AuditPlanDto> list) {
        list.forEach(plan -> {
            // 获取审核人员姓名
            if (plan.getApprovedBy() != null) {
                Approver approver = approverRepository.findById(plan.getApprovedBy()).orElseGet(Approver::new);
                ValidationUtil.isNull(approver.getId(), "Approver", "id", plan.getApprovedBy());
                plan.setApprover(approver.getUsername());
                // --- todo 审核计划修改审核人是否与审核人员一致待leader确认
            }
            // 获取审核计划负责人信息
            if (plan.getChargeBy() != null) {
                Approver charge = approverRepository.findById(plan.getChargeBy()).orElseGet(Approver::new);
                ValidationUtil.isNull(charge.getId(), "Approver", "id", plan.getChargeBy());
                plan.setChargeman(charge.getUsername());
            }
            // 获取审核员信息
            if (!plan.getAuditors().isEmpty()) {
                plan.getAuditors().forEach(auditorDto -> {
                    Auditor auditor = auditorRepository.findById(auditorDto.getId()).orElseGet(Auditor::new);
                    if (auditor != null) {
                        Approver people = approverRepository.findById(auditor.getUserId()).orElseGet(Approver::new);
                        auditorDto.setUsername(people.getUsername());
                    }
                });
            }
        });
    }

    @Override
    public AuditPlanDto findById(Long id) {
        AuditPlanDto dto = null;
        AuditPlan plan = auditPlanRepository.findById(id).orElseGet(AuditPlan::new);
        if (plan != null) {
            dto = auditPlanMapper.toDto(plan);
            // todo 视情况是否需要显示中文审核人员名称注释等
            // 获取审核人员姓名
            if (dto.getApprovedBy() != null) {
                Approver approver = approverRepository.findById(plan.getApprovedBy()).orElseGet(Approver::new);
                ValidationUtil.isNull(approver.getId(), "Approver", "id", plan.getApprovedBy());
                dto.setApprover(approver.getUsername());
                // --- todo 审核计划修改审核人是否与审核人员一致待leader确认
            }
            // 获取审核计划负责人信息
            if (dto.getChargeBy() != null) {
                Approver charge = approverRepository.findById(plan.getChargeBy()).orElseGet(Approver::new);
                ValidationUtil.isNull(charge.getId(), "Approver", "id", plan.getChargeBy());
                dto.setChargeman(charge.getUsername());
            }
            // 获取审核员信息
            if (!dto.getAuditors().isEmpty()) {
                dto.getAuditors().forEach(auditorDto -> {
                    Auditor auditor = auditorRepository.findById(auditorDto.getId()).orElseGet(Auditor::new);
                    if (auditor != null) {
                        Approver people = approverRepository.findById(auditor.getUserId()).orElseGet(Approver::new);
                        auditorDto.setUsername(people.getUsername());
                    }
                });
            }
        }
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(AuditPlan resources) {

        // 新增计划默认“计划”
        resources.setStatus(CommonConstants.AUDIT_PLAN_STATUS_TO);
        // 新增数据默认待审核
        resources.setApprovalStatus(CommonConstants.AUDIT_PLAN_STATUS_TO_ACTIVATED);
        // 添加审批人信息
        resources.setApprovedBy(commonUtils.getSuperiorId());
        //  生成审核计划名称和审核编号
        // 创建审核编号
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String num = "001";
        // 查询今日创建数目
        Integer count = auditPlanRepository.findTodayCountByCreateTime();
        if (9 > count) {
            num = "00" + (count + 1);
        } else if (count > 8 && 99 > count) {
            num = "0" + (count + 1);
        } else {
            num = String.valueOf((count + 1));
        }
        resources.setAuditNo(StringUtils.getPinyin(resources.getType()) + "-" + format.format(date) + "-" + num);
        resources.setRealName(resources.getContent() + format.format(date) + "-" + resources.getType());
        resources.setSystemName(resources.getType());

        //重名校验
        AuditPlan plan = auditPlanRepository.findByRealName(resources.getRealName());
        if (plan != null) {
            throw new EntityExistException(AuditPlan.class, "realName", resources.getRealName());
        }

        AuditPlan auditPlan = auditPlanRepository.save(resources);

        // 初始化审批计划模板
        if (resources.getTemplateId() == null) {
            PlanTemplate planTemplate = initPlanTemplate(resources, auditPlan);
            auditPlan.setTemplateId(planTemplate.getId());
        } else {
            // 如果创建时候就传入了模板标识，则可以断定是来自系统推荐模板
            if (CommonConstants.AUDIT_PLAN_TEMPLATE_LIST.contains(resources.getTemplateType())) {
                List<PlanTemplate> initList = planTemplateRepository.findByTempTypeAndDisEnabled(resources.getTemplateType(), false);
                if (ValidationUtil.isNotEmpty(initList)) {
                    // 复制一份模板出来
                    PlanTemplate template = new PlanTemplate();
                    template.setTemplateType(resources.getTemplateType());
                    template.setPlanId(auditPlan.getId());
                    template.setName(resources.getRealName() + "模板");
                    PlanTemplate newTemplate = planTemplateRepository.save(template);
                    ApTemplateContent content = contentRepository.findById(initList.get(0).getId()).orElseGet(ApTemplateContent::new);
                    ApTemplateContent cnt = new ApTemplateContent();
                    cnt.copy(content);
                    cnt.setProcessType(resources.getLine());
                    cnt.setId(newTemplate.getId());
                    contentRepository.save(cnt);
                    // 复制一份问题清单或者初始化一份问题清单
                    initTemplateScore(newTemplate.getId());
                    auditPlan.setTemplateId(newTemplate.getId());
                }
            }
        }
        auditPlanRepository.save(resources);

        //创建审批任务给质量部Master
        PreTrail preTrail = new PreTrail();
        preTrail.setPreTrailNo(createNoFormat(resources.getRealName()));
        preTrail.setStorageId(auditPlan.getId());
        preTrail.setSrcPath(CommonConstants.IS_BLANK);
        preTrail.setTarPath(CommonConstants.IS_BLANK);
        preTrail.setSuffix(CommonConstants.IS_BLANK);
        preTrail.setVersion(CommonConstants.IS_BLANK);
        preTrail.setSize(CommonConstants.IS_BLANK);
        preTrail.setType(CommonConstants.TRAIL_TYPE_AUDIT_PLAN);
        preTrail.setRealName(auditPlan.getRealName());
        preTrail.setChangeDesc("新建审核计划：[" + auditPlan.getRealName() + "]待审批");
        preTrail.setIsDel(CommonConstants.IS_DEL);
        // 指定审批人
        preTrail.setApprovedBy(commonUtils.getSuperiorId());
        preTrailRepository.save(preTrail);
    }

    private PlanTemplate initPlanTemplate(AuditPlan resources, AuditPlan auditPlan) {
        PlanTemplate template = new PlanTemplate();
        template.setPlanId(auditPlan.getId());
        template.setName(resources.getRealName() + "模板");
        template.setTemplateType(resources.getTemplateType());
        return planTemplateRepository.save(template);
    }

    private String createNoFormat(String name) {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        return StringUtils.getPinyin(name) + "-" + format.format(date);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(AuditPlan resources) {

        AuditPlan plan = auditPlanRepository.findById(resources.getId()).orElseGet(AuditPlan::new);
        //重名校验
        AuditPlan old = auditPlanRepository.findByRealName(resources.getRealName());
        if (old != null && !old.getId().equals(resources.getId())) {
            throw new EntityExistException(AuditPlan.class, "realName", resources.getRealName());
        }
        ValidationUtil.isNull(plan.getId(), "AuditPlan", "id", resources.getId());
        resources.setId(plan.getId());
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd hh:mm:ss");
        String pinyin = StringUtils.getPinyin(resources.getType());
        resources.setAuditNo(plan.getAuditNo().replace(StringUtils.getPinyin(plan.getType()), pinyin));
        resources.setRealName(resources.getContent() + format.format(plan.getCreateTime()) + "-" + resources.getType());
        // 修改需要审批
        if (!resources.getStatus().equals(CommonConstants.AUDIT_PLAN_STATUS_TO)) {
            throw new BadRequestException("No Access!计划已进入" + resources.getStatus() + "阶段，不可再修改！");
        }
        // 若是‘被驳回’则修改后自动改为待激活
        if (resources.getApprovalStatus().equals(CommonConstants.AUDIT_PLAN_STATUS_REFUSED) &&
                resources.getStatus().equals(CommonConstants.AUDIT_PLAN_STATUS_TO)) {
            resources.setApprovalStatus(CommonConstants.AUDIT_PLAN_STATUS_TO_ACTIVATED);
            //创建审批任务给质量部Master
            PreTrail preTrail = new PreTrail();
            preTrail.setPreTrailNo(createNoFormat(resources.getRealName()));
            preTrail.setStorageId(resources.getId());
            preTrail.setSrcPath(CommonConstants.IS_BLANK);
            preTrail.setTarPath(CommonConstants.IS_BLANK);
            preTrail.setSuffix(CommonConstants.IS_BLANK);
            preTrail.setVersion(CommonConstants.IS_BLANK);
            preTrail.setSize(CommonConstants.IS_BLANK);
            preTrail.setType(CommonConstants.TRAIL_TYPE_AUDIT_PLAN);
            preTrail.setRealName(resources.getRealName());
            preTrail.setChangeDesc("审核计划：[" + resources.getRealName() + "]被驳回后重新发起审批");
            preTrail.setIsDel(CommonConstants.IS_DEL);
            // 指定审批人
            preTrail.setApprovedBy(commonUtils.getSuperiorId());
            preTrailRepository.save(preTrail);
        }
        PlanTemplate template = planTemplateRepository.findByPlanId(plan.getId());
        if (template != null) {
            if (!resources.getRealName().equals(plan.getRealName())) {
                template.setName(resources.getRealName() + "模板");
            }
            if (!resources.getTemplateType().equals(plan.getTemplateType())) {
                // 如果自定义模板类型改了，是否删除内容和相关附件
                template.setTemplateType(resources.getTemplateType());
                if (CommonConstants.AUDIT_PLAN_TEMPLATE_LIST.contains(resources.getTemplateType())) {
                    List<PlanTemplate> initList = planTemplateRepository.findByTempTypeAndDisEnabled(resources.getTemplateType(), false);
                    if (ValidationUtil.isNotEmpty(initList)) {
                        // 复制一份模板出来
                        PlanTemplate template1 = new PlanTemplate();
                        template1.setTemplateType(resources.getTemplateType());
                        template1.setPlanId(plan.getId());
                        template1.setName(resources.getRealName() + "模板");
                        PlanTemplate newTemplate = planTemplateRepository.save(template1);
                        resources.setTemplateId(newTemplate.getId());
                        ApTemplateContent content = contentRepository.findById(initList.get(0).getId()).orElseGet(ApTemplateContent::new);
                        ApTemplateContent content1 = new ApTemplateContent();
                        content1.copy(content);
                        content1.setId(newTemplate.getId());
                        content1.setProcessType(resources.getLine());
                        contentRepository.save(content1);
                        // 复制一份问题清单或者初始化一份问题清单
                        initTemplateScore(newTemplate.getId());
                        // 删除原有的模板数据
                        planTemplateRepository.deleteById(template.getId());
                    }
                } else if (CommonConstants.AUDIT_PLAN_TEMPLATE_LIST.contains(plan.getTemplateType())) {
                    // 若之前使用的系统推荐模板，此处改变后需要删除旧类型模板数据
                    scoreRepository.deleteByTemplateId(plan.getTemplateId());
                    contentRepository.deleteByTemplateId(plan.getTemplateId());
                    resources.setTemplateId(template.getId());
                }
            } else {
                planTemplateRepository.save(template);
                resources.setTemplateId(template.getId());
            }
        } else {
            // 初始化审批计划模板,原则上是不会为null的，但防止旧数据污染
            PlanTemplate planTemplate = initPlanTemplate(resources, plan);
            resources.setTemplateId(planTemplate.getId());
        }
        auditPlanRepository.save(resources);
    }

    private void initTemplateScore(Long tempId) {
        // 初始化VDA6.3模板问题清单
        List<TemplateScore> scores2 = new ArrayList<>();
        List<TemplateScore> scores5 = new ArrayList<>();
        List<TemplateScore> scores7 = new ArrayList<>();

        // P2-项目管理
        TemplateScore sec = new TemplateScore();
        sec.setTemplateId(tempId);
        sec.setItemName(CommonConstants.TEMPLATE_QUES_P2);
        sec.setItemType(CommonConstants.TEMPLATE_QUES_P2);
        sec.setContent("项目管理");
        sec.setIsSpecial(false);
        sec.setIsNeed(false);
        TemplateScore rSec = scoreRepository.save(sec);

        // P2.1
        TemplateScore sec1 = new TemplateScore();
        sec1.setTemplateId(tempId);
        sec1.setItemName("2.1");
        sec1.setItemType(CommonConstants.TEMPLATE_QUES_P2);
        sec1.setContent("是否建立项目管理及项目组织机构？");
        sec1.setIsSpecial(false);
        sec1.setPid(rSec.getId());
        scores2.add(sec1);

        // P2.2
        TemplateScore sec2 = new TemplateScore();
        sec2.setTemplateId(tempId);
        sec2.setItemName("2.2");
        sec2.setItemType(CommonConstants.TEMPLATE_QUES_P2);
        sec2.setContent("是否为落实项目而规划了所有必要的资源，这些资源是否已经到位，并且体现了变更情况？");
        sec2.setIsSpecial(false);
        sec2.setPid(rSec.getId());
        scores2.add(sec2);

        // P2.3
        TemplateScore sec3 = new TemplateScore();
        sec3.setTemplateId(tempId);
        sec3.setItemName("2.3");
        sec3.setItemType(CommonConstants.TEMPLATE_QUES_P2);
        sec3.setContent("是否编制项目计划，并与顾客协调一致？");
        sec3.setIsSpecial(false);
        sec3.setPid(rSec.getId());
        scores2.add(sec3);

        // P2.4
        TemplateScore sec4 = new TemplateScore();
        sec4.setTemplateId(tempId);
        sec4.setItemName("2.4");
        sec4.setItemType(CommonConstants.TEMPLATE_QUES_P2);
        sec4.setContent("是否进行项目质量策划，并对其符合性进行监控？");
        sec4.setIsSpecial(false);
        sec4.setPid(rSec.getId());
        scores2.add(sec4);

        // P2.5
        TemplateScore sec5 = new TemplateScore();
        sec5.setTemplateId(tempId);
        sec5.setItemName("2.5");
        sec5.setItemType(CommonConstants.TEMPLATE_QUES_P2);
        sec5.setContent("项目所涉及的采购事项是否得以实施，并对其符合性加以监控？");
        sec5.setIsSpecial(true);
        sec5.setPid(rSec.getId());
        scores2.add(sec5);

        // P2.6
        TemplateScore sec6 = new TemplateScore();
        sec6.setTemplateId(tempId);
        sec6.setItemName("2.6");
        sec6.setItemType(CommonConstants.TEMPLATE_QUES_P2);
        sec6.setContent("项目组织机构是否可以在项目进行过程中提供可靠的变更管理？");
        sec6.setIsSpecial(true);
        sec6.setPid(rSec.getId());
        scores2.add(sec6);

        // P2.7
        TemplateScore sec7 = new TemplateScore();
        sec7.setTemplateId(tempId);
        sec7.setItemName("2.7");
        sec7.setItemType(CommonConstants.TEMPLATE_QUES_P2);
        sec7.setContent("是否建立事态升级程序，该程序是否得到有效执行？");
        sec7.setIsSpecial(false);
        sec7.setPid(rSec.getId());
        scores2.add(sec7);
        scoreRepository.saveAll(scores2);

        // P3-产品和过程开发的策划
        TemplateScore third = new TemplateScore();
        third.setTemplateId(tempId);
        third.setItemName(CommonConstants.TEMPLATE_QUES_P3);
        third.setItemType(CommonConstants.TEMPLATE_QUES_P3);
        third.setContent("产品和过程开发的策划");
        third.setIsSpecial(false);
        third.setIsNeed(false);
        TemplateScore rThird = scoreRepository.save(third);

        // P3下产品
        TemplateScore third_1 = new TemplateScore();
        third_1.setTemplateId(tempId);
        third_1.setItemName(null);
        third_1.setItemType(CommonConstants.TEMPLATE_QUES_P3);
        third_1.setContent("产品");
        third_1.setIsSpecial(false);
        third_1.setIsNeed(false);
        third_1.setPid(rThird.getId());
        scoreRepository.save(third_1);

        // P3下过程
        TemplateScore third_2 = new TemplateScore();
        third_2.setTemplateId(tempId);
        third_2.setItemName(null);
        third_2.setItemType(CommonConstants.TEMPLATE_QUES_P3);
        third_2.setContent("过程");
        third_2.setIsSpecial(false);
        third_2.setIsNeed(false);
        third_2.setPid(rThird.getId());
        scoreRepository.save(third_2);

        // P3.1
        TemplateScore third1 = new TemplateScore();
        third1.setTemplateId(tempId);
        third1.setItemName("3.1");
        third1.setItemType(CommonConstants.TEMPLATE_QUES_P3);
        third1.setContent("针对产品和过程的具体要求是否已明确？");
        third1.setIsSpecial(false);
        third1.setIsNeed(false);
        third1.setPid(rThird.getId());
        TemplateScore pThird1 = scoreRepository.save(third1);

        // P3.1下产品
        TemplateScore third1_1 = new TemplateScore();
        third1_1.setTemplateId(tempId);
        third1_1.setItemName(null);
        third1_1.setItemType(CommonConstants.TEMPLATE_QUES_P3);
        third1_1.setContent("产品");
        third1_1.setIsSpecial(false);
        third1_1.setPid(pThird1.getId());
        scoreRepository.save(third1_1);

        // P3.1下过程
        TemplateScore third1_2 = new TemplateScore();
        third1_2.setTemplateId(tempId);
        third1_2.setItemName(null);
        third1_2.setItemType(CommonConstants.TEMPLATE_QUES_P3);
        third1_2.setContent("过程");
        third1_2.setIsSpecial(false);
        third1_2.setPid(pThird1.getId());
        scoreRepository.save(third1_2);

        // P3.2
        TemplateScore third2 = new TemplateScore();
        third2.setTemplateId(tempId);
        third2.setItemName("3.2");
        third2.setItemType(CommonConstants.TEMPLATE_QUES_P3);
        third2.setContent("在产品和过程要求已明确的基础上，是否对可行性进行跨职能分析？");
        third2.setIsSpecial(true);
        third2.setIsNeed(false);
        third2.setPid(rThird.getId());
        TemplateScore pThird2 = scoreRepository.save(third2);

        // P3.2下产品
        TemplateScore third2_1 = new TemplateScore();
        third2_1.setTemplateId(tempId);
        third2_1.setItemName(null);
        third2_1.setItemType(CommonConstants.TEMPLATE_QUES_P3);
        third2_1.setContent("产品");
        third2_1.setIsSpecial(true);
        third2_1.setPid(pThird2.getId());
        scoreRepository.save(third2_1);

        // P3.2下过程
        TemplateScore third2_2 = new TemplateScore();
        third2_2.setTemplateId(tempId);
        third2_2.setItemName(null);
        third2_2.setItemType(CommonConstants.TEMPLATE_QUES_P3);
        third2_2.setContent("过程");
        third2_2.setIsSpecial(true);
        third2_2.setPid(pThird2.getId());
        scoreRepository.save(third2_2);

        // P3.3
        TemplateScore third3 = new TemplateScore();
        third3.setTemplateId(tempId);
        third3.setItemName("3.3");
        third3.setItemType(CommonConstants.TEMPLATE_QUES_P3);
        third3.setContent("是否具有产品和过程开发事项的详细计划？");
        third3.setIsSpecial(false);
        third3.setIsNeed(false);
        third3.setPid(rThird.getId());
        TemplateScore pThird3 = scoreRepository.save(third3);

        // P3.3下产品
        TemplateScore third3_1 = new TemplateScore();
        third3_1.setTemplateId(tempId);
        third3_1.setItemName(null);
        third3_1.setItemType(CommonConstants.TEMPLATE_QUES_P3);
        third3_1.setContent("产品");
        third3_1.setIsSpecial(false);
        third3_1.setPid(pThird3.getId());
        scoreRepository.save(third3_1);

        // P3.3下过程
        TemplateScore third3_2 = new TemplateScore();
        third3_2.setTemplateId(tempId);
        third3_2.setItemName(null);
        third3_2.setItemType(CommonConstants.TEMPLATE_QUES_P3);
        third3_2.setContent("过程");
        third3_2.setIsSpecial(false);
        third3_2.setPid(pThird3.getId());
        scoreRepository.save(third3_2);

        // P3.4
        TemplateScore third4 = new TemplateScore();
        third4.setTemplateId(tempId);
        third4.setItemName("3.4");
        third4.setItemType(CommonConstants.TEMPLATE_QUES_P3);
        third4.setContent("顾客关怀/顾客满意/顾客服务以及现场失效分析方面的事项是否具有相应计划？");
        third4.setIsSpecial(false);
        third4.setIsNeed(false);
        third4.setPid(rThird.getId());
        TemplateScore pThird4 = scoreRepository.save(third4);

        // P3.4下产品
        TemplateScore third4_1 = new TemplateScore();
        third4_1.setTemplateId(tempId);
        third4_1.setItemName(null);
        third4_1.setItemType(CommonConstants.TEMPLATE_QUES_P3);
        third4_1.setContent("产品");
        third4_1.setIsSpecial(false);
        third4_1.setPid(pThird4.getId());
        scoreRepository.save(third4_1);

        // P3.4下过程
        TemplateScore third4_2 = new TemplateScore();
        third4_2.setTemplateId(tempId);
        third4_2.setItemName(null);
        third4_2.setItemType(CommonConstants.TEMPLATE_QUES_P3);
        third4_2.setContent("过程");
        third4_2.setIsSpecial(false);
        third4_2.setPid(pThird4.getId());
        scoreRepository.save(third4_2);

        // P3.5
        TemplateScore third5 = new TemplateScore();
        third5.setTemplateId(tempId);
        third5.setItemName("3.5");
        third5.setItemType(CommonConstants.TEMPLATE_QUES_P3);
        third5.setContent("是否对产品和过程开发所需的资源进行策划？");
        third5.setIsSpecial(false);
        third5.setIsNeed(false);
        third5.setPid(rThird.getId());
        TemplateScore pThird5 = scoreRepository.save(third5);

        // P3.5下产品
        TemplateScore third5_1 = new TemplateScore();
        third5_1.setTemplateId(tempId);
        third5_1.setItemName(null);
        third5_1.setItemType(CommonConstants.TEMPLATE_QUES_P3);
        third5_1.setContent("产品");
        third5_1.setIsSpecial(false);
        third5_1.setPid(pThird5.getId());
        scoreRepository.save(third5_1);

        // P3.5下过程
        TemplateScore third5_2 = new TemplateScore();
        third5_2.setTemplateId(tempId);
        third5_2.setItemName(null);
        third5_2.setItemType(CommonConstants.TEMPLATE_QUES_P3);
        third5_2.setContent("过程");
        third5_2.setIsSpecial(false);
        third5_2.setPid(pThird5.getId());
        scoreRepository.save(third5_2);

        // P4-产品和过程开发的实现
        TemplateScore forth = new TemplateScore();
        forth.setTemplateId(tempId);
        forth.setItemName(CommonConstants.TEMPLATE_QUES_P4);
        forth.setItemType(CommonConstants.TEMPLATE_QUES_P4);
        forth.setContent("项目管理");
        forth.setIsSpecial(false);
        forth.setIsNeed(false);
        TemplateScore rForth = scoreRepository.save(forth);

        // P4下产品
        TemplateScore forth_1 = new TemplateScore();
        forth_1.setTemplateId(tempId);
        forth_1.setItemName(null);
        forth_1.setItemType(CommonConstants.TEMPLATE_QUES_P4);
        forth_1.setContent("产品");
        forth_1.setIsSpecial(false);
        forth_1.setIsNeed(false);
        forth_1.setPid(rForth.getId());
        scoreRepository.save(forth_1);

        // P4下过程
        TemplateScore forth_2 = new TemplateScore();
        forth_2.setTemplateId(tempId);
        forth_2.setItemName(null);
        forth_2.setItemType(CommonConstants.TEMPLATE_QUES_P4);
        forth_2.setContent("过程");
        forth_2.setIsSpecial(false);
        forth_2.setIsNeed(false);
        forth_2.setPid(rForth.getId());
        scoreRepository.save(forth_2);

        // P4.1
        TemplateScore forth1 = new TemplateScore();
        forth1.setTemplateId(tempId);
        forth1.setItemName("4.1");
        forth1.setItemType(CommonConstants.TEMPLATE_QUES_P4);
        forth1.setContent("产品和过程开发计划中确定的事项是否得到落实？");
        forth1.setIsSpecial(true);
        forth1.setIsNeed(false);
        forth1.setPid(rForth.getId());
        TemplateScore pForth1 = scoreRepository.save(forth1);

        // P4.1下产品
        TemplateScore forth1_1 = new TemplateScore();
        forth1_1.setTemplateId(tempId);
        forth1_1.setItemName(null);
        forth1_1.setItemType(CommonConstants.TEMPLATE_QUES_P4);
        forth1_1.setContent("产品");
        forth1_1.setIsSpecial(true);
        forth1_1.setPid(pForth1.getId());
        scoreRepository.save(forth1_1);

        // P4.1下过程
        TemplateScore forth1_2 = new TemplateScore();
        forth1_2.setTemplateId(tempId);
        forth1_2.setItemName(null);
        forth1_2.setItemType(CommonConstants.TEMPLATE_QUES_P4);
        forth1_2.setContent("过程");
        forth1_2.setIsSpecial(true);
        forth1_2.setPid(pForth1.getId());
        scoreRepository.save(forth1_2);

        // P4.2
        TemplateScore forth2 = new TemplateScore();
        forth2.setTemplateId(tempId);
        forth2.setItemName("4.2");
        forth2.setItemType(CommonConstants.TEMPLATE_QUES_P4);
        forth2.setContent("人力资源是否到位并且具备资质，以确保量产启动？");
        forth2.setIsSpecial(false);
        forth2.setIsNeed(false);
        forth2.setPid(rForth.getId());
        TemplateScore pForth2 = scoreRepository.save(forth2);

        // P4.2下产品
        TemplateScore forth2_1 = new TemplateScore();
        forth2_1.setTemplateId(tempId);
        forth2_1.setItemName(null);
        forth2_1.setItemType(CommonConstants.TEMPLATE_QUES_P4);
        forth2_1.setContent("产品");
        forth2_1.setIsSpecial(false);
        forth2_1.setPid(pForth2.getId());
        scoreRepository.save(forth2_1);

        // P4.2下过程
        TemplateScore forth2_2 = new TemplateScore();
        forth2_2.setTemplateId(tempId);
        forth2_2.setItemName(null);
        forth2_2.setItemType(CommonConstants.TEMPLATE_QUES_P4);
        forth2_2.setContent("过程");
        forth2_2.setIsSpecial(false);
        forth2_2.setPid(pForth2.getId());
        scoreRepository.save(forth2_2);

        // P4.3
        TemplateScore forth3 = new TemplateScore();
        forth3.setTemplateId(tempId);
        forth3.setItemName("4.3");
        forth3.setItemType(CommonConstants.TEMPLATE_QUES_P4);
        forth3.setContent("物质资源是否到位并且适用，以确保量产启动？");
        forth3.setIsSpecial(false);
        forth3.setIsNeed(false);
        forth3.setPid(rForth.getId());
        TemplateScore pForth3 = scoreRepository.save(forth3);

        // P4.3下产品
        TemplateScore forth3_1 = new TemplateScore();
        forth3_1.setTemplateId(tempId);
        forth3_1.setItemName(null);
        forth3_1.setItemType(CommonConstants.TEMPLATE_QUES_P4);
        forth3_1.setContent("产品");
        forth3_1.setIsSpecial(false);
        forth3_1.setPid(pForth3.getId());
        scoreRepository.save(forth3_1);

        // P4.3下过程
        TemplateScore forth3_2 = new TemplateScore();
        forth3_2.setTemplateId(tempId);
        forth3_2.setItemName(null);
        forth3_2.setItemType(CommonConstants.TEMPLATE_QUES_P4);
        forth3_2.setContent("过程");
        forth3_2.setIsSpecial(false);
        forth3_2.setPid(pForth3.getId());
        scoreRepository.save(forth3_2);

        // P4.4
        TemplateScore forth4 = new TemplateScore();
        forth4.setTemplateId(tempId);
        forth4.setItemName("4.4");
        forth4.setItemType(CommonConstants.TEMPLATE_QUES_P4);
        forth4.setContent("产品和过程开发是否具有所要求的认可批准？");
        forth4.setIsSpecial(true);
        forth4.setIsNeed(false);
        forth4.setPid(rForth.getId());
        TemplateScore pForth4 = scoreRepository.save(forth4);

        // P4.4下产品
        TemplateScore forth4_1 = new TemplateScore();
        forth4_1.setTemplateId(tempId);
        forth4_1.setItemName(null);
        forth4_1.setItemType(CommonConstants.TEMPLATE_QUES_P4);
        forth4_1.setContent("产品");
        forth4_1.setIsSpecial(true);
        forth4_1.setPid(pForth4.getId());
        scoreRepository.save(forth4_1);

        // P4.4下过程
        TemplateScore forth4_2 = new TemplateScore();
        forth4_2.setTemplateId(tempId);
        forth4_2.setItemName(null);
        forth4_2.setItemType(CommonConstants.TEMPLATE_QUES_P4);
        forth4_2.setContent("过程");
        forth4_2.setIsSpecial(true);
        forth4_2.setPid(pForth4.getId());
        scoreRepository.save(forth4_2);

        // P4.5
        TemplateScore forth5 = new TemplateScore();
        forth5.setTemplateId(tempId);
        forth5.setItemName("4.5");
        forth5.setItemType(CommonConstants.TEMPLATE_QUES_P4);
        forth5.setContent("是否基于产品和过程开发制定生产和检验规范并加以实施？");
        forth5.setIsSpecial(false);
        forth5.setIsNeed(false);
        forth5.setPid(rForth.getId());
        TemplateScore pForth5 = scoreRepository.save(forth5);

        // P4.5下产品
        TemplateScore forth5_1 = new TemplateScore();
        forth5_1.setTemplateId(tempId);
        forth5_1.setItemName(null);
        forth5_1.setItemType(CommonConstants.TEMPLATE_QUES_P4);
        forth5_1.setContent("产品");
        forth5_1.setIsSpecial(false);
        forth5_1.setPid(pForth5.getId());
        scoreRepository.save(forth5_1);

        // P4.5下过程
        TemplateScore forth5_2 = new TemplateScore();
        forth5_2.setTemplateId(tempId);
        forth5_2.setItemName(null);
        forth5_2.setItemType(CommonConstants.TEMPLATE_QUES_P4);
        forth5_2.setContent("过程");
        forth5_2.setIsSpecial(false);
        forth5_2.setPid(pForth5.getId());
        scoreRepository.save(forth5_2);

        // P4.6
        TemplateScore forth6 = new TemplateScore();
        forth6.setTemplateId(tempId);
        forth6.setItemName("4.6");
        forth6.setItemType(CommonConstants.TEMPLATE_QUES_P4);
        forth6.setContent("是否在量产条件下开展了效能测试，以便获得批量生产批准/放行？");
        forth6.setIsSpecial(false);
        forth6.setIsNeed(false);
        forth6.setPid(rForth.getId());
        TemplateScore pForth6 = scoreRepository.save(forth6);

        // P4.6.1
        TemplateScore forth6_1 = new TemplateScore();
        forth6_1.setTemplateId(tempId);
        forth6_1.setItemName(null);
        forth6_1.setItemType(CommonConstants.TEMPLATE_QUES_P4);
        forth6_1.setContent("过程");
        forth6_1.setIsSpecial(false);
        forth6_1.setPid(pForth6.getId());
        scoreRepository.save(forth6_1);

        // P4.7
        TemplateScore forth7 = new TemplateScore();
        forth7.setTemplateId(tempId);
        forth7.setItemName("4.7");
        forth7.setItemType(CommonConstants.TEMPLATE_QUES_P4);
        forth7.setContent("是否建立流程以便确保顾客关怀/顾客满意/顾客服务以及现场失效分析的实施？");
        forth7.setIsSpecial(false);
        forth7.setIsNeed(false);
        forth7.setPid(rForth.getId());
        TemplateScore pForth7 = scoreRepository.save(forth7);

        // P4.7.1
        TemplateScore forth7_1 = new TemplateScore();
        forth7_1.setTemplateId(tempId);
        forth7_1.setItemName(null);
        forth7_1.setItemType(CommonConstants.TEMPLATE_QUES_P4);
        forth7_1.setContent("过程");
        forth7_1.setIsSpecial(false);
        forth7_1.setPid(pForth7.getId());
        scoreRepository.save(forth7_1);

        // P4.8
        TemplateScore forth8 = new TemplateScore();
        forth8.setTemplateId(tempId);
        forth8.setItemName("4.8");
        forth8.setItemType(CommonConstants.TEMPLATE_QUES_P4);
        forth8.setContent("是否对项目从开发移交至批量生产开展了控制管理？");
        forth8.setIsSpecial(true);
        forth8.setIsNeed(false);
        forth8.setPid(rForth.getId());
        TemplateScore pForth8 = scoreRepository.save(forth8);

        // P4.8下产品
        TemplateScore forth8_1 = new TemplateScore();
        forth8_1.setTemplateId(tempId);
        forth8_1.setItemName(null);
        forth8_1.setItemType(CommonConstants.TEMPLATE_QUES_P4);
        forth8_1.setContent("产品");
        forth8_1.setIsSpecial(true);
        forth8_1.setPid(pForth8.getId());
        scoreRepository.save(forth8_1);

        // P4.8下过程
        TemplateScore forth8_2 = new TemplateScore();
        forth8_2.setTemplateId(tempId);
        forth8_2.setItemName(null);
        forth8_2.setItemType(CommonConstants.TEMPLATE_QUES_P4);
        forth8_2.setContent("过程");
        forth8_2.setIsSpecial(true);
        forth8_2.setPid(pForth8.getId());
        scoreRepository.save(forth8_2);

        // P5-供应商管理
        TemplateScore fifth = new TemplateScore();
        fifth.setTemplateId(tempId);
        fifth.setItemName(CommonConstants.TEMPLATE_QUES_P5);
        fifth.setItemType(CommonConstants.TEMPLATE_QUES_P5);
        fifth.setContent("供应商管理");
        fifth.setIsSpecial(false);
        fifth.setIsNeed(false);
        TemplateScore rFifth = scoreRepository.save(fifth);

        // P5.1
        TemplateScore fifth1 = new TemplateScore();
        fifth1.setTemplateId(tempId);
        fifth1.setItemName("5.1");
        fifth1.setItemType(CommonConstants.TEMPLATE_QUES_P5);
        fifth1.setContent("是否只和获得批准/放行且具备质量能力的供方开展合作？");
        fifth1.setIsSpecial(false);
        fifth1.setPid(rFifth.getId());
        scores5.add(fifth1);

        // P5.2
        TemplateScore fifth2 = new TemplateScore();
        fifth2.setTemplateId(tempId);
        fifth2.setItemName("5.2");
        fifth2.setItemType(CommonConstants.TEMPLATE_QUES_P5);
        fifth2.setContent("在供应链中是否考虑到顾客要求？");
        fifth2.setIsSpecial(false);
        fifth2.setPid(rFifth.getId());
        scores5.add(fifth2);

        // P5.3
        TemplateScore fifth3 = new TemplateScore();
        fifth3.setTemplateId(tempId);
        fifth3.setItemName("5.3");
        fifth3.setItemType(CommonConstants.TEMPLATE_QUES_P5);
        fifth3.setContent("是否与供方就供货绩效约定目标，并且加以落实？");
        fifth3.setIsSpecial(false);
        fifth3.setPid(rFifth.getId());
        scores5.add(fifth3);

        // P5.4
        TemplateScore fifth4 = new TemplateScore();
        fifth4.setTemplateId(tempId);
        fifth4.setItemName("5.4");
        fifth4.setItemType(CommonConstants.TEMPLATE_QUES_P5);
        fifth4.setContent("针对采购的产品和服务，是否获得了必要的批准/放行？");
        fifth4.setIsSpecial(true);
        fifth4.setPid(rFifth.getId());
        scores5.add(fifth4);

        // P5.5
        TemplateScore fifth5 = new TemplateScore();
        fifth5.setTemplateId(tempId);
        fifth5.setItemName("5.5");
        fifth5.setItemType(CommonConstants.TEMPLATE_QUES_P5);
        fifth5.setContent("针对采购的产品和服务，约定的质量是否得到保障？");
        fifth5.setIsSpecial(true);
        fifth5.setPid(rFifth.getId());
        scores5.add(fifth5);

        // P5.6
        TemplateScore fifth6 = new TemplateScore();
        fifth6.setTemplateId(tempId);
        fifth6.setItemName("5.6");
        fifth6.setItemType(CommonConstants.TEMPLATE_QUES_P5);
        fifth6.setContent("是否对进厂的货物进行适当的搬运和储存？");
        fifth6.setIsSpecial(false);
        fifth6.setPid(rFifth.getId());
        scores5.add(fifth6);

        // P5.7
        TemplateScore fifth7 = new TemplateScore();
        fifth7.setTemplateId(tempId);
        fifth7.setItemName("5.7");
        fifth7.setItemType(CommonConstants.TEMPLATE_QUES_P5);
        fifth7.setContent("人员资质是否能够满足不同的任务，并定义了其职责？");
        fifth7.setIsSpecial(false);
        fifth7.setPid(rFifth.getId());
        scores5.add(fifth7);
        scoreRepository.saveAll(scores5);

        // P6- 特殊处理
        TemplateScore sixth = new TemplateScore();
        sixth.setTemplateId(tempId);
        sixth.setItemName(CommonConstants.TEMPLATE_QUES_P6);
        sixth.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth.setContent("工艺分析/生产");
        sixth.setIsSpecial(false);
        sixth.setIsNeed(false);
        TemplateScore rSixth = scoreRepository.save(sixth);

        // P6.1 过程输入
        TemplateScore sixth1 = new TemplateScore();
        sixth1.setTemplateId(tempId);
        sixth1.setItemName("6.1");
        sixth1.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth1.setContent("过程输入是什么？（过程输入）");
        sixth1.setIsSpecial(false);
        sixth1.setIsNeed(false);
        sixth1.setPid(rSixth.getId());
        TemplateScore pSixth1 = scoreRepository.save(sixth1);

        TemplateScore sixth1_1 = new TemplateScore();
        sixth1_1.setTemplateId(tempId);
        sixth1_1.setItemName("6.1.1");
        sixth1_1.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth1_1.setContent("是否从开发向批量生产进行了项目移交，并确保了可靠的量产启动？");
        sixth1_1.setIsSpecial(false);
        sixth1_1.setPid(pSixth1.getId());
        scoreRepository.save(sixth1_1);

        TemplateScore sixth1_2 = new TemplateScore();
        sixth1_2.setTemplateId(tempId);
        sixth1_2.setItemName("6.1.2");
        sixth1_2.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth1_2.setContent("来料是否在约定的时间按所需数量/生产批次大小被送至正确的地点/工位？");
        sixth1_2.setIsSpecial(false);
        sixth1_2.setPid(pSixth1.getId());
        scoreRepository.save(sixth1_2);

        TemplateScore sixth1_3 = new TemplateScore();
        sixth1_3.setTemplateId(tempId);
        sixth1_3.setItemName("6.1.3");
        sixth1_3.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth1_3.setContent("是否对来料进行适当的仓储，所使用的运输工具/包装设备是否适合来料的特殊特性？");
        sixth1_3.setIsSpecial(false);
        sixth1_3.setPid(pSixth1.getId());
        scoreRepository.save(sixth1_3);

        TemplateScore sixth1_4 = new TemplateScore();
        sixth1_4.setTemplateId(tempId);
        sixth1_4.setItemName("6.1.4");
        sixth1_4.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth1_4.setContent("必要的标识/记录/放行是否具备，并且适当地体现在来料上？");
        sixth1_4.setIsSpecial(false);
        sixth1_4.setPid(pSixth1.getId());
        scoreRepository.save(sixth1_4);

        TemplateScore sixth1_5 = new TemplateScore();
        sixth1_5.setTemplateId(tempId);
        sixth1_5.setItemName("6.1.5");
        sixth1_5.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth1_5.setContent("在量产过程中，是否对产品或过程的变更开展跟踪和记录？");
        sixth1_5.setIsSpecial(true);
        sixth1_5.setPid(pSixth1.getId());
        scoreRepository.save(sixth1_5);

        // P6.2 工艺流程
        TemplateScore sixth2 = new TemplateScore();
        sixth2.setTemplateId(tempId);
        sixth2.setItemName("6.2");
        sixth2.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth2.setContent("所有生产过程是否受控？（工艺流程）");
        sixth2.setIsSpecial(false);
        sixth2.setIsNeed(false);
        sixth2.setPid(rSixth.getId());
        TemplateScore pSixth2 = scoreRepository.save(sixth2);

        TemplateScore sixth2_1 = new TemplateScore();
        sixth2_1.setTemplateId(tempId);
        sixth2_1.setItemName("6.2.1");
        sixth2_1.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth2_1.setContent("控制计划里的要求是否完整，并且得到有效实施？");
        sixth2_1.setIsSpecial(false);
        sixth2_1.setPid(pSixth2.getId());
        scoreRepository.save(sixth2_1);

        TemplateScore sixth2_2 = new TemplateScore();
        sixth2_2.setTemplateId(tempId);
        sixth2_2.setItemName("6.2.2");
        sixth2_2.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth2_2.setContent("对生产操作是否重新进行批准/放行？");
        sixth2_2.setIsSpecial(false);
        sixth2_1.setPid(pSixth2.getId());
        scoreRepository.save(sixth2_1);

        TemplateScore sixth2_3 = new TemplateScore();
        sixth2_3.setTemplateId(tempId);
        sixth2_3.setItemName("6.2.3");
        sixth2_3.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth2_3.setContent("特殊特性在生产中是否进行控制管理？");
        sixth2_3.setIsSpecial(true);
        sixth2_3.setPid(pSixth2.getId());
        scoreRepository.save(sixth2_3);

        TemplateScore sixth2_4 = new TemplateScore();
        sixth2_4.setTemplateId(tempId);
        sixth2_4.setItemName("6.2.4");
        sixth2_4.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth2_4.setContent("对未批准放行件和/或缺陷件是否进行管控？");
        sixth2_4.setIsSpecial(true);
        sixth2_4.setPid(pSixth2.getId());
        scoreRepository.save(sixth2_4);

        TemplateScore sixth2_5 = new TemplateScore();
        sixth2_5.setTemplateId(tempId);
        sixth2_5.setItemName("6.2.5");
        sixth2_5.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth2_5.setContent("是否确保材料/零部件在流转的过程中不发生混合/弄错？");
        sixth2_5.setIsSpecial(false);
        sixth2_5.setPid(pSixth2.getId());
        scoreRepository.save(sixth2_5);

        // P6.3 人力资源
        TemplateScore sixth3 = new TemplateScore();
        sixth3.setTemplateId(tempId);
        sixth3.setItemName("6.3");
        sixth3.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth3.setContent("哪些岗位为过程提供支持？（人力资源）");
        sixth3.setIsSpecial(false);
        sixth3.setIsNeed(false);
        sixth3.setPid(rSixth.getId());
        TemplateScore pSixth3 = scoreRepository.save(sixth3);

        TemplateScore sixth3_1 = new TemplateScore();
        sixth3_1.setTemplateId(tempId);
        sixth3_1.setItemName("6.3.1");
        sixth3_1.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth3_1.setContent("员工是否能胜任被委派的工作？");
        sixth3_1.setIsSpecial(true);
        sixth3_1.setPid(pSixth3.getId());
        scoreRepository.save(sixth3_1);

        TemplateScore sixth3_2 = new TemplateScore();
        sixth3_2.setTemplateId(tempId);
        sixth3_2.setItemName("6.3.2");
        sixth3_2.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth3_2.setContent("员工是否清楚被委以的产品和过程质量监控的职责和权限？");
        sixth3_2.setIsSpecial(false);
        sixth3_2.setPid(pSixth3.getId());
        scoreRepository.save(sixth3_2);

        TemplateScore sixth3_3 = new TemplateScore();
        sixth3_3.setTemplateId(tempId);
        sixth3_3.setItemName("6.3.3");
        sixth3_3.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth3_3.setContent("是否具备必要的人力资源？");
        sixth3_3.setIsSpecial(false);
        sixth3_3.setPid(pSixth3.getId());
        scoreRepository.save(sixth3_3);

        // P6.4 物质资源
        TemplateScore sixth4 = new TemplateScore();
        sixth4.setTemplateId(tempId);
        sixth4.setItemName("6.4");
        sixth4.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth4.setContent("通过哪些资源落实了过程？（物质资源）");
        sixth4.setIsSpecial(false);
        sixth4.setIsNeed(false);
        sixth4.setPid(rSixth.getId());
        TemplateScore pSixth4 = scoreRepository.save(sixth4);

        TemplateScore sixth4_1 = new TemplateScore();
        sixth4_1.setTemplateId(tempId);
        sixth4_1.setItemName("6.4.1");
        sixth4_1.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth4_1.setContent("使用的生产设备是否可以满足顾客对产品的特定要求？");
        sixth4_1.setIsSpecial(true);
        sixth4_1.setPid(pSixth4.getId());
        scoreRepository.save(sixth4_1);

        TemplateScore sixth4_2 = new TemplateScore();
        sixth4_2.setTemplateId(tempId);
        sixth4_2.setItemName("6.4.2");
        sixth4_2.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth4_2.setContent("生产设备/工具的维护保养是否受控？");
        sixth4_2.setIsSpecial(false);
        sixth4_2.setPid(pSixth4.getId());
        scoreRepository.save(sixth4_2);

        TemplateScore sixth4_3 = new TemplateScore();
        sixth4_3.setTemplateId(tempId);
        sixth4_3.setItemName("6.4.3");
        sixth4_3.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth4_3.setContent("通过使用的测量和试验设备，是否能够有效地监控质量要求？");
        sixth4_3.setIsSpecial(true);
        sixth4_3.setPid(pSixth4.getId());
        scoreRepository.save(sixth4_3);

        TemplateScore sixth4_4 = new TemplateScore();
        sixth4_4.setTemplateId(tempId);
        sixth4_4.setItemName("6.4.4");
        sixth4_4.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth4_4.setContent("加工工位以及检验工位是否满足具体的要求？");
        sixth4_4.setIsSpecial(false);
        sixth4_4.setPid(pSixth4.getId());
        scoreRepository.save(sixth4_4);

        TemplateScore sixth4_5 = new TemplateScore();
        sixth4_5.setTemplateId(tempId);
        sixth4_5.setItemName("6.4.5");
        sixth4_5.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth4_5.setContent("是否根据要求，正确的存放工具、装置和检验工具？");
        sixth4_5.setIsSpecial(false);
        sixth4_5.setPid(pSixth4.getId());
        scoreRepository.save(sixth4_5);

        // P6.5 有效性、效率、减少浪费
        TemplateScore sixth5 = new TemplateScore();
        sixth5.setTemplateId(tempId);
        sixth5.setItemName("6.5");
        sixth5.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth5.setContent("过程实现的效果如何？（有效性、效率、减少浪费）");
        sixth5.setIsSpecial(false);
        sixth5.setIsNeed(false);
        sixth5.setPid(rSixth.getId());
        TemplateScore pSixth5 = scoreRepository.save(sixth5);

        TemplateScore sixth5_1 = new TemplateScore();
        sixth5_1.setTemplateId(tempId);
        sixth5_1.setItemName("6.5.1");
        sixth5_1.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth5_1.setContent("是否为制造过程设定目标要求？");
        sixth5_1.setIsSpecial(false);
        sixth5_1.setPid(pSixth5.getId());
        scoreRepository.save(sixth5_1);

        TemplateScore sixth5_2 = new TemplateScore();
        sixth5_2.setTemplateId(tempId);
        sixth5_2.setItemName("6.5.2");
        sixth5_2.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth5_2.setContent("对收集的质量和过程数据是否可以开展评价？");
        sixth5_2.setIsSpecial(false);
        sixth5_2.setPid(pSixth5.getId());
        scoreRepository.save(sixth5_2);

        TemplateScore sixth5_3 = new TemplateScore();
        sixth5_3.setTemplateId(tempId);
        sixth5_3.setItemName("6.5.3");
        sixth5_3.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth5_3.setContent("一旦与产品和过程要求不符，是否对原因进行分析，并且检验纠正措施的有效性？");
        sixth5_3.setIsSpecial(true);
        sixth5_3.setPid(pSixth5.getId());
        scoreRepository.save(sixth5_3);

        TemplateScore sixth5_4 = new TemplateScore();
        sixth5_4.setTemplateId(tempId);
        sixth5_4.setItemName("6.5.4");
        sixth5_4.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth5_4.setContent("对过程和产品是否定期开展审核？");
        sixth5_4.setIsSpecial(false);
        sixth5_4.setPid(pSixth5.getId());
        scoreRepository.save(sixth5_4);

        // P6.6 有效性、效率、减少浪费
        TemplateScore sixth6 = new TemplateScore();
        sixth6.setTemplateId(tempId);
        sixth6.setItemName("6.6");
        sixth6.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth6.setContent("过程应取得怎样的成果？过程成果/输出）");
        sixth6.setIsSpecial(false);
        sixth6.setIsNeed(false);
        sixth6.setPid(rSixth.getId());
        TemplateScore pSixth6 = scoreRepository.save(sixth6);

        TemplateScore sixth6_1 = new TemplateScore();
        sixth6_1.setTemplateId(tempId);
        sixth6_1.setItemName("6.6.1");
        sixth6_1.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth6_1.setContent("产量/生产批量是否是根据需要确定的，并且有目的地运往下道工序？");
        sixth6_1.setIsSpecial(false);
        sixth6_1.setPid(pSixth6.getId());
        scoreRepository.save(sixth6_1);

        TemplateScore sixth6_2 = new TemplateScore();
        sixth6_2.setTemplateId(tempId);
        sixth6_2.setItemName("6.6.2");
        sixth6_2.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth6_2.setContent("是否根据要求对产品/零部件进行适当仓储，所使用的运输设备/包装方式是否与产品/零部件的特殊特性相互适应？");
        sixth6_2.setIsSpecial(false);
        sixth6_2.setPid(pSixth6.getId());
        scoreRepository.save(sixth6_2);

        TemplateScore sixth6_3 = new TemplateScore();
        sixth6_3.setTemplateId(tempId);
        sixth6_3.setItemName("6.6.3");
        sixth6_3.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth6_3.setContent("是否对必要的记录和放行进行文件记录？");
        sixth6_3.setIsSpecial(false);
        sixth6_3.setPid(pSixth6.getId());
        scoreRepository.save(sixth6_3);

        TemplateScore sixth6_4 = new TemplateScore();
        sixth6_4.setTemplateId(tempId);
        sixth6_4.setItemName("6.6.4");
        sixth6_4.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth6_4.setContent("成品的交付方面是否满足顾客要求？");
        sixth6_4.setIsSpecial(true);
        sixth6_4.setPid(pSixth6.getId());
        scoreRepository.save(sixth6_4);

        // P7-顾客关怀/顾客满意/服务
        TemplateScore seventh = new TemplateScore();
        seventh.setTemplateId(tempId);
        seventh.setItemName(CommonConstants.TEMPLATE_QUES_P7);
        seventh.setItemType(CommonConstants.TEMPLATE_QUES_P7);
        seventh.setContent("顾客关怀/顾客满意/服务");
        seventh.setIsSpecial(false);
        seventh.setIsNeed(false);
        TemplateScore rSeventh = scoreRepository.save(seventh);

        // P7.1
        TemplateScore seventh1 = new TemplateScore();
        seventh1.setTemplateId(tempId);
        seventh1.setItemName("7.1");
        seventh1.setItemType(CommonConstants.TEMPLATE_QUES_P7);
        seventh1.setContent("质量管理体系、产品和过程方面的要求是否得到满足？");
        seventh1.setIsSpecial(false);
        seventh1.setPid(rSeventh.getId());
        scores7.add(seventh1);

        // P7.2
        TemplateScore seventh2 = new TemplateScore();
        seventh2.setTemplateId(tempId);
        seventh2.setItemName("7.2");
        seventh2.setItemType(CommonConstants.TEMPLATE_QUES_P7);
        seventh2.setContent("是否对顾客关怀提供了必要的保障？");
        seventh2.setIsSpecial(false);
        seventh2.setPid(rSeventh.getId());
        scores7.add(seventh2);

        // P7.3
        TemplateScore seventh3 = new TemplateScore();
        seventh3.setTemplateId(tempId);
        seventh3.setItemName("7.3");
        seventh3.setItemType(CommonConstants.TEMPLATE_QUES_P7);
        seventh3.setContent("是否保障了供货？");
        seventh3.setIsSpecial(true);
        seventh3.setPid(rSeventh.getId());
        scores7.add(seventh3);

        // P7.4
        TemplateScore seventh4 = new TemplateScore();
        seventh4.setTemplateId(tempId);
        seventh4.setItemName("7.4");
        seventh4.setItemType(CommonConstants.TEMPLATE_QUES_P7);
        seventh4.setContent("针对投诉是否开展了失效分析，并且有效地落实了纠正措施？");
        seventh4.setIsSpecial(true);
        seventh4.setPid(rSeventh.getId());
        scores7.add(seventh4);

        // P7.5
        TemplateScore seventh5 = new TemplateScore();
        seventh5.setTemplateId(tempId);
        seventh5.setItemName("7.5");
        seventh5.setItemType(CommonConstants.TEMPLATE_QUES_P7);
        seventh5.setContent("针对各具体的任务，相关的人员是否具备资质，是否定义了责权关系？");
        seventh5.setIsSpecial(false);
        seventh5.setPid(rSeventh.getId());
        scores7.add(seventh5);
        scoreRepository.saveAll(scores7);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        // 删除关联的任务
        preTrailRepository.physicalDeleteAllByStorageIdIn(ids);
        auditPlanRepository.deleteAllByIdIn(ids);
        // 删除关联的模板信息
        planTemplateRepository.deleteByPlanIdIn(ids);
        // todo 删除模板内容信息
        // todo 删除VDA6.3模板下问题清单信息
        // 删除附件信息
        planFileRepository.deleteByPlanIdIn(ids);
        // JPA自动删除计划审核人关联信息，此处无需处理
        // 删除计划执行信息
        executeRepository.deleteByPlanIdIn(ids);
        // 删除报告信息
        reportRepository.deleteByPlanIdIn(ids);
        // 删除报告下问题信息
        questionRepository.deleteByPlanIdIn(ids);

    }

    @Override
    public void download(List<AuditPlanDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (AuditPlanDto planDto : queryAll) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("审核计划名称", planDto.getRealName());
            map.put("审核编号", planDto.getAuditNo());
            map.put("审核种类", planDto.getType());
            map.put("审核内容", planDto.getContent());
            map.put("审核原因", planDto.getReason());
            map.put("审核计划时间", planDto.getPlanTime());
            map.put("审核计划周期", planDto.getPeriod());
            map.put("审核模板类型", planDto.getTemplateType());
            map.put("审核范围", planDto.getScope());
            map.put("审核产品", planDto.getProduct());
            map.put("产品技术", planDto.getTechnology());
            map.put("审核地点", planDto.getAddress());
            map.put("审核产线", planDto.getLine());
            map.put("计划状态", planDto.getStatus());
            map.put("创建日期", planDto.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public List<AuditPlan> findByExample(AuditPlanQueryDto queryDto) {
        return auditPlanRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, queryDto, criteriaBuilder));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activatedById(Long planId) {
        AuditPlan plan = auditPlanRepository.findById(planId).orElseGet(AuditPlan::new);
        ValidationUtil.isNull(plan.getId(), "AuditPlan", "id", planId);
        PreTrail task = preTrailRepository.findTaskByStorageId(planId, CommonConstants.TRAIL_TYPE_AUDIT_PLAN, CommonConstants.IS_DEL, false);
        if (task != null) {
            // 仅取最新一条数据，理论上也只会有一条数据
//            Long superiorId = SecurityUtils.getCurrentUserSuperior() == null ? commonUtils.getZlbMaster() : SecurityUtils.getCurrentUserSuperior();
            task.setIsDel(CommonConstants.NOT_DEL);
//            task.setApprovedBy(superiorId);
            preTrailRepository.save(task);
            // 变更审核人员的审核状态
            plan.setApprovalStatus(CommonConstants.AUDIT_PLAN_STATUS_WAIT);
            auditPlanRepository.save(plan);
        } else {
            throw new BadRequestException("This plan no Task Existed!未查到任务信息！");
        }
    }

    @Override
    public void checkHasAuthExecute(Long planId) {
        Boolean isAdmin = SecurityUtils.isAdmin();
        Long userId = SecurityUtils.getCurrentUserId();
        if (!isAdmin) {
            // 非管理不可擅改
            AuditPlan plan = auditPlanRepository.findById(planId).orElseGet(AuditPlan::new);
            ValidationUtil.isNull(plan.getId(), "AuditPlan", "id", planId);
            Set<Auditor> auditors = plan.getAuditors();
            if (ValidationUtil.isNotEmpty(Collections.singletonList(auditors))) {
                List<Long> idList = new ArrayList<>();
                for (Auditor auditor : auditors) {
                    idList.add(auditor.getUserId());
                }
                if (!idList.contains(userId)) {
                    throw new BadRequestException("No Access!非本计划审核人员，无修改权限！");
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long planId) {
        // 校验是否具备提交结案权限
        checkHasAuthExecute(planId);
        AuditPlan plan = auditPlanRepository.findById(planId).orElseGet(AuditPlan::new);
        ValidationUtil.isNull(plan.getId(), "AuditPlan", "id", planId);
        // 变更审核人员的审核状态
        plan.setStatus(CommonConstants.AUDIT_PLAN_STATUS_FINISHED);
        // 提交结案时间和耗时时长
        plan.setCloseTime(CommonUtils.getNow());
        // 验证是否超出时间
        AuditPlanReport report = reportRepository.findByPlanId(plan.getId());
        if (report != null) {
            if (CommonUtils.getNow().getTime() > report.getFinalDeadline().getTime()) {
                plan.setIsOverdue(true);
            } else {
                plan.setIsOverdue(false);
            }
        }
        auditPlanRepository.save(plan);
    }

    @Override
    public Map<String, Object> queryAuditPlansByStatus() {
        Map<String, Object> map = new HashMap<>();
        List<CommonDTO> list = new ArrayList<>();
        CommonConstants.AUDIT_PLAN_STATUS_LIST.forEach(status -> {
            int count = 0;
            count = auditPlanRepository.getCountByStatus(status);
            CommonDTO commonDTO = new CommonDTO();
            commonDTO.setName(status);
            commonDTO.setValue(String.valueOf(count));
            list.add(commonDTO);
        });
        int totalCount = auditPlanRepository.getAuditorCount();
        map.put("content", totalCount);
        map.put("totalElements", list);
        map.put("scope", "All");

        return map;
    }

    @Override
    public Map<String, Object> queryAuditPlansByDate(ApQueryDto dto) {
        if (dto.getYearType().equals(CommonConstants.ALL)) {

        }
        return null;
    }

    @Override
    public Map<String, Object> getRtdByYear() {
        Map<String, Object> map = new HashMap<>();
        List<Integer> yearList = new ArrayList<>();
        List<Integer> planList = new ArrayList<>();
        List<Integer> execList = new ArrayList<>();
        List<Integer> trailList = new ArrayList<>();
        List<Integer> closeList = new ArrayList<>();
        AuditPlan first = auditPlanRepository.findFirstByCreateTime();
//        AuditPlan last = auditPlanRepository.findFirstByCreateTime();
        if (first != null) {
            Date now = new Date();
            Timestamp firstTime = first.getCreateTime();
//            Timestamp lastTime = last.getCreateTime();
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(firstTime);
//            cal2.setTime(lastTime);
            cal2.setTime(new Timestamp(now.getTime()));
            int firstYear = cal1.get(Calendar.YEAR);
            int lastYear = cal2.get(Calendar.YEAR);
            int years = lastYear - firstYear;
            if (years == 0) {
                yearList.add(lastYear);
                planList.add(auditPlanRepository.findCountByYearAndStatus(lastYear, CommonConstants.AUDIT_PLAN_STATUS_TO));
                execList.add(auditPlanRepository.findCountByYearAndStatus(lastYear, CommonConstants.AUDIT_PLAN_STATUS_PROCESS));
                trailList.add(auditPlanRepository.findCountByYearAndStatus(lastYear, CommonConstants.AUDIT_PLAN_STATUS_TRACE));
                closeList.add(auditPlanRepository.findCountByYearAndStatus(lastYear, CommonConstants.AUDIT_PLAN_STATUS_FINISHED));
            } else {
                for (int i = 0; i < years + 1; i++) {
                    int year = firstYear + i;
                    yearList.add(year);
                    planList.add(auditPlanRepository.findCountByYearAndStatus(year, CommonConstants.AUDIT_PLAN_STATUS_TO));
                    execList.add(auditPlanRepository.findCountByYearAndStatus(year, CommonConstants.AUDIT_PLAN_STATUS_PROCESS));
                    trailList.add(auditPlanRepository.findCountByYearAndStatus(year, CommonConstants.AUDIT_PLAN_STATUS_TRACE));
                    closeList.add(auditPlanRepository.findCountByYearAndStatus(year, CommonConstants.AUDIT_PLAN_STATUS_FINISHED));
                }
            }
        }
        map.put("years", yearList);
        map.put("planDatas", planList);
        map.put("execDatas", execList);
        map.put("trailDatas", trailList);
        map.put("closeDatas", closeList);
        return map;
    }

    @Override
    public Map<String, Object> getRtdByMonth() {
        Map<String, Object> map = new HashMap<>();
        List<Integer> planList = new ArrayList<>();
        List<Integer> execList = new ArrayList<>();
        List<Integer> trailList = new ArrayList<>();
        List<Integer> closeList = new ArrayList<>();
        for (int i = 1; i < 13; i++) {
            planList.add(auditPlanRepository.findCountByMonthAndStatus(i, CommonConstants.AUDIT_PLAN_STATUS_TO));
            execList.add(auditPlanRepository.findCountByMonthAndStatus(i, CommonConstants.AUDIT_PLAN_STATUS_PROCESS));
            trailList.add(auditPlanRepository.findCountByMonthAndStatus(i, CommonConstants.AUDIT_PLAN_STATUS_TRACE));
            closeList.add(auditPlanRepository.findCountByMonthAndStatus(i, CommonConstants.AUDIT_PLAN_STATUS_FINISHED));
        }
        map.put("planDatas", planList);
        map.put("execDatas", execList);
        map.put("trailDatas", trailList);
        map.put("closeDatas", closeList);
        return map;
    }

    @Override
    public List<AuditPlan> findByExampleV2(AuditPlanV2QueryDto queryDto) {
        return auditPlanRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, queryDto, criteriaBuilder));
    }

}
