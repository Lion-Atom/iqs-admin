package me.zhengjie.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import lombok.RequiredArgsConstructor;
import me.zhengjie.base.CommonDTO;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.*;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.repository.*;
import me.zhengjie.utils.CommonUtils;
import me.zhengjie.service.IssueService;
import me.zhengjie.service.TeamMemberService;
import me.zhengjie.service.dto.IssueDto;
import me.zhengjie.service.dto.IssueQueryCriteria;
import me.zhengjie.service.mapstruct.IssueMapper;
import me.zhengjie.utils.*;
import org.springframework.cache.annotation.CacheConfig;
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
 * @date 2021/7/21 18:08
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "issue")
//@DS("self")
public class IssueServiceImpl implements IssueService {

    private final IssueRepository issueRepository;
    private final IssueMapper issueMapper;
    private final RedisUtils redisUtils;
    private final TimeManagementRepository timeMangeRepository;
    private final TeamRepository teamRepository;
    private final TeamMembersRepository teamMembersRepository;
    private final StepDefectRepository stepDefectRepository;
    private final ChangeDescRepository changeDescRepository;
    private final IssueConclusionRepository conclusionRepository;
    private final TeamMemberService teamMemberService;
    private final ConActionRepository conActionRepository;
    private final IssueCauseRepository issueCauseRepository;
    private final IssueAnalysisRepository issueAnalysisRepository;
    private final IssueQuestionRepository issueQuestionRepository;
    private final IssueNumRepository issueNumRepository;
    private final IssueFileRepository issueFileRepository;
    private final IssueActionRepository issueActionRepository;
    private final IssueSpecailRepository issueSpecailRepository;
    private final CommonUtils commonUtils;
    private final PreTrailRepository preTrailRepository;
    private final IssueScoreRepository issueScoreRepository;

    @Override
//    @Cacheable(key = "'id:' + #p0")
    public IssueDto findById(Long id) {
        IssueDto dto = new IssueDto();
        Issue issue = issueRepository.findById(id).orElseGet(Issue::new);
        ValidationUtil.isNull(issue.getId(), "Issue", "id", id);
        List<IssueFile> issueFiles = issueFileRepository.findComFileByStepNameAndIssueId(id, "D0");
        dto = issueMapper.toDto(issue);
        dto.setFileList(issueFiles);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(IssueDto resources) {
        Issue issue = issueRepository.findByIssueTitle(resources.getIssueTitle());
        // 重名校验
        if (issue != null) {
            throw new EntityExistException(Issue.class, "issueTitle", resources.getIssueTitle());
        }
        // 生成8D编码：年月日+序列号(今日批次)
        Date date = new Date();
        List<Issue> list = issueRepository.getIssueByCreateTime();
        int num = 1;
        num += list.size();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        if (num > 9) {
            resources.setEncodeNum(df.format(date) + "-00" + num);
        } else {
            resources.setEncodeNum(df.format(date) + "-000" + num);
        }
        resources.setStatus(CommonConstants.D_STATUS_AUDIT);
        Issue newIssue = issueRepository.save(issueMapper.toEntity(resources));

        // 添加文件
        if (ValidationUtil.isNotEmpty(resources.getFileList())) {
            resources.getFileList().forEach(file -> {
                file.setIssueId(newIssue.getId());
            });
            issueFileRepository.saveAll(resources.getFileList());
        }

        //创建审批任务给质量部Master
        PreTrail preTrail = new PreTrail();
        preTrail.setPreTrailNo(createNoFormat());
        preTrail.setStorageId(newIssue.getId());
        preTrail.setSrcPath(CommonConstants.IS_BLANK);
        preTrail.setTarPath(CommonConstants.IS_BLANK);
        preTrail.setSuffix(CommonConstants.IS_BLANK);
        preTrail.setVersion(CommonConstants.IS_BLANK);
        preTrail.setSize(CommonConstants.IS_BLANK);
        preTrail.setType(CommonConstants.TRAIL_TYPE_8D);
        preTrail.setRealName(newIssue.getIssueTitle());
        preTrail.setChangeDesc("新建8D：[" + newIssue.getIssueTitle() + "]待审批");
        preTrail.setIsDel(CommonConstants.NOT_DEL);
        // 指定审批人
        preTrail.setApprovedBy(commonUtils.getZlbMaster());
        preTrailRepository.save(preTrail);
    }

    private String createNoFormat() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmssS");
        return StringUtils.getPinyin(SecurityUtils.getCurrentDeptName()) + "-" + format.format(date);
    }

