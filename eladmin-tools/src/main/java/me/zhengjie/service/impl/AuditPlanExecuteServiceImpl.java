package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.*;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.*;
import me.zhengjie.service.AuditPlanExecuteService;
import me.zhengjie.service.AuditPlanService;
import me.zhengjie.service.dto.AuditPlanExecuteDto;
import me.zhengjie.service.mapstruct.AuditPlanExecuteMapper;
import me.zhengjie.utils.SecurityUtils;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * @author tmj
 * @version 1.0
 * @date 2021/11/2 16:04
 */
@Service
@RequiredArgsConstructor
public class AuditPlanExecuteServiceImpl implements AuditPlanExecuteService {

    private final AuditPlanRepository auditPlanRepository;
    private final AuditPlanExecuteRepository auditPlanExecuteRepository;
    private final ApproverRepository approverRepository;
    private final AuditPlanExecuteMapper auditPlanExecuteMapper;
    private final AuditPlanService auditPlanService;
    private final AuditPlanReportRepository auditPlanReportRepository;
    private final TemplateScoreRepository scoreRepository;

    @Override
    public AuditPlanExecuteDto findByPlanId(Long planId) {
        AuditPlan plan = auditPlanRepository.findById(planId).orElseGet(AuditPlan::new);
        ValidationUtil.isNull(plan.getId(), "AuditPlan", "id", planId);
        AuditPlanExecute execute = auditPlanExecuteRepository.findByPlanId(planId);
        if (execute == null) {
            throw new BadRequestException("Executor not found!未查到执行信息！");
        }
        AuditPlanExecuteDto dto = auditPlanExecuteMapper.toDto(execute);
        Approver approver = approverRepository.findById(dto.getUserId()).orElseGet(Approver::new);
        ValidationUtil.isNull(approver.getId(), "Approver", "id", dto.getUserId());
        dto.setUsername(approver.getUsername());
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(AuditPlanExecute resources) {
        AuditPlan plan = auditPlanRepository.findById(resources.getPlanId()).orElseGet(AuditPlan::new);
        ValidationUtil.isNull(plan.getId(), "AuditPlan", "id", resources.getPlanId());
        Long tempId = plan.getTemplateId();
        // 判断是否有执行改计划的权限
        auditPlanService.checkHasAuthExecute(plan.getId());
        AuditPlanExecute execute = auditPlanExecuteRepository.findByPlanId(resources.getPlanId());
        if (execute != null) {
            throw new BadRequestException("Executor has Existed!不要重复加执行者！");
        }
        // 判断执行是否已存在
        auditPlanExecuteRepository.save(resources);
        // 审核计划状态改为“进行”
        plan.setStatus(CommonConstants.AUDIT_PLAN_STATUS_PROCESS);
        auditPlanRepository.save(plan);
        // 判断师傅是系统推荐模板，若是则需要初始化审核数据
        if (CommonConstants.AUDIT_PLAN_TEMPLATE_LIST.contains(plan.getTemplateType())) {
            // todo 按照scope初始化需要打分的问题清单并激活：isActive = true
            List<TemplateScore> scores = new ArrayList<>();
            List<TemplateScore> activeScores = new ArrayList<>();
            // 1.第一步：根据审核计划中的审核范围激活对应需要打分的问题清单
            String[] scopeArr = plan.getScope().split(",");
            if (ValidationUtil.isNotEmpty(Collections.singletonList(scopeArr))) {
                scores = scoreRepository.findByTempIdAndItemTypeIn(tempId, Arrays.asList(scopeArr), false);
                if (ValidationUtil.isNotEmpty(scores)) {
                    scores.forEach(score -> {
                        if (!score.getItemType().equals(CommonConstants.TEMPLATE_QUES_P6) && !score.getItemType().equals(CommonConstants.TEMPLATE_QUES_P7)) {
                            // todo 初始化除了P6\P7外的模板数据，switch-case
                            score.setIsActive(true);
                            activeScores.add(score);
                        }
                    });
                    scoreRepository.saveAll(activeScores);
                    // 如果需要为P6打分，则需要按照产线初始化P6明细
                    if (Arrays.asList(scopeArr).contains(CommonConstants.TEMPLATE_QUES_P6)) {
                        List<String> lines = Arrays.asList(plan.getLine().split(","));
                        if (ValidationUtil.isNotEmpty(lines)) {
                            scoreRepository.deleteByTemplateIdAndItemType(tempId, CommonConstants.TEMPLATE_QUES_P6);
                            // 2.第二步：根据产线初始化P6打分问题清单内容
                            // 初始化P6根节点
                            initP6sList(tempId, lines);
                        }
                    }
                    // 如果需要为P7打分，则需要初始化P7明细,这样初始化可以保证顺序正确
                    if (Arrays.asList(scopeArr).contains(CommonConstants.TEMPLATE_QUES_P7)) {
                        scoreRepository.deleteByTemplateIdAndItemType(tempId, CommonConstants.TEMPLATE_QUES_P7);
                        initP7sList(tempId);
                    }
                }
            }
            // 根据产线初始化VDA6.3下模板数据
        }
        // 初始化审核报告信息
        AuditPlanReport report = new AuditPlanReport();
        report.setPlanId(plan.getId());
        // todo 使用系统模板，需要传递审核结果和分数？？
        /*report.setResult();
        report.setScore();*/
        auditPlanReportRepository.save(report);
    }

    private void initP7sList(Long tempId) {
        List<TemplateScore> scores7 = new ArrayList<>();
        // P7-顾客关怀/顾客满意/服务
        TemplateScore seventh = new TemplateScore();
        seventh.setTemplateId(tempId);
        seventh.setItemName(CommonConstants.TEMPLATE_QUES_P7);
        seventh.setItemType(CommonConstants.TEMPLATE_QUES_P7);
        seventh.setContent("顾客关怀/顾客满意/服务");
        seventh.setIsSpecial(false);
        seventh.setIsNeed(false);
        seventh.setIsActive(true);
        TemplateScore rSeventh = scoreRepository.save(seventh);

        // P7.1
        TemplateScore seventh1 = new TemplateScore();
        seventh1.setTemplateId(tempId);
        seventh1.setItemName("7.1");
        seventh1.setItemType(CommonConstants.TEMPLATE_QUES_P7);
        seventh1.setContent("质量管理体系、产品和过程方面的要求是否得到满足？");
        seventh1.setIsSpecial(false);
        seventh1.setIsActive(true);
        seventh1.setPid(rSeventh.getId());
        scores7.add(seventh1);

        // P7.2
        TemplateScore seventh2 = new TemplateScore();
        seventh2.setTemplateId(tempId);
        seventh2.setItemName("7.2");
        seventh2.setItemType(CommonConstants.TEMPLATE_QUES_P7);
        seventh2.setContent("是否对顾客关怀提供了必要的保障？");
        seventh2.setIsSpecial(false);
        seventh2.setIsActive(true);
        seventh2.setPid(rSeventh.getId());
        scores7.add(seventh2);

        // P7.3
        TemplateScore seventh3 = new TemplateScore();
        seventh3.setTemplateId(tempId);
        seventh3.setItemName("7.3");
        seventh3.setItemType(CommonConstants.TEMPLATE_QUES_P7);
        seventh3.setContent("是否保障了供货？");
        seventh3.setIsSpecial(true);
        seventh3.setIsActive(true);
        seventh3.setPid(rSeventh.getId());
        scores7.add(seventh3);

        // P7.4
        TemplateScore seventh4 = new TemplateScore();
        seventh4.setTemplateId(tempId);
        seventh4.setItemName("7.4");
        seventh4.setItemType(CommonConstants.TEMPLATE_QUES_P7);
        seventh4.setContent("针对投诉是否开展了失效分析，并且有效地落实了纠正措施？");
        seventh4.setIsSpecial(true);
        seventh4.setIsActive(true);
        seventh4.setPid(rSeventh.getId());
        scores7.add(seventh4);

        // P7.5
        TemplateScore seventh5 = new TemplateScore();
        seventh5.setTemplateId(tempId);
        seventh5.setItemName("7.5");
        seventh5.setItemType(CommonConstants.TEMPLATE_QUES_P7);
        seventh5.setContent("针对各具体的任务，相关的人员是否具备资质，是否定义了责权关系？");
        seventh5.setIsSpecial(false);
        seventh5.setIsActive(true);
        seventh5.setPid(rSeventh.getId());
        scores7.add(seventh5);
        scoreRepository.saveAll(scores7);
    }

    private void initP6sList(Long tempId, List<String> lines) {
        TemplateScore sixth = new TemplateScore();
        sixth.setTemplateId(tempId);
        sixth.setItemName(CommonConstants.TEMPLATE_QUES_P6);
        sixth.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth.setContent("工艺分析/生产");
        sixth.setIsSpecial(false);
        sixth.setIsNeed(false);
        sixth.setIsActive(true);
        TemplateScore rSixth = scoreRepository.save(sixth);

        //  6线路集合
        List<TemplateScore> six1List = new ArrayList<>();
        lines.forEach(line -> {
            TemplateScore score = new TemplateScore();
            score.setTemplateId(tempId);
            score.setItemName("过程" + (lines.indexOf(line) + 1));
            score.setItemType(CommonConstants.TEMPLATE_QUES_P6);
            score.setContent(line);
            score.setIsSpecial(false);
            score.setIsNeed(false);
            score.setIsActive(true);
            score.setPid(rSixth.getId());
            six1List.add(score);
        });
        scoreRepository.saveAll(six1List);

        // P6.1 过程输入
        TemplateScore sixth1 = new TemplateScore();
        sixth1.setTemplateId(tempId);
        sixth1.setItemName("6.1");
        sixth1.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth1.setContent("过程输入是什么？（过程输入）");
        sixth1.setIsSpecial(false);
        sixth1.setIsNeed(false);
        sixth1.setIsActive(true);
        sixth1.setPid(rSixth.getId());
        TemplateScore pSixth1 = scoreRepository.save(sixth1);
        // 6.1.1
        TemplateScore sixth1_1 = new TemplateScore();
        sixth1_1.setTemplateId(tempId);
        sixth1_1.setItemName("6.1.1");
        sixth1_1.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth1_1.setContent("是否从开发向批量生产进行了项目移交，并确保了可靠的量产启动？");
        sixth1_1.setIsSpecial(false);
        sixth1_1.setIsNeed(false);
        sixth1_1.setIsActive(true);
        sixth1_1.setPid(pSixth1.getId());
        TemplateScore lSixth1_1 = scoreRepository.save(sixth1_1);
        List<TemplateScore> six1_1List = new ArrayList<>();
        initLineIsSpecial(tempId, lines, lSixth1_1, six1_1List, false);
        scoreRepository.saveAll(six1_1List);

        // 6.1.2
        TemplateScore sixth1_2 = new TemplateScore();
        sixth1_2.setTemplateId(tempId);
        sixth1_2.setItemName("6.1.2");
        sixth1_2.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth1_2.setContent("来料是否在约定的时间按所需数量/生产批次大小被送至正确的地点/工位？");
        sixth1_2.setIsSpecial(false);
        sixth1_2.setIsNeed(false);
        sixth1_2.setIsActive(true);
        sixth1_2.setPid(pSixth1.getId());
        TemplateScore lSixth1_2 = scoreRepository.save(sixth1_2);
        List<TemplateScore> six1_2List = new ArrayList<>();
        initLineIsSpecial(tempId, lines, lSixth1_2, six1_2List, false);
        scoreRepository.saveAll(six1_2List);

        // 6.1.3
        TemplateScore sixth1_3 = new TemplateScore();
        sixth1_3.setTemplateId(tempId);
        sixth1_3.setItemName("6.1.3");
        sixth1_3.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth1_3.setContent("是否对来料进行适当的仓储，所使用的运输工具/包装设备是否适合来料的特殊特性？");
        sixth1_3.setIsSpecial(false);
        sixth1_3.setIsNeed(false);
        sixth1_3.setIsActive(true);
        sixth1_3.setPid(pSixth1.getId());
        TemplateScore lSixth1_3 = scoreRepository.save(sixth1_3);
        List<TemplateScore> six1_3List = new ArrayList<>();
        initLineIsSpecial(tempId, lines, lSixth1_3, six1_3List, false);
        scoreRepository.saveAll(six1_3List);

        // 6.1.4
        TemplateScore sixth1_4 = new TemplateScore();
        sixth1_4.setTemplateId(tempId);
        sixth1_4.setItemName("6.1.4");
        sixth1_4.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth1_4.setContent("必要的标识/记录/放行是否具备，并且适当地体现在来料上？");
        sixth1_4.setIsSpecial(false);
        sixth1_4.setIsNeed(false);
        sixth1_4.setIsActive(true);
        sixth1_4.setPid(pSixth1.getId());
        TemplateScore lSixth1_4 = scoreRepository.save(sixth1_4);
        List<TemplateScore> six1_4List = new ArrayList<>();
        initLineIsSpecial(tempId, lines, lSixth1_4, six1_4List, false);
        scoreRepository.saveAll(six1_4List);

        // 6.1.5
        TemplateScore sixth1_5 = new TemplateScore();
        sixth1_5.setTemplateId(tempId);
        sixth1_5.setItemName("6.1.5");
        sixth1_5.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth1_5.setContent("在量产过程中，是否对产品或过程的变更开展跟踪和记录？");
        sixth1_5.setIsSpecial(true);
        sixth1_5.setIsNeed(false);
        sixth1_5.setIsActive(true);
        sixth1_5.setPid(pSixth1.getId());
        TemplateScore lSixth1_5 = scoreRepository.save(sixth1_5);
        List<TemplateScore> six1_5List = new ArrayList<>();
        initLineIsSpecial(tempId, lines, lSixth1_5, six1_5List, true);
        scoreRepository.saveAll(six1_5List);

        // P6.2 所有生产过程是否受控？（工艺流程）
        TemplateScore sixth2 = new TemplateScore();
        sixth2.setTemplateId(tempId);
        sixth2.setItemName("6.2");
        sixth2.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth2.setContent("所有生产过程是否受控？（工艺流程）");
        sixth2.setIsSpecial(false);
        sixth2.setIsNeed(false);
        sixth2.setIsActive(true);
        sixth2.setPid(rSixth.getId());
        TemplateScore pSixth2 = scoreRepository.save(sixth2);

        // 6.2.1
        TemplateScore sixth2_1 = new TemplateScore();
        sixth2_1.setTemplateId(tempId);
        sixth2_1.setItemName("6.2.1");
        sixth2_1.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth2_1.setContent("控制计划里的要求是否完整，并且得到有效实施？");
        sixth2_1.setIsSpecial(false);
        sixth2_1.setIsNeed(false);
        sixth2_1.setIsActive(true);
        sixth2_1.setPid(pSixth2.getId());
        TemplateScore lSixth2_1 = scoreRepository.save(sixth2_1);
        List<TemplateScore> six2_1List = new ArrayList<>();
        initLineIsSpecial(tempId, lines, lSixth2_1, six2_1List, false);
        scoreRepository.saveAll(six2_1List);

        // 6.2.2
        TemplateScore sixth2_2 = new TemplateScore();
        sixth2_2.setTemplateId(tempId);
        sixth2_2.setItemName("6.2.2");
        sixth2_2.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth2_2.setContent("对生产操作是否重新进行批准/放行？");
        sixth2_2.setIsSpecial(false);
        sixth2_2.setIsNeed(false);
        sixth2_2.setIsActive(true);
        sixth2_2.setPid(pSixth2.getId());
        TemplateScore lSixth2_2 = scoreRepository.save(sixth2_2);
        List<TemplateScore> six2_2List = new ArrayList<>();
        initLineIsSpecial(tempId, lines, lSixth2_2, six2_2List, false);
        scoreRepository.saveAll(six2_2List);

        // 6.2.3
        TemplateScore sixth2_3 = new TemplateScore();
        sixth2_3.setTemplateId(tempId);
        sixth2_3.setItemName("6.2.3");
        sixth2_3.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth2_3.setContent("特殊特性在生产中是否进行控制管理？");
        sixth2_3.setIsSpecial(true);
        sixth2_3.setIsNeed(false);
        sixth2_3.setIsActive(true);
        sixth2_3.setPid(pSixth2.getId());
        TemplateScore lSixth2_3 = scoreRepository.save(sixth2_3);
        List<TemplateScore> six2_3List = new ArrayList<>();
        initLineIsSpecial(tempId, lines, lSixth2_3, six2_3List, true);
        scoreRepository.saveAll(six2_3List);

        // 6.2.4
        TemplateScore sixth2_4 = new TemplateScore();
        sixth2_4.setTemplateId(tempId);
        sixth2_4.setItemName("6.2.4");
        sixth2_4.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth2_4.setContent("对未批准放行件和/或缺陷件是否进行管控？");
        sixth2_4.setIsSpecial(true);
        sixth2_4.setIsNeed(false);
        sixth2_4.setIsActive(true);
        sixth2_4.setPid(pSixth2.getId());
        TemplateScore lSixth2_4 = scoreRepository.save(sixth2_4);
        List<TemplateScore> six2_4List = new ArrayList<>();
        initLineIsSpecial(tempId, lines, lSixth2_4, six2_4List, true);
        scoreRepository.saveAll(six2_4List);

        // 6.2.5
        TemplateScore sixth2_5 = new TemplateScore();
        sixth2_5.setTemplateId(tempId);
        sixth2_5.setItemName("6.2.5");
        sixth2_5.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth2_5.setContent("是否确保材料/零部件在流转的过程中不发生混合/弄错？");
        sixth2_5.setIsSpecial(false);
        sixth2_5.setIsNeed(false);
        sixth2_5.setIsActive(true);
        sixth2_5.setPid(pSixth2.getId());
        TemplateScore lSixth2_5 = scoreRepository.save(sixth2_5);
        List<TemplateScore> six2_5List = new ArrayList<>();
        initLineIsSpecial(tempId, lines, lSixth2_5, six2_5List, false);
        scoreRepository.saveAll(six2_5List);

        // P6.3 人力资源
        TemplateScore sixth3 = new TemplateScore();
        sixth3.setTemplateId(tempId);
        sixth3.setItemName("6.3");
        sixth3.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth3.setContent("哪些岗位为过程提供支持？（人力资源）");
        sixth3.setIsSpecial(false);
        sixth3.setIsNeed(false);
        sixth3.setIsActive(true);
        sixth3.setPid(rSixth.getId());
        TemplateScore pSixth3 = scoreRepository.save(sixth3);

        // 6.3.1
        TemplateScore sixth3_1 = new TemplateScore();
        sixth3_1.setTemplateId(tempId);
        sixth3_1.setItemName("6.3.1");
        sixth3_1.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth3_1.setContent("员工是否能胜任被委派的工作？");
        sixth3_1.setIsSpecial(true);
        sixth3_1.setIsNeed(false);
        sixth3_1.setIsActive(true);
        sixth3_1.setPid(pSixth3.getId());
        TemplateScore lSixth3_1 = scoreRepository.save(sixth3_1);
        List<TemplateScore> six3_1List = new ArrayList<>();
        initLineIsSpecial(tempId, lines, lSixth3_1, six3_1List, true);
        scoreRepository.saveAll(six3_1List);

        // 6.3.2
        TemplateScore sixth3_2 = new TemplateScore();
        sixth3_2.setTemplateId(tempId);
        sixth3_2.setItemName("6.3.2");
        sixth3_2.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth3_2.setContent("员工是否清楚被委以的产品和过程质量监控的职责和权限？");
        sixth3_2.setIsSpecial(false);
        sixth3_2.setIsNeed(false);
        sixth3_2.setIsActive(true);
        sixth3_2.setPid(pSixth3.getId());
        TemplateScore lSixth3_2 = scoreRepository.save(sixth3_2);
        List<TemplateScore> six3_2List = new ArrayList<>();
        initLineIsSpecial(tempId, lines, lSixth3_2, six3_2List, false);
        scoreRepository.saveAll(six3_2List);

        // 6.3.3
        TemplateScore sixth3_3 = new TemplateScore();
        sixth3_3.setTemplateId(tempId);
        sixth3_3.setItemName("6.3.3");
        sixth3_3.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth3_3.setContent("是否具备必要的人力资源？");
        sixth3_3.setIsSpecial(false);
        sixth3_3.setIsNeed(false);
        sixth3_3.setIsActive(true);
        sixth3_3.setPid(pSixth3.getId());
        TemplateScore lSixth3_3 = scoreRepository.save(sixth3_3);
        List<TemplateScore> six3_3List = new ArrayList<>();
        initLineIsSpecial(tempId, lines, lSixth3_3, six3_3List, false);
        scoreRepository.saveAll(six3_3List);

        // P6.4 物质资源
        TemplateScore sixth4 = new TemplateScore();
        sixth4.setTemplateId(tempId);
        sixth4.setItemName("6.4");
        sixth4.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth4.setContent("通过哪些资源落实了过程？（物质资源）");
        sixth4.setIsSpecial(false);
        sixth4.setIsNeed(false);
        sixth4.setIsActive(true);
        sixth4.setPid(rSixth.getId());
        TemplateScore pSixth4 = scoreRepository.save(sixth4);

        // 6.4.1
        TemplateScore sixth4_1 = new TemplateScore();
        sixth4_1.setTemplateId(tempId);
        sixth4_1.setItemName("6.4.1");
        sixth4_1.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth4_1.setContent("使用的生产设备是否可以满足顾客对产品的特定要求？");
        sixth4_1.setIsSpecial(true);
        sixth4_1.setIsNeed(false);
        sixth4_1.setIsActive(true);
        sixth4_1.setPid(pSixth4.getId());
        TemplateScore lSixth4_1 = scoreRepository.save(sixth4_1);
        List<TemplateScore> six4_1List = new ArrayList<>();
        initLineIsSpecial(tempId, lines, lSixth4_1, six4_1List, true);
        scoreRepository.saveAll(six4_1List);

        // 6.4.2
        TemplateScore sixth4_2 = new TemplateScore();
        sixth4_2.setTemplateId(tempId);
        sixth4_2.setItemName("6.4.2");
        sixth4_2.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth4_2.setContent("生产设备/工具的维护保养是否受控？");
        sixth4_2.setIsSpecial(false);
        sixth4_2.setIsNeed(false);
        sixth4_2.setIsActive(true);
        sixth4_2.setPid(pSixth4.getId());
        TemplateScore lSixth4_2 = scoreRepository.save(sixth4_2);
        List<TemplateScore> six4_2List = new ArrayList<>();
        initLineIsSpecial(tempId, lines, lSixth4_2, six4_2List, false);
        scoreRepository.saveAll(six4_2List);

        // 6.4.3
        TemplateScore sixth4_3 = new TemplateScore();
        sixth4_3.setTemplateId(tempId);
        sixth4_3.setItemName("6.4.2");
        sixth4_3.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth4_3.setContent("通过使用的测量和试验设备，是否能够有效地监控质量要求？");
        sixth4_3.setIsSpecial(true);
        sixth4_3.setIsNeed(false);
        sixth4_3.setIsActive(true);
        sixth4_3.setPid(pSixth4.getId());
        TemplateScore lSixth4_3 = scoreRepository.save(sixth4_3);
        List<TemplateScore> six4_3List = new ArrayList<>();
        initLineIsSpecial(tempId, lines, lSixth4_3, six4_3List, true);
        scoreRepository.saveAll(six4_3List);

        // 6.4.4
        TemplateScore sixth4_4 = new TemplateScore();
        sixth4_4.setTemplateId(tempId);
        sixth4_4.setItemName("6.4.4");
        sixth4_4.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth4_4.setContent("加工工位以及检验工位是否满足具体的要求？");
        sixth4_4.setIsSpecial(false);
        sixth4_4.setIsNeed(false);
        sixth4_4.setIsActive(true);
        sixth4_4.setPid(pSixth4.getId());
        TemplateScore lSixth4_4 = scoreRepository.save(sixth4_4);
        List<TemplateScore> six4_4List = new ArrayList<>();
        initLineIsSpecial(tempId, lines, lSixth4_4, six4_4List, false);
        scoreRepository.saveAll(six4_4List);

        // 6.4.5
        TemplateScore sixth4_5 = new TemplateScore();
        sixth4_5.setTemplateId(tempId);
        sixth4_5.setItemName("6.4.5");
        sixth4_5.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth4_5.setContent("加工工位以及检验工位是否满足具体的要求？");
        sixth4_5.setIsSpecial(false);
        sixth4_5.setIsNeed(false);
        sixth4_5.setIsActive(true);
        sixth4_5.setPid(pSixth4.getId());
        TemplateScore lSixth4_5 = scoreRepository.save(sixth4_5);
        List<TemplateScore> six4_5List = new ArrayList<>();
        initLineIsSpecial(tempId, lines, lSixth4_5, six4_5List, false);
        scoreRepository.saveAll(six4_5List);

        // P6.5 有效性、效率、减少浪费
        TemplateScore sixth5 = new TemplateScore();
        sixth5.setTemplateId(tempId);
        sixth5.setItemName("6.5");
        sixth5.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth5.setContent("过程实现的效果如何？（有效性、效率、减少浪费）");
        sixth5.setIsSpecial(false);
        sixth5.setIsNeed(false);
        sixth5.setIsActive(true);
        sixth5.setPid(rSixth.getId());
        TemplateScore pSixth5 = scoreRepository.save(sixth5);

        // 6.5.1
        TemplateScore sixth5_1 = new TemplateScore();
        sixth5_1.setTemplateId(tempId);
        sixth5_1.setItemName("6.5.1");
        sixth5_1.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth5_1.setContent("是否为制造过程设定目标要求？");
        sixth5_1.setIsSpecial(false);
        sixth5_1.setIsNeed(false);
        sixth5_1.setIsActive(true);
        sixth5_1.setPid(pSixth5.getId());
        TemplateScore lSixth5_1 = scoreRepository.save(sixth5_1);
        List<TemplateScore> six5_1List = new ArrayList<>();
        initLineIsSpecial(tempId, lines, lSixth5_1, six5_1List, false);
        scoreRepository.saveAll(six5_1List);

        // 6.5.2
        TemplateScore sixth5_2 = new TemplateScore();
        sixth5_2.setTemplateId(tempId);
        sixth5_2.setItemName("6.5.2");
        sixth5_2.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth5_2.setContent("对收集的质量和过程数据是否可以开展评价？");
        sixth5_2.setIsSpecial(false);
        sixth5_2.setIsNeed(false);
        sixth5_2.setIsActive(true);
        sixth5_2.setPid(pSixth5.getId());
        TemplateScore lSixth5_2 = scoreRepository.save(sixth5_2);
        List<TemplateScore> six5_2List = new ArrayList<>();
        initLineIsSpecial(tempId, lines, lSixth5_2, six5_2List, false);
        scoreRepository.saveAll(six5_2List);

        // 6.5.3
        TemplateScore sixth5_3 = new TemplateScore();
        sixth5_3.setTemplateId(tempId);
        sixth5_3.setItemName("6.5.3");
        sixth5_3.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth5_3.setContent("一旦与产品和过程要求不符，是否对原因进行分析，并且检验纠正措施的有效性？");
        sixth5_3.setIsSpecial(true);
        sixth5_3.setIsNeed(false);
        sixth5_3.setIsActive(true);
        sixth5_3.setPid(pSixth5.getId());
        TemplateScore lSixth5_3 = scoreRepository.save(sixth5_3);
        List<TemplateScore> six5_3List = new ArrayList<>();
        initLineIsSpecial(tempId, lines, lSixth5_3, six5_3List, true);
        scoreRepository.saveAll(six5_3List);

        // 6.5.4
        TemplateScore sixth5_4 = new TemplateScore();
        sixth5_4.setTemplateId(tempId);
        sixth5_4.setItemName("6.5.4");
        sixth5_4.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth5_4.setContent("对过程和产品是否定期开展审核？");
        sixth5_4.setIsSpecial(false);
        sixth5_4.setIsNeed(false);
        sixth5_4.setIsActive(true);
        sixth5_4.setPid(pSixth5.getId());
        TemplateScore lSixth5_4 = scoreRepository.save(sixth5_4);
        List<TemplateScore> six5_4List = new ArrayList<>();
        initLineIsSpecial(tempId, lines, lSixth5_4, six5_4List, false);
        scoreRepository.saveAll(six5_4List);

        // P6.6 过程成果/输出
        TemplateScore sixth6 = new TemplateScore();
        sixth6.setTemplateId(tempId);
        sixth6.setItemName("6.6");
        sixth6.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth6.setContent("过程应取得怎样的成果？过程成果/输出）");
        sixth6.setIsSpecial(false);
        sixth6.setIsNeed(false);
        sixth6.setIsActive(true);
        sixth6.setPid(rSixth.getId());
        TemplateScore pSixth6 = scoreRepository.save(sixth6);

        // 6.6.1
        TemplateScore sixth6_1 = new TemplateScore();
        sixth6_1.setTemplateId(tempId);
        sixth6_1.setItemName("6.6.1");
        sixth6_1.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth6_1.setContent("产量/生产批量是否是根据需要确定的，并且有目的地运往下道工序？");
        sixth6_1.setIsSpecial(false);
        sixth6_1.setIsNeed(false);
        sixth6_1.setIsActive(true);
        sixth6_1.setPid(pSixth6.getId());
        TemplateScore lSixth6_1 = scoreRepository.save(sixth6_1);
        List<TemplateScore> six6_1List = new ArrayList<>();
        initLineIsSpecial(tempId, lines, lSixth6_1, six6_1List, false);
        scoreRepository.saveAll(six6_1List);

        // 6.6.2
        TemplateScore sixth6_2 = new TemplateScore();
        sixth6_2.setTemplateId(tempId);
        sixth6_2.setItemName("6.6.2");
        sixth6_2.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth6_2.setContent("是否根据要求对产品/零部件进行适当仓储，所使用的运输设备/包装方式是否与产品/零部件的特殊特性相互适应？");
        sixth6_2.setIsSpecial(false);
        sixth6_2.setIsNeed(false);
        sixth6_2.setIsActive(true);
        sixth6_2.setPid(pSixth6.getId());
        TemplateScore lSixth6_2 = scoreRepository.save(sixth6_2);
        List<TemplateScore> six6_2List = new ArrayList<>();
        initLineIsSpecial(tempId, lines, lSixth6_2, six6_2List, false);
        scoreRepository.saveAll(six6_2List);

        // 6.6.3
        TemplateScore sixth6_3 = new TemplateScore();
        sixth6_3.setTemplateId(tempId);
        sixth6_3.setItemName("6.6.3");
        sixth6_3.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth6_3.setContent("是否对必要的记录和放行进行文件记录？");
        sixth6_3.setIsSpecial(false);
        sixth6_3.setIsNeed(false);
        sixth6_3.setIsActive(true);
        sixth6_3.setPid(pSixth6.getId());
        TemplateScore lSixth6_3 = scoreRepository.save(sixth6_3);
        List<TemplateScore> six6_3List = new ArrayList<>();
        initLineIsSpecial(tempId, lines, lSixth6_3, six6_3List, false);
        scoreRepository.saveAll(six6_3List);

        // 6.6.4
        TemplateScore sixth6_4 = new TemplateScore();
        sixth6_4.setTemplateId(tempId);
        sixth6_4.setItemName("6.6.4");
        sixth6_4.setItemType(CommonConstants.TEMPLATE_QUES_P6);
        sixth6_4.setContent("成品的交付方面是否满足顾客要求？");
        sixth6_4.setIsSpecial(true);
        sixth6_4.setIsNeed(false);
        sixth6_4.setIsActive(true);
        sixth6_4.setPid(pSixth6.getId());
        TemplateScore lSixth6_4 = scoreRepository.save(sixth6_4);
        List<TemplateScore> six6_4List = new ArrayList<>();
        initLineIsSpecial(tempId, lines, lSixth6_4, six6_4List, true);
        scoreRepository.saveAll(six6_4List);
    }

    private void initLineIsSpecial(Long tempId, List<String> lines, TemplateScore lSixth1_5, List<TemplateScore> six1_5List, boolean b) {
        lines.forEach(line -> {
            TemplateScore score = new TemplateScore();
            score.setTemplateId(tempId);
            score.setItemName("过程" + (lines.indexOf(line) + 1));
            score.setItemType(CommonConstants.TEMPLATE_QUES_P6);
            score.setContent(line);
            score.setIsSpecial(b);
            score.setIsNeed(true);
            score.setIsActive(true);
            score.setPid(lSixth1_5.getId());
            six1_5List.add(score);
        });
    }

}