    @Override
//    @CacheEvict(key = "'id:' + #p0.id")
    @Transactional(rollbackFor = Exception.class)
    public void update(Issue resources) {
        // 包含审批
        Issue issue = issueRepository.findById(resources.getId()).orElseGet(Issue::new);
        Issue old = issueRepository.findByIssueTitle(resources.getIssueTitle());
        if (old != null && !old.getId().equals(resources.getId())) {
            throw new EntityExistException(Issue.class, "issueTitle", resources.getIssueTitle());
        }
        ValidationUtil.isNull(issue.getId(), "Issue", "id", resources.getId());
        resources.setId(issue.getId());
        // 权限校验
        teamMemberService.checkEditAuthorized(issue.getId());
        // 是否执行8D
        if (resources.getHasReport() != null) {
            if (resources.getHasReport().equals(CommonConstants.EXECUTE_8D)) {

                // 创建8D则进入待进行状态
                if (issue.getHasReport() == null || !issue.getHasReport().equals(CommonConstants.EXECUTE_8D)) {
                    resources.setStatus(CommonConstants.D_STATUS_OPEN);
                }
                resources.setCloseTime(null);
                resources.setDuration(null);

                //  初始化数据-初始化之前都需要先行判断是否已经初始化了
                // 判断是否已经初始化
                String issueTitle = issue.getIssueTitle();
                String teamName = "teamFor_" + issueTitle;
                // 1.时间进程的判断
                TimeManagement man = timeMangeRepository.findByIssueId(issue.getId());
                if (man == null) {
                    // 1.初始化时间进程
                    TimeManagement management = new TimeManagement();
                    management.setIssueId(issue.getId());
                    timeMangeRepository.save(management);
                } else {
                    resources.setStatus(CommonConstants.D_STATUS_IN_PROCESS);
                }

                // 2.成立小组--审核人默认为组长,重做
                judgeLeader(resources, issue, teamName);

                // 2.5 D2-详细描述新增
                initIssueQuestion(issue);

                // 3.D4-初始化过程缺陷定位数据
                //初始化数据
                initStepDefect(issue);

                // 4.D3-围堵措施
                initConAction(issue);

                // 5.D4-人机料法环根本原因的初始化
                initIssueCause(issue);

                // 6.初始化D7文档描述
                initDesc(issue);

                // 7.初始化D7过程分析
                initAnalysis(issue);

                // 8.初始化D8各方意见
                IssueConclusion conclusion = conclusionRepository.findByIssueId(issue.getId());
                if (conclusion == null) {
                    IssueConclusion issueConclusion = new IssueConclusion();
                    issueConclusion.setIssueId(issue.getId());
                    conclusionRepository.save(issueConclusion);
                }

                // 8.初始化8D评分
                if (resources.getHasScore()) {
                    initIssueScore(issue);
                    // 首次初始化，D8分数初始化为及格分数
                    if (issue.getHasScore() == null || !issue.getHasScore()) {
                        resources.setScore(70);
                    }
                } else {
                    // 重置8D分数
                    resources.setScore(null);
                }

                // D3-临时文件变更,若不存在临时文件需要移除原有的临时文件
                if (!resources.getHasTempFile()) {
                    issueFileRepository.deleteTempByIssueIdAndStepName(resources.getId(), CommonConstants.D_STEP_D3);
                }

                // 各步骤描述或风险评估变更引发变更步骤状态,前端已做监控


            } else {
                // 若不执行8D则清空8D相关所有数据
                if (resources.getHasReport().equals(CommonConstants.EXECUTE_SINGLE_REPORT)) {
                    // 已完成状态，则计算时长
                    if (resources.getStatus().equals(CommonConstants.D_STATUS_DONE)) {
                        resources.setDuration(ValidationUtil.getDuration(issue.getCreateTime()));
                    } else {
                        resources.setHasScore(null);
                        resources.setCloseTime(null);
                        resources.setDuration(null);
                        resources.setStatus(CommonConstants.D_STATUS_IN_PROCESS);
                    }
                } else if (resources.getHasReport().equals(CommonConstants.EXECUTE_CLOSE)) {
                    resources.setHasScore(null);
                    resources.setCloseTime(null);
                    resources.setDuration(null);
                    resources.setStatus(CommonConstants.D_STATUS_CLOSE);
                }
                // 清除8D问题痕迹
                issueRepository.clearByIssueId(issue.getId(), new Timestamp(new Date().getTime()));
                // 删除时间进程
                timeMangeRepository.deleteByIssueId(issue.getId());
                // 删除小组
                teamRepository.deleteByIssueId(issue.getId());
                // 删除小组组员
                teamMembersRepository.deleteByIssueId(issue.getId());
                // 删除相关数据
                issueNumRepository.deleteByIssueId(issue.getId());
                // 删除D2的数据
                issueQuestionRepository.deleteByIssueId(issue.getId());
                // 删除围堵措施信息
                conActionRepository.deleteByIssueId(issue.getId());
                // 删除缺陷定位
                stepDefectRepository.deleteByIssueId(issue.getId());
                // 删除原因
                issueCauseRepository.deleteByIssueId(issue.getId());
                // 删除D7文档描述
                changeDescRepository.deleteByIssueId(issue.getId());
                // 删除D7过程分析
                issueAnalysisRepository.deleteByIssueId(issue.getId());
                // 删除D8各方意见
                conclusionRepository.deleteByIssueId(issue.getId());
                // 删除审核之外的所有附件信息
                issueFileRepository.deleteByIssueIdAndInStepNames(issue.getId(), CommonConstants.D_STEP_LIST);
                // 删除所有措施
                issueActionRepository.deleteByIssueIdAndInStepNames(issue.getId(), CommonConstants.D_STEP_LIST);
                // 删除特殊事件
                issueSpecailRepository.deleteByIssueId(issue.getId());
            }
            // 更新审批状态
            PreTrail preTrail = preTrailRepository.findAllByIssueIdAndType(issue.getId(), CommonConstants.NOT_DEL, CommonConstants.TRAIL_TYPE_8D);
            if (preTrail != null && !preTrail.getIsDone()) {
                preTrail.setIsDone(true);
                preTrail.setApproveResult(true);
                preTrailRepository.save(preTrail);
            }
        } else {
            // 非审核
            if (resources.getStatus().equals(CommonConstants.D_STATUS_REJECT)) {
                // 更新审批状态
                PreTrail preTrail = preTrailRepository.findAllByIssueIdAndType(issue.getId(), CommonConstants.NOT_DEL, CommonConstants.TRAIL_TYPE_8D);
                if (preTrail != null && !preTrail.getIsDone()) {
                    if (resources.getReason() != null) {
                        preTrail.setApproveResult(false);
                        preTrail.setComment(resources.getReason());
                    } else {
                        preTrail.setApproveResult(true);
                        preTrail.setComment(null);
                    }
                    preTrail.setIsDone(true);
                    preTrailRepository.save(preTrail);
                }
            }
        }
        if (!resources.getStatus().equals(CommonConstants.D_STATUS_REJECT)) {
            resources.setReason(null);
        }
        issueRepository.save(resources);
    }


    private void judgeLeader(Issue resources, Issue issue, String teamName) {
        Team oldTeam = teamRepository.findByIssueId(issue.getId());
        if (oldTeam == null) {
            // 未成立小组
            Team team = new Team();
            team.setIssueId(issue.getId());
            team.setLeaderId(resources.getLeaderId());
            team.setName(teamName);
            Team newTeam = teamRepository.save(team);
            // 设立初始成员
            List<TeamMember> members = new ArrayList<>();
            TeamMember leader = new TeamMember();
            leader.setIsLeader(true);
            leader.setIssueId(issue.getId());
            leader.setTeamId(newTeam.getId());
            leader.setUserId(resources.getLeaderId());
            leader.setTeamRole(CommonConstants.D_TEAM_ROLE_LEADER);

            members.add(leader);

            // 设置质量部master进去
            if (!resources.getLeaderId().equals(commonUtils.getZlbMaster())) {
                TeamMember manage = new TeamMember();
                manage.setIsLeader(false);
                manage.setIssueId(issue.getId());
                manage.setTeamId(newTeam.getId());
                manage.setUserId(commonUtils.getZlbMaster());
                manage.setTeamRole(CommonConstants.D_TEAM_ROLE_MANAGER);
                members.add(manage);
            }
            teamMembersRepository.saveAll(members);
        } else {
            // 如果组长改变了，前一个人如何自处？改为“管理层”
            Long oldLeader = oldTeam.getLeaderId();
            if (resources.getLeaderId() != null && !oldTeam.getLeaderId().equals(resources.getLeaderId())) {
                // 已成立小组
                oldTeam.setLeaderId(resources.getLeaderId());
                teamRepository.save(oldTeam);

                // 重新设立初始成员
                List<TeamMember> members = new ArrayList<>();
                // 组长与管理者
                // 设置组长
                TeamMember newLeader = teamMembersRepository.findByIssueIdAndTeamIdAndUserId(issue.getId(), oldTeam.getId(), resources.getLeaderId());
                if (newLeader != null) {
                    newLeader.setTeamRole(CommonConstants.D_TEAM_ROLE_LEADER);
                    members.add(newLeader);
                } else {
                    TeamMember leader = new TeamMember();
                    leader.setIsLeader(true);
                    leader.setIssueId(issue.getId());
                    leader.setTeamId(oldTeam.getId());
                    leader.setUserId(resources.getLeaderId());
                    leader.setTeamRole(CommonConstants.D_TEAM_ROLE_LEADER);
                    members.add(leader);
                }

                TeamMember manager = teamMembersRepository.findByIssueIdAndTeamIdAndUserId(issue.getId(), oldTeam.getId(), oldLeader);
                if (manager != null) {
                    manager.setIsLeader(false);
                    manager.setTeamRole(CommonConstants.D_TEAM_ROLE_MANAGER);
                    members.add(manager);
                }
                teamMembersRepository.saveAll(members);
            }
        }
    }

    private void initAnalysis(Issue issue) {
        List<IssueAnalysis> issueAnalysisList = issueAnalysisRepository.findByIssueId(issue.getId());
        if (ValidationUtil.isEmpty(issueAnalysisList)) {
            List<IssueAnalysis> analysisList = new ArrayList<>();
            IssueAnalysis analysis1 = new IssueAnalysis();
            analysis1.setIssueId(issue.getId());
            analysis1.setSystemWide(CommonConstants.ANALYSIS_PART_1);
            analysis1.setSystemNum(1L);

            IssueAnalysis analysis2 = new IssueAnalysis();
            analysis2.setIssueId(issue.getId());
            analysis2.setSystemWide(CommonConstants.ANALYSIS_PART_2);
            analysis2.setSystemNum(2L);

            IssueAnalysis analysis3 = new IssueAnalysis();
            analysis3.setIssueId(issue.getId());
            analysis3.setSystemWide(CommonConstants.ANALYSIS_PART_3);
            analysis3.setSystemNum(3L);

            analysisList.add(analysis1);
            analysisList.add(analysis2);
            analysisList.add(analysis3);

            issueAnalysisRepository.saveAll(analysisList);

        }
    }

    private void initIssueQuestion(Issue issue) {
        List<IssueQuestion> questions = issueQuestionRepository.findByIssueIdAndType(issue.getId(), CommonConstants.QUESTION_5W2H);
        List<IssueQuestion> questionList = new ArrayList<>();
        if (ValidationUtil.isEmpty(questions)) {
            // 初始化5W2H
            IssueQuestion when = new IssueQuestion();
            when.setIssueId(issue.getId());
            when.setName(CommonConstants.QUESTION_WHEN);
            when.setType(CommonConstants.QUESTION_5W2H);

            IssueQuestion where = new IssueQuestion();
            where.setIssueId(issue.getId());
            where.setName(CommonConstants.QUESTION_WHERE);
            where.setType(CommonConstants.QUESTION_5W2H);

            IssueQuestion what = new IssueQuestion();
            what.setIssueId(issue.getId());
            what.setName(CommonConstants.QUESTION_WHAT);
            what.setType(CommonConstants.QUESTION_5W2H);

            IssueQuestion who = new IssueQuestion();
            who.setIssueId(issue.getId());
            who.setName(CommonConstants.QUESTION_WHO);
            who.setType(CommonConstants.QUESTION_5W2H);

            IssueQuestion why = new IssueQuestion();
            why.setIssueId(issue.getId());
            why.setName(CommonConstants.QUESTION_WHY);
            why.setType(CommonConstants.QUESTION_5W2H);

            IssueQuestion how = new IssueQuestion();
            how.setIssueId(issue.getId());
            how.setName(CommonConstants.QUESTION_HOW);
            how.setType(CommonConstants.QUESTION_5W2H);

            IssueQuestion howMany = new IssueQuestion();
            howMany.setIssueId(issue.getId());
            howMany.setName(CommonConstants.QUESTION_HOW_MANY);
            howMany.setType(CommonConstants.QUESTION_5W2H);

            questionList.add(when);
            questionList.add(where);
            questionList.add(what);
            questionList.add(who);
            questionList.add(why);
            questionList.add(how);
            questionList.add(howMany);

        }
        List<IssueQuestion> isNotList = issueQuestionRepository.findByIssueIdAndType(issue.getId(), CommonConstants.QUESTION_IS_ISNOT);
        if (ValidationUtil.isEmpty(isNotList)) {
            // 初始化IS/IS Not
            IssueQuestion what1 = new IssueQuestion();
            what1.setIssueId(issue.getId());
            what1.setName(CommonConstants.QUESTION_WHAT_IS);
            what1.setDescription(CommonConstants.QUESTION_WHAT_IS_OBJECT);
            what1.setType(CommonConstants.QUESTION_IS_ISNOT);

            IssueQuestion what2 = new IssueQuestion();
            what2.setIssueId(issue.getId());
            what2.setName(CommonConstants.QUESTION_WHAT_IS);
            what2.setDescription(CommonConstants.QUESTION_WHAT_IS_DEFECT);
            what2.setType(CommonConstants.QUESTION_IS_ISNOT);

            IssueQuestion where1 = new IssueQuestion();
            where1.setIssueId(issue.getId());
            where1.setName(CommonConstants.QUESTION_WHERE_IS);
            where1.setDescription(CommonConstants.QUESTION_WHERE_IS_POSITION);
            where1.setType(CommonConstants.QUESTION_IS_ISNOT);

            IssueQuestion where2 = new IssueQuestion();
            where2.setIssueId(issue.getId());
            where2.setName(CommonConstants.QUESTION_WHERE_IS);
            where2.setDescription(CommonConstants.QUESTION_WHERE_IS_WHERE);
            where2.setType(CommonConstants.QUESTION_IS_ISNOT);

            IssueQuestion when1 = new IssueQuestion();
            when1.setIssueId(issue.getId());
            when1.setName(CommonConstants.QUESTION_WHEN_IS);
            when1.setDescription(CommonConstants.QUESTION_WHEN_IS_WHEN);
            when1.setType(CommonConstants.QUESTION_IS_ISNOT);

            IssueQuestion when2 = new IssueQuestion();
            when2.setIssueId(issue.getId());
            when2.setName(CommonConstants.QUESTION_WHEN_IS);
            when2.setDescription(CommonConstants.QUESTION_WHERE_IS_PATTERN);
            when2.setType(CommonConstants.QUESTION_IS_ISNOT);

            IssueQuestion extent1 = new IssueQuestion();
            extent1.setIssueId(issue.getId());
            extent1.setName(CommonConstants.QUESTION_EXTENT_IS);
            extent1.setDescription(CommonConstants.QUESTION_EXTENT_IS_QT);
            extent1.setType(CommonConstants.QUESTION_IS_ISNOT);

            questionList.add(what1);
            questionList.add(what2);
            questionList.add(where1);
            questionList.add(where2);
            questionList.add(when1);
            questionList.add(when2);
            questionList.add(extent1);
        }
        if (ValidationUtil.isNotEmpty(questionList)) {
            issueQuestionRepository.saveAll(questionList);
        }
    }

    private void initDesc(Issue issue) {
        List<ChangeDesc> descs = changeDescRepository.findByIssueId(issue.getId());
        if (ValidationUtil.isEmpty(descs)) {
            List<ChangeDesc> changeDescList = new ArrayList<>();
            ChangeDesc planDesc = new ChangeDesc();
            planDesc.setIssueId(issue.getId());
            planDesc.setName(CommonConstants.D7_CHANGE_DESC);

            ChangeDesc others = new ChangeDesc();
            others.setIssueId(issue.getId());
            others.setName(CommonConstants.D7_OTHERS_DESC);

            changeDescList.add(planDesc);
            changeDescList.add(others);
            changeDescRepository.saveAll(changeDescList);
        }
    }

    private void initIssueCause(Issue issue) {
        List<IssueCause> causeList = issueCauseRepository.findByIssueId(issue.getId());
        if (ValidationUtil.isEmpty(causeList)) {
            List<IssueCause> causes = new ArrayList<>();
            // 机器
            IssueCause machine = new IssueCause();
            machine.setIssueId(issue.getId());
            machine.setName(CommonConstants.CAUSE_MACHINE);
            machine.setContribution(0d);
            // 人员
            IssueCause manpower = new IssueCause();
            manpower.setIssueId(issue.getId());
            manpower.setName(CommonConstants.CAUSE_MANPOWER);
            manpower.setContribution(0d);
            // 材料
            IssueCause material = new IssueCause();
            material.setIssueId(issue.getId());
            material.setName(CommonConstants.CAUSE_MATERIAL);
            material.setContribution(0d);
            // 方法
            IssueCause method = new IssueCause();
            method.setIssueId(issue.getId());
            method.setName(CommonConstants.CAUSE_METHOD);
            method.setContribution(0d);
            // 环境
            IssueCause environment = new IssueCause();
            environment.setIssueId(issue.getId());
            environment.setName(CommonConstants.CAUSE_ENVIRONMENT);
            environment.setContribution(0d);

            causes.add(machine);
            causes.add(manpower);
            causes.add(material);
            causes.add(method);
            causes.add(environment);

            issueCauseRepository.saveAll(causes);
        }
    }

    private void initStepDefect(Issue issue) {
        List<StepDefect> defects = stepDefectRepository.findByIssueId(issue.getId());
        if (ValidationUtil.isEmpty(defects)) {
            List<StepDefect> steps = new ArrayList<>();

            StepDefect step1 = new StepDefect();
            step1.setIssueId(issue.getId());
            step1.setProcessStep("在供应商端");

            StepDefect step2 = new StepDefect();
            step2.setIssueId(issue.getId());
            step2.setProcessStep("在制造过程中");

            StepDefect step3 = new StepDefect();
            step3.setIssueId(issue.getId());
            step3.setProcessStep("在客户端");

            StepDefect step4 = new StepDefect();
            step4.setIssueId(issue.getId());
            step4.setProcessStep("其他过程中");

            steps.add(step1);
            steps.add(step2);
            steps.add(step3);
            steps.add(step4);

            stepDefectRepository.saveAll(steps);
        }
    }

    private void initIssueScore(Issue issue) {
        List<IssueScore> scoreList = issueScoreRepository.findByIssueId(issue.getId());
        if (ValidationUtil.isEmpty(scoreList)) {
            List<IssueScore> scores = new ArrayList<>();

            // D1 打分
            IssueScore score1 = new IssueScore();
            score1.setIssueId(issue.getId());
            score1.setName(CommonConstants.D_STEP_D1);
            score1.setContent("问题解决团队");
            score1.setScoreType(CommonConstants.SCORE_TYPE_FIVE);

            IssueScore score2 = new IssueScore();
            score2.setIssueId(issue.getId());
            score2.setName(CommonConstants.D_STEP_D2);
            score2.setContent("问题描述");
            score2.setScoreType(CommonConstants.SCORE_TYPE_SEVEN);

            IssueScore score3 = new IssueScore();
            score3.setIssueId(issue.getId());
            score3.setName(CommonConstants.D_STEP_D3);
            score3.setContent("遏制措施");
            score3.setScoreType(CommonConstants.SCORE_TYPE_TEN);

            IssueScore score4 = new IssueScore();
            score4.setIssueId(issue.getId());
            score4.setName(CommonConstants.D_STEP_D4);
            score4.setContent("定义和确认根本原因");
            score4.setScoreType(CommonConstants.SCORE_TYPE_TEN);

            IssueScore score5 = new IssueScore();
            score5.setIssueId(issue.getId());
            score5.setName(CommonConstants.D_STEP_D4);
            score5.setContent("定义和验证非探测性纠正措施");
            score5.setScoreType(CommonConstants.SCORE_TYPE_TEN);

            IssueScore score6 = new IssueScore();
            score6.setIssueId(issue.getId());
            score6.setName(CommonConstants.D_STEP_D5);
            score6.setContent("选择和验证问题的纠正措施");
            score6.setScoreType(CommonConstants.SCORE_TYPE_TEN);

            IssueScore score7 = new IssueScore();
            score7.setIssueId(issue.getId());
            score7.setName(CommonConstants.D_STEP_D5);
            score7.setContent("选择和验证非探测性纠正措施");
            score7.setScoreType(CommonConstants.SCORE_TYPE_TEN);

            IssueScore score8 = new IssueScore();
            score8.setIssueId(issue.getId());
            score8.setName(CommonConstants.D_STEP_D6);
            score8.setContent("问题纠正措施的实施");
            score8.setScoreType(CommonConstants.SCORE_TYPE_TEN);

            IssueScore score9 = new IssueScore();
            score9.setIssueId(issue.getId());
            score9.setName(CommonConstants.D_STEP_D6);
            score9.setContent("非探测性纠正措施的实施");
            score9.setScoreType(CommonConstants.SCORE_TYPE_TEN);

            IssueScore score10 = new IssueScore();
            score10.setIssueId(issue.getId());
            score10.setName(CommonConstants.D_STEP_D7);
            score10.setContent("防止再发生的措施");
            score10.setScoreType(CommonConstants.SCORE_TYPE_TEN);

            IssueScore score11 = new IssueScore();
            score11.setIssueId(issue.getId());
            score11.setName(CommonConstants.D_STEP_D8);
            score11.setContent("关闭8D");
            score11.setScoreType(CommonConstants.SCORE_TYPE_FIVE);

            IssueScore score12 = new IssueScore();
            score12.setIssueId(issue.getId());
            score12.setName(" ");
            score12.setContent("报告");
            score12.setScoreType(CommonConstants.SCORE_TYPE_THREE);

            scores.add(score1);
            scores.add(score2);
            scores.add(score3);
            scores.add(score4);
            scores.add(score5);
            scores.add(score6);
            scores.add(score7);
            scores.add(score8);
            scores.add(score9);
            scores.add(score10);
            scores.add(score11);
            scores.add(score12);

            issueScoreRepository.saveAll(scores);

        }
    }

    private void initConAction(Issue issue) {
        List<ConAction> conActionList = conActionRepository.findByIssueId(issue.getId());
        if (ValidationUtil.isEmpty(conActionList)) {
            List<ConAction> conActs = new ArrayList<>();

            ConAction conAct1 = new ConAction();
            conAct1.setIssueId(issue.getId());
            conAct1.setTitle("供应商");
            conActs.add(conAct1);

            ConAction conAct2 = new ConAction();
            conAct2.setIssueId(issue.getId());
            conAct2.setTitle("供应商运输途中");
            conActs.add(conAct2);

            ConAction conAct3 = new ConAction();
            conAct3.setIssueId(issue.getId());
            conAct3.setTitle("收料仓库");
            conActs.add(conAct3);

            ConAction conAct4 = new ConAction();
            conAct4.setIssueId(issue.getId());
            conAct4.setTitle("生产线");
            conActs.add(conAct4);

            ConAction conAct5 = new ConAction();
            conAct5.setIssueId(issue.getId());
            conAct5.setTitle("成品仓库");
            conActs.add(conAct5);

            ConAction conAct6 = new ConAction();
            conAct6.setIssueId(issue.getId());
            conAct6.setTitle("出货运输途中");
            conActs.add(conAct6);

            ConAction conAct7 = new ConAction();
            conAct7.setIssueId(issue.getId());
            conAct7.setTitle("客户仓库");
            conActs.add(conAct7);

            ConAction conAct8 = new ConAction();
            conAct8.setIssueId(issue.getId());
            conAct8.setTitle("客户产线");
            conActs.add(conAct8);

            ConAction conAct9 = new ConAction();
            conAct9.setIssueId(issue.getId());
            conAct9.setTitle("其他");
            conActs.add(conAct9);

            conActionRepository.saveAll(conActs);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        // 权限校验
//        ids.forEach(teamMemberService::checkEditAuthorized);
        // 普通组员无权删除
        ids.forEach(teamMemberService::checkSubmitAuthorized);
        issueRepository.deleteAllByIdIn(ids);
        // 删除8D-问题相关数据
        ids.forEach(id -> {
            // 删除时间进程
            timeMangeRepository.deleteByIssueId(id);
            // 删除小组
            teamRepository.deleteByIssueId(id);
            // 删除小组组员
            teamMembersRepository.deleteByIssueId(id);
            // 删除相关数据
            issueNumRepository.deleteByIssueId(id);
            // 删除D2的数据
            issueQuestionRepository.deleteByIssueId(id);
            // 删除围堵措施信息
            conActionRepository.deleteByIssueId(id);
            // 删除缺陷定位
            stepDefectRepository.deleteByIssueId(id);
            // 删除原因
            issueCauseRepository.deleteByIssueId(id);
            // 删除D7文档描述
            changeDescRepository.deleteByIssueId(id);
            // 删除D7过程分析
            issueAnalysisRepository.deleteByIssueId(id);
            // 删除D8各方意见
            conclusionRepository.deleteByIssueId(id);
            // 删除所有附件信息
            issueFileRepository.deleteByIssueId(id);
            // 删除所有措施
            issueActionRepository.deleteByIssueId(id);
            // 删除特殊事件
            issueSpecailRepository.deleteByIssueId(id);
            // 删除D8打分分数分布信息
            issueScoreRepository.deleteByIssueId(id);
            // todo 删除未审批的关联任务
            preTrailRepository.deleteAllByStorageId(id);
        });
        // 删除缓存
        redisUtils.delByKeys(CacheKey.ISSUE_ID, ids);
    }

    @Override
    public Map<String, Object> queryAll(IssueQueryCriteria criteria, Pageable pageable) {
        if (criteria.getDuration() != null) {
            criteria.setDuration(criteria.getDuration() + "天");
        }
        Page<Issue> page = issueRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        Map<String, Object> map = new HashMap<>();
        List<IssueDto> list = new ArrayList<>();
        long total = 0L;
        if (ValidationUtil.isNotEmpty(page.getContent())) {
            list = issueMapper.toDto(page.getContent());
            list.forEach(issue -> {
                TimeManagement tm = timeMangeRepository.findByIssueId(issue.getId());
                Timestamp now = new Timestamp(new Date().getTime());
                List<CommonDTO> commonDTOList = new ArrayList<>();
                if (tm != null) {
                    // 1.初始化8D进程样式设置
                    CommonDTO d1 = new CommonDTO();
                    d1.setName(CommonConstants.D_STEP_D1);
                    d1.setValue(tm.getD1Status() ? CommonConstants.D_FORMAT_SUCCESS : CommonConstants.D_FORMAT_WAIT);

                    CommonDTO d2 = new CommonDTO();
                    d2.setName(CommonConstants.D_STEP_D2);
                    d2.setValue(tm.getD2Status() ? CommonConstants.D_FORMAT_SUCCESS : CommonConstants.D_FORMAT_WAIT);

                    CommonDTO d3 = new CommonDTO();
                    d3.setName(CommonConstants.D_STEP_D3);
                    d3.setValue(tm.getD3Status() ? CommonConstants.D_FORMAT_SUCCESS : CommonConstants.D_FORMAT_WAIT);

                    CommonDTO d4 = new CommonDTO();
                    d4.setName(CommonConstants.D_STEP_D4);
                    d4.setValue(tm.getD4Status() ? CommonConstants.D_FORMAT_SUCCESS : CommonConstants.D_FORMAT_WAIT);

                    CommonDTO d5 = new CommonDTO();
                    d5.setName(CommonConstants.D_STEP_D5);
                    d5.setValue(tm.getD5Status() ? CommonConstants.D_FORMAT_SUCCESS : CommonConstants.D_FORMAT_WAIT);

                    CommonDTO d6 = new CommonDTO();
                    d6.setName(CommonConstants.D_STEP_D6);
                    d6.setValue(tm.getD6Status() ? CommonConstants.D_FORMAT_SUCCESS : CommonConstants.D_FORMAT_WAIT);

                    CommonDTO d7 = new CommonDTO();
                    d7.setName(CommonConstants.D_STEP_D7);
                    d7.setValue(tm.getD7Status() ? CommonConstants.D_FORMAT_SUCCESS : CommonConstants.D_FORMAT_WAIT);

                    CommonDTO d8 = new CommonDTO();
                    d8.setName(CommonConstants.D_STEP_D8);
                    d8.setValue(tm.getD8Status() ? CommonConstants.D_FORMAT_SUCCESS : CommonConstants.D_FORMAT_WAIT);

                    commonDTOList.add(d1);
                    commonDTOList.add(d2);
                    commonDTOList.add(d3);
                    commonDTOList.add(d4);
                    if (ValidationUtil.isBlank(issue.getSpecialEvent())) {
                        // 若无特殊事件，则继续走D5-D7路线
                        commonDTOList.add(d5);
                        commonDTOList.add(d6);
                        commonDTOList.add(d7);
                    }
                    commonDTOList.add(d8);

                    // 2.超时变红
                    if (!ValidationUtil.isBlank(tm.getPlanStep1())) {

                        switch (tm.getPlanStep1()) {
                            case CommonConstants.D_STEP_D1:
                                if (tm.getD1Status()) {
                                    if (tm.getD1Time().getTime() > tm.getPlanTime1().getTime()) {
                                        d1.setOtherValue(CommonConstants.D_FORMAT_WARN);
                                    }
                                } else {
                                    if (now.getTime() > tm.getPlanTime1().getTime()) {
                                        d1.setValue(CommonConstants.D_FORMAT_ERROR);
                                    }
                                }

                                break;
                            case CommonConstants.D_STEP_D2:
                                if (tm.getD2Status()) {
                                    if (tm.getD2Time().getTime() > tm.getPlanTime1().getTime()) {
                                        d2.setOtherValue(CommonConstants.D_FORMAT_WARN);
                                    }
                                } else {
                                    if (now.getTime() > tm.getPlanTime1().getTime()) {
                                        d2.setValue(CommonConstants.D_FORMAT_ERROR);
                                    }
                                }
                                break;
                            case CommonConstants.D_STEP_D3:
                                if (tm.getD3Status()) {
                                    if (tm.getD3Time().getTime() > tm.getPlanTime1().getTime()) {
                                        d3.setOtherValue(CommonConstants.D_FORMAT_WARN);
                                    }
                                } else {
                                    if (now.getTime() > tm.getPlanTime1().getTime()) {
                                        d3.setValue(CommonConstants.D_FORMAT_ERROR);
                                    }
                                }
                                break;
                            case CommonConstants.D_STEP_D4:
                                if (tm.getD4Status()) {
                                    if (tm.getD4Time().getTime() > tm.getPlanTime1().getTime()) {
                                        d4.setOtherValue(CommonConstants.D_FORMAT_WARN);
                                    }
                                } else {
                                    if (now.getTime() > tm.getPlanTime1().getTime()) {
                                        d4.setValue(CommonConstants.D_FORMAT_ERROR);
                                    }
                                }
                                break;
                            case CommonConstants.D_STEP_D5:
                                if (tm.getD5Status()) {
                                    if (tm.getD5Time().getTime() > tm.getPlanTime1().getTime()) {
                                        d5.setOtherValue(CommonConstants.D_FORMAT_WARN);
                                    }
                                } else {
                                    if (now.getTime() > tm.getPlanTime1().getTime()) {
                                        d5.setValue(CommonConstants.D_FORMAT_ERROR);
                                    }
                                }
                                break;
                            case CommonConstants.D_STEP_D6:
                                if (tm.getD6Status()) {
                                    if (tm.getD6Time().getTime() > tm.getPlanTime1().getTime()) {
                                        d6.setOtherValue(CommonConstants.D_FORMAT_WARN);
                                    }
                                } else {
                                    if (now.getTime() > tm.getPlanTime1().getTime()) {
                                        d6.setValue(CommonConstants.D_FORMAT_ERROR);
                                    }
                                }
                                break;
                            case CommonConstants.D_STEP_D7:
                                if (tm.getD7Status()) {
                                    if (tm.getD7Time().getTime() > tm.getPlanTime1().getTime()) {
                                        d7.setOtherValue(CommonConstants.D_FORMAT_WARN);
                                    }
                                } else {
                                    if (now.getTime() > tm.getPlanTime1().getTime()) {
                                        d7.setValue(CommonConstants.D_FORMAT_ERROR);
                                    }
                                }
                                break;
                            case CommonConstants.D_STEP_D8:
                                if (tm.getD8Status()) {
                                    if (tm.getD8Time().getTime() > tm.getPlanTime1().getTime()) {
                                        d8.setOtherValue(CommonConstants.D_FORMAT_WARN);
                                    }
                                } else {
                                    if (now.getTime() > tm.getPlanTime1().getTime()) {
                                        d8.setValue(CommonConstants.D_FORMAT_ERROR);
                                    }
                                }
                                break;
                        }
                    }
                    // 预期二
                    if (!ValidationUtil.isBlank(tm.getPlanStep2())) {
                        switch (tm.getPlanStep2()) {
                            case CommonConstants.D_STEP_D1:
                                if (tm.getD1Status()) {
                                    if (tm.getD1Time().getTime() > tm.getPlanTime2().getTime()) {
                                        d1.setOtherValue(CommonConstants.D_FORMAT_WARN);
                                    }
                                } else {
                                    if (now.getTime() > tm.getPlanTime2().getTime()) {
                                        d1.setValue(CommonConstants.D_FORMAT_ERROR);
                                    }
                                }

                                break;
                            case CommonConstants.D_STEP_D2:
                                if (tm.getD2Status()) {
                                    if (tm.getD2Time().getTime() > tm.getPlanTime2().getTime()) {
                                        d2.setOtherValue(CommonConstants.D_FORMAT_WARN);
                                    }
                                } else {
                                    if (now.getTime() > tm.getPlanTime2().getTime()) {
                                        d2.setValue(CommonConstants.D_FORMAT_ERROR);
                                    }
                                }
                                break;
                            case CommonConstants.D_STEP_D3:
                                if (tm.getD3Status()) {
                                    if (tm.getD3Time().getTime() > tm.getPlanTime2().getTime()) {
                                        d3.setOtherValue(CommonConstants.D_FORMAT_WARN);
                                    }
                                } else {
                                    if (now.getTime() > tm.getPlanTime2().getTime()) {
                                        d3.setValue(CommonConstants.D_FORMAT_ERROR);
                                    }
                                }
                                break;
                            case CommonConstants.D_STEP_D4:
                                if (tm.getD4Status()) {
                                    if (tm.getD4Time().getTime() > tm.getPlanTime2().getTime()) {
                                        d4.setOtherValue(CommonConstants.D_FORMAT_WARN);
                                    }
                                } else {
                                    if (now.getTime() > tm.getPlanTime2().getTime()) {
                                        d4.setValue(CommonConstants.D_FORMAT_ERROR);
                                    }
                                }
                                break;
                            case CommonConstants.D_STEP_D5:
                                if (tm.getD5Status()) {
                                    if (tm.getD5Time().getTime() > tm.getPlanTime2().getTime()) {
                                        d5.setOtherValue(CommonConstants.D_FORMAT_WARN);
                                    }
                                } else {
                                    if (now.getTime() > tm.getPlanTime2().getTime()) {
                                        d5.setValue(CommonConstants.D_FORMAT_ERROR);
                                    }
                                }
                                break;
                            case CommonConstants.D_STEP_D6:
                                if (tm.getD6Status()) {
                                    if (tm.getD6Time().getTime() > tm.getPlanTime2().getTime()) {
                                        d6.setOtherValue(CommonConstants.D_FORMAT_WARN);
                                    }
                                } else {
                                    if (now.getTime() > tm.getPlanTime2().getTime()) {
                                        d6.setValue(CommonConstants.D_FORMAT_ERROR);
                                    }
                                }
                                break;
                            case CommonConstants.D_STEP_D7:
                                if (tm.getD7Status()) {
                                    if (tm.getD7Time().getTime() > tm.getPlanTime2().getTime()) {
                                        d7.setOtherValue(CommonConstants.D_FORMAT_WARN);
                                    }
                                } else {
                                    if (now.getTime() > tm.getPlanTime2().getTime()) {
                                        d7.setValue(CommonConstants.D_FORMAT_ERROR);
                                    }
                                }
                                break;
                            case CommonConstants.D_STEP_D8:
                                if (tm.getD8Status()) {
                                    if (tm.getD8Time().getTime() > tm.getPlanTime2().getTime()) {
                                        d8.setOtherValue(CommonConstants.D_FORMAT_WARN);
                                    }
                                } else {
                                    if (now.getTime() > tm.getPlanTime2().getTime()) {
                                        d8.setValue(CommonConstants.D_FORMAT_ERROR);
                                    }
                                }
                                break;
                        }
                    }
                    // 预期三
                    if (!ValidationUtil.isBlank(tm.getPlanStep3())) {
                        switch (tm.getPlanStep3()) {
                            case CommonConstants.D_STEP_D1:
                                if (tm.getD1Status()) {
                                    if (tm.getD1Time().getTime() > tm.getPlanTime3().getTime()) {
                                        d1.setOtherValue(CommonConstants.D_FORMAT_WARN);
                                    }
                                } else {
                                    if (now.getTime() > tm.getPlanTime3().getTime()) {
                                        d1.setValue(CommonConstants.D_FORMAT_ERROR);
                                    }
                                }

                                break;
                            case CommonConstants.D_STEP_D2:
                                if (tm.getD2Status()) {
                                    if (tm.getD2Time().getTime() > tm.getPlanTime3().getTime()) {
                                        d2.setOtherValue(CommonConstants.D_FORMAT_WARN);
                                    }
                                } else {
                                    if (now.getTime() > tm.getPlanTime3().getTime()) {
                                        d2.setValue(CommonConstants.D_FORMAT_ERROR);
                                    }
                                }
                                break;
                            case CommonConstants.D_STEP_D3:
                                if (tm.getD3Status()) {
                                    if (tm.getD3Time().getTime() > tm.getPlanTime3().getTime()) {
                                        d3.setOtherValue(CommonConstants.D_FORMAT_WARN);
                                    }
                                } else {
                                    if (now.getTime() > tm.getPlanTime3().getTime()) {
                                        d3.setValue(CommonConstants.D_FORMAT_ERROR);
                                    }
                                }
                                break;
                            case CommonConstants.D_STEP_D4:
                                if (tm.getD4Status()) {
                                    if (tm.getD4Time().getTime() > tm.getPlanTime3().getTime()) {
                                        d4.setOtherValue(CommonConstants.D_FORMAT_WARN);
                                    }
                                } else {
                                    if (now.getTime() > tm.getPlanTime3().getTime()) {
                                        d4.setValue(CommonConstants.D_FORMAT_ERROR);
                                    }
                                }
                                break;
                            case CommonConstants.D_STEP_D5:
                                if (tm.getD5Status()) {
                                    if (tm.getD5Time().getTime() > tm.getPlanTime3().getTime()) {
                                        d5.setOtherValue(CommonConstants.D_FORMAT_WARN);
                                    }
                                } else {
                                    if (now.getTime() > tm.getPlanTime3().getTime()) {
                                        d5.setValue(CommonConstants.D_FORMAT_ERROR);
                                    }
                                }
                                break;
                            case CommonConstants.D_STEP_D6:
                                if (tm.getD6Status()) {
                                    if (tm.getD6Time().getTime() > tm.getPlanTime3().getTime()) {
                                        d6.setOtherValue(CommonConstants.D_FORMAT_WARN);
                                    }
                                } else {
                                    if (now.getTime() > tm.getPlanTime3().getTime()) {
                                        d6.setValue(CommonConstants.D_FORMAT_ERROR);
                                    }
                                }
                                break;
                            case CommonConstants.D_STEP_D7:
                                if (tm.getD7Status()) {
                                    if (tm.getD7Time().getTime() > tm.getPlanTime3().getTime()) {
                                        d7.setOtherValue(CommonConstants.D_FORMAT_WARN);
                                    }
                                } else {
                                    if (now.getTime() > tm.getPlanTime3().getTime()) {
                                        d7.setValue(CommonConstants.D_FORMAT_ERROR);
                                    }
                                }
                                break;
                            case CommonConstants.D_STEP_D8:
                                if (tm.getD8Status()) {
                                    if (tm.getD8Time().getTime() > tm.getPlanTime3().getTime()) {
                                        d8.setOtherValue(CommonConstants.D_FORMAT_WARN);
                                    }
                                } else {
                                    if (now.getTime() > tm.getPlanTime3().getTime()) {
                                        d8.setValue(CommonConstants.D_FORMAT_ERROR);
                                    }
                                }
                                break;
                        }
                    }
                }

                issue.setCommonDTOList(commonDTOList);
                issue.setTimeManagement(tm);
            });
            total = page.getTotalElements();
        }
        // return PageUtil.toPage(page.map(issueMapper::toDto).getContent(), page.getTotalElements());
        map.put("content", list);
        map.put("totalElements", total);
        return map;
    }

    @Override
    public List<IssueDto> queryAll(IssueQueryCriteria criteria) {
        List<Issue> list = issueRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
        return issueMapper.toDto(list);
    }

    @Override
    public void download(List<IssueDto> issueDtoList, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (IssueDto issueDto : issueDtoList) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("问题标题", issueDto.getIssueTitle());
            map.put("物料编码", issueDto.getPartNum());
            map.put("客户名", issueDto.getCustomerName());
            map.put("状态", issueDto.getStatus());
            map.put("分数", issueDto.getScore());
            map.put("关闭时间", issueDto.getCloseTime());
            map.put("完成时长", issueDto.getDuration());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public void verification(Set<Long> ids) {

    }

    @Override
    public void reactiveTaskById(Long issueId) {
        Issue issue = issueRepository.findById(issueId).orElseGet(Issue::new);
        ValidationUtil.isNull(issue.getId(), "Issue", "id", issueId);
        PreTrail preTrail = preTrailRepository.findAllByIssueIdAndType(issueId, CommonConstants.NOT_DEL, CommonConstants.TRAIL_TYPE_8D);
        if (preTrail != null && preTrail.getIsDone()) {
            if (preTrail.getApproveResult().equals(false)) {
                preTrail.setApproveResult(null);
                preTrail.setComment(null);
                preTrail.setIsDone(false);
                issue.setStatus(CommonConstants.D_STATUS_AUDIT);
                issue.setReason(null);
                issueRepository.save(issue);
            }
            preTrailRepository.save(preTrail);
        }
    }
}
