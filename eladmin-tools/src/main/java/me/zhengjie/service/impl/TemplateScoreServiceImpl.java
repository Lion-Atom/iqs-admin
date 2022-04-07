package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.*;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.*;
import me.zhengjie.service.AuditPlanService;
import me.zhengjie.service.TemplateScoreService;
import me.zhengjie.service.dto.TemplateScoreDto;
import me.zhengjie.service.dto.TemplateScoreQueryDto;
import me.zhengjie.service.mapstruct.TemplateScoreMapper;
import me.zhengjie.utils.ValidationUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.xerces.impl.validation.ValidationState;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;


/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/21 18:08
 */
@Service
@RequiredArgsConstructor
public class TemplateScoreServiceImpl implements TemplateScoreService {

    private final PlanTemplateRepository templateRepository;
    private final TemplateScoreRepository scoreRepository;
    private final AuditPlanService auditPlanService;
    private final AuditPlanRepository planRepository;
    private final TemplateScoreMapper scoreMapper;
    private final AuditPlanReportRepository reportRepository;

    @Override
    public List<TemplateScoreDto> getByTemplateIdAndTypes(TemplateScoreQueryDto dto) {
        List<TemplateScoreDto> list = new ArrayList<>();
        List<TemplateScore> scores = new ArrayList<>();
        if (ValidationUtil.isEmpty(dto.getItemTypes())) {
            if (dto.getIsActive()) {
                // 需要筛选激活数据
                scores = scoreRepository.findByTemplateId(dto.getTemplateId(), dto.getIsActive());
            } else {
                // 获取全部
                scores = scoreRepository.findAllByTemplateId(dto.getTemplateId());
            }
        } else {
            if (dto.getIsActive()) {
                scores = scoreRepository.findByTempIdAndItemTypeIn(dto.getTemplateId(), dto.getItemTypes(), dto.getIsActive());
            } else {
                scores = scoreRepository.findAllByTempIdAndItemTypeIn(dto.getTemplateId(), dto.getItemTypes());
            }
        }
        if (ValidationUtil.isNotEmpty(scores)) {
            list = scoreMapper.toDto(scores);
            list.forEach(sto -> {
                if (sto.getItemName() == null && sto.getPid() != null) {
                    TemplateScore pScore = scoreRepository.findById(sto.getPid()).orElseGet(TemplateScore::new);
                    sto.setPItemName(pScore.getItemName());
                } else {
                    sto.setPItemName(sto.getItemType());
                }
            });
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(List<TemplateScore> resources) {
        if (ValidationUtil.isNotEmpty(resources)) {
            DecimalFormat df = new DecimalFormat("0.00");
            Long templateId = resources.get(0).getTemplateId();
            // 权限判断
            PlanTemplate template = templateRepository.findById(templateId).orElseGet(PlanTemplate::new);
            ValidationUtil.isNull(template.getId(), "PlanTemplate", "id", templateId);
            // 判断是否具备修改审核计划权限
            auditPlanService.checkHasAuthExecute(template.getPlanId());
            // 查询审核计划的产线分布
            List<String> lines = new ArrayList<>();
            Map<String, List<Double>> map = new HashMap<>();
            AuditPlan plan = planRepository.findById(template.getPlanId()).orElseGet(AuditPlan::new);
            AuditPlanReport report = reportRepository.findByPlanId(template.getPlanId());
            ValidationUtil.isNull(report.getPlanId(), "AuditPlanReport", "planId", template.getPlanId());
            if (plan != null) {
                lines = Arrays.asList(plan.getLine().split(","));
            }
            // 处理分数
            // 判断总数是否达到2/3，否则不可提交，提示缺失几项
            int actualSum = 0;
            int total = 0;

            // 计算总得分及平均分
            List<Double> allScores = new ArrayList<>();
            double avgAllScore = 0;

            /**
             * todo 统计带*号项目得分及其等级
             *
             * F90： 1.星号： 1个=4，B；1个=0，C；2.非星号有问题 = 0 ，B
             *
             */
            List<Double> allAvgScores = new ArrayList<>();
            String level = "A";
            Set<String> itemTypes = new HashSet<>();
            List<Double> specialScores = new ArrayList<>();
            List<Double> commonScores = new ArrayList<>();

            // 计算P2项目
            List<Double> p2Scores = new ArrayList<>();
            double sumP2Score = 0;
            double avdP2Score = 0;

            // 计算P3项目
            final Long[] p3Id = new Long[1];
            List<Double> p3CQScores = new ArrayList<>();
            double sumP3CQScore = 0;
            double avdP3CQScore = 0;

            List<Double> p3GCScores = new ArrayList<>();
            double sumP3GCScore = 0;
            double avdP3GCScore = 0;

            // 计算P4项目
            final Long[] p4Id = new Long[1];
            List<Double> p4CQScores = new ArrayList<>();
            double sumP4CQScore = 0;
            double avdP4CQScore = 0;

            List<Double> p4GCScores = new ArrayList<>();
            double sumP4GCScore = 0;
            double avdP4GCScore = 0;

            // 计算P5项目
            List<Double> p5Scores = new ArrayList<>();
            double sumP5Score = 0;
            double avdP5Score = 0;

            // 计算P6项目
            final Long[] p6Id = new Long[1];
            List<Double> p6AllScores = new ArrayList<>();

            // 计算P7项目
            List<Double> p7Scores = new ArrayList<>();
            double sumP7Score = 0;
            double avdP7Score = 0;

            List<String> finalLines = lines;
            resources.forEach(score -> {
                itemTypes.add(score.getItemType());
                // 统计带星号和非星号的问题得分
                if (score.getIsNeed() && score.getScore() != null && score.getScore() != -1) {
                    // 特殊问题得分汇总
                    if (score.getIsSpecial()) {
                        specialScores.add(score.getScore());
                    } else {
                        // 非星号问题得分汇总
                        commonScores.add(score.getScore());
                    }
                }
                //"NA"占位符设置值未-1
                // 过滤出P2项目
                if (score.getItemType().equals(CommonConstants.TEMPLATE_QUES_P2)) {
                    if (!score.getItemName().equals(CommonConstants.TEMPLATE_QUES_P2) && score.getScore() != null && score.getScore() != -1) {
                        //属于P2的子项
                        p2Scores.add(score.getScore());
                        allScores.add(score.getScore());
                    }
                }

                // 过滤出P3项目
                if (score.getItemType().equals(CommonConstants.TEMPLATE_QUES_P3)) {
                    if (score.getPid() == null) {
                        //属于P3根结点
                        p3Id[0] = score.getId();
                    }

                    if (score.getItemName() == null && score.getContent().equals(CommonConstants.VDA_TEMPLATE_PRODUCT) && score.getScore() != null && score.getScore() != -1) {
                        //属于P3的产品子项
                        p3CQScores.add(score.getScore());
                        allScores.add(score.getScore());
                    }
                    if (score.getItemName() == null && score.getContent().equals(CommonConstants.VDA_TEMPLATE_PROCESS) && score.getScore() != null && score.getScore() != -1) {
                        //属于P3的过程子项
                        p3GCScores.add(score.getScore());
                        allScores.add(score.getScore());
                    }
                }

                // 过滤出P4项目
                if (score.getItemType().equals(CommonConstants.TEMPLATE_QUES_P4)) {
                    if (score.getPid() == null) {
                        //属于P3根结点
                        p4Id[0] = score.getId();
                    }

                    if (score.getItemName() == null && score.getContent().equals(CommonConstants.VDA_TEMPLATE_PRODUCT) && score.getScore() != null && score.getScore() != -1) {
                        //属于P4的产品子项
                        p4CQScores.add(score.getScore());
                        allScores.add(score.getScore());
                    }
                    if (score.getItemName() == null && score.getContent().equals(CommonConstants.VDA_TEMPLATE_PROCESS) && score.getScore() != null && score.getScore() != -1) {
                        //属于P5的过程子项
                        p4GCScores.add(score.getScore());
                        allScores.add(score.getScore());
                    }
                }

                // 过滤出P5项目
                if (score.getItemType().equals(CommonConstants.TEMPLATE_QUES_P5)) {
                    if (!score.getItemName().equals(CommonConstants.TEMPLATE_QUES_P5) && score.getScore() != null && score.getScore() != -1) {
                        //属于P5的子项
                        p5Scores.add(score.getScore());
                        allScores.add(score.getScore());
                    }
                }

                // 过滤出P6项目
                if (score.getItemType().equals(CommonConstants.TEMPLATE_QUES_P6)) {
                    if (score.getPid() == null) {
                        //属于P3根结点
                        p6Id[0] = score.getId();
                    }
                    // 获得各产线的分数集合
                    finalLines.forEach(line -> {
                        if (score.getContent().equals(line) && score.getIsNeed() && score.getScore() != null && score.getScore() != -1) {
                            p6AllScores.add(score.getScore());
                            allScores.add(score.getScore());
                            if (map.containsKey(line)) {
                                map.get(line).add(score.getScore());
                            } else {
                                List<Double> scoreList = new ArrayList<>();
                                scoreList.add(score.getScore());
                                map.put(line, scoreList);
                            }
                        }
                    });
                }

                // 过滤出P7项目
                if (score.getItemType().equals(CommonConstants.TEMPLATE_QUES_P7)) {
                    if (!score.getItemName().equals(CommonConstants.TEMPLATE_QUES_P7) && score.getScore() != null && score.getScore() != -1) {
                        //属于P5的子项
                        p7Scores.add(score.getScore());
                        allScores.add(score.getScore());
                    }
                }
            });
            if (itemTypes.contains(CommonConstants.TEMPLATE_QUES_P2)) {
                total += 7;
            }
            if (itemTypes.contains(CommonConstants.TEMPLATE_QUES_P3)) {
                total += 10;
            }
            if (itemTypes.contains(CommonConstants.TEMPLATE_QUES_P4)) {
                total += 14;
            }
            if (itemTypes.contains(CommonConstants.TEMPLATE_QUES_P5)) {
                total += 7;
            }
            if (itemTypes.contains(CommonConstants.TEMPLATE_QUES_P6)) {
                total += (26 * lines.size());
            }
            if (itemTypes.contains(CommonConstants.TEMPLATE_QUES_P7)) {
                total += 7;
            }

            if (ValidationUtil.isNotEmpty(p2Scores)) {
                actualSum += p2Scores.size();
//                total += 7;
            }
            if (ValidationUtil.isNotEmpty(p3CQScores)) {
                actualSum += p3CQScores.size();
//                total += 5;
            }
            if (ValidationUtil.isNotEmpty(p3GCScores)) {
                actualSum += p3GCScores.size();
//                total += 5;
            }
            if (ValidationUtil.isNotEmpty(p4CQScores)) {
                actualSum += p4CQScores.size();
//                total += 6;
            }
            if (ValidationUtil.isNotEmpty(p4GCScores)) {
                actualSum += p4GCScores.size();
//                total += 8;
            }
            if (ValidationUtil.isNotEmpty(p5Scores)) {
                actualSum += p5Scores.size();
//                total += 7;
            }
            // 添加P6数目
            if (!map.isEmpty()) {
                // 计算所有产线的数据
                actualSum += p6AllScores.size();
//                total += (26 * lines.size());
            }
            if (ValidationUtil.isNotEmpty(p7Scores)) {
                actualSum += p7Scores.size();
//                total += 5;
            }
            // 计算比例:所填/所选不可小于2/3
            if (total > 0 && (((float) actualSum / total) < ((float) 2 / 3))) {
                throw new BadRequestException("当前填写项占比：" + df.format((float) actualSum / total) + ";填写项小于2/3，不符合提交条件！");
            } else if (total == 0) {
                throw new BadRequestException("尚未填写子项目得分，无法提交！");
            }

            // 计算P2项目平均分
            if (ValidationUtil.isNotEmpty(p2Scores)) {
                for (Double p2Score : p2Scores) {
                    sumP2Score += p2Score;
                }
                avdP2Score = sumP2Score / p2Scores.size();
                // 收集P2项目平均分
                allAvgScores.add(avdP2Score);
                double finalAvdP2Score = avdP2Score;
                resources.forEach(score -> {
                    if (score.getItemType().equals(CommonConstants.TEMPLATE_QUES_P2) && score.getItemName().equals(CommonConstants.TEMPLATE_QUES_P2) && !score.getIsNeed()) {
                        //属于P2的子项
                        score.setScore(Double.valueOf(df.format(finalAvdP2Score)));
                    }
                });
            } else {
                resources.forEach(score -> {
                    if (score.getItemType().equals(CommonConstants.TEMPLATE_QUES_P2) && score.getItemName().equals(CommonConstants.TEMPLATE_QUES_P2) && !score.getIsNeed()) {
                        //属于P2的子项
                        score.setScore(null);
                    }
                });
            }

            // 计算P3项目产品平均分
            if (ValidationUtil.isNotEmpty(p3CQScores)) {
                for (Double p3Score : p3CQScores) {
                    sumP3CQScore += p3Score;
                }
                avdP3CQScore = sumP3CQScore / p3CQScores.size();
                allAvgScores.add(avdP3CQScore);
                double finalAvdP3CQScore = avdP3CQScore;
                resources.forEach(score -> {
                    if (score.getItemType().equals(CommonConstants.TEMPLATE_QUES_P3) && score.getItemName() == null && !score.getIsNeed() && ArrayUtils.contains(p3Id, score.getPid())
                            && score.getContent().equals(CommonConstants.VDA_TEMPLATE_PRODUCT)) {
                        //属于P3的产品子项
                        score.setScore(Double.valueOf(df.format(finalAvdP3CQScore)));
                    }
                });
            } else {
                resources.forEach(score -> {
                    if (score.getItemType().equals(CommonConstants.TEMPLATE_QUES_P3) && score.getItemName() == null && !score.getIsNeed() && ArrayUtils.contains(p3Id, score.getPid())
                            && score.getContent().equals(CommonConstants.VDA_TEMPLATE_PRODUCT)) {
                        //属于P3的产品子项
                        score.setScore(null);
                    }
                });
            }

            // 计算P3项目过程平均分
            if (ValidationUtil.isNotEmpty(p3GCScores)) {
                for (Double p3Score : p3GCScores) {
                    sumP3GCScore += p3Score;
                }
                avdP3GCScore = sumP3GCScore / p3GCScores.size();
                allAvgScores.add(avdP3GCScore);
                double finalAvdP3GCScore = avdP3GCScore;
                resources.forEach(score -> {
                    if (score.getItemType().equals(CommonConstants.TEMPLATE_QUES_P3) && score.getItemName() == null && !score.getIsNeed() && ArrayUtils.contains(p3Id, score.getPid())
                            && score.getContent().equals(CommonConstants.VDA_TEMPLATE_PROCESS)) {
                        //属于P3的过程子项
                        score.setScore(Double.valueOf(df.format(finalAvdP3GCScore)));
                    }
                });
            } else {
                resources.forEach(score -> {
                    if (score.getItemType().equals(CommonConstants.TEMPLATE_QUES_P3) && score.getItemName() == null && !score.getIsNeed() && ArrayUtils.contains(p3Id, score.getPid())
                            && score.getContent().equals(CommonConstants.VDA_TEMPLATE_PROCESS)) {
                        //属于P3的过程子项
                        score.setScore(null);
                    }
                });
            }

            // 计算P4项目产品平均分
            if (ValidationUtil.isNotEmpty(p4CQScores)) {
                for (Double p4Score : p4CQScores) {
                    sumP4CQScore += p4Score;
                }
                avdP4CQScore = sumP4CQScore / p4CQScores.size();
                allAvgScores.add(avdP4CQScore);
                double finalAvdP4CQScore = avdP4CQScore;
                resources.forEach(score -> {
                    if (score.getItemType().equals(CommonConstants.TEMPLATE_QUES_P4) && score.getItemName() == null && !score.getIsNeed() && ArrayUtils.contains(p4Id, score.getPid())
                            && score.getContent().equals(CommonConstants.VDA_TEMPLATE_PRODUCT)) {
                        //属于P4的产品子项
                        score.setScore(Double.valueOf(df.format(finalAvdP4CQScore)));
                    }
                });
            } else {
                resources.forEach(score -> {
                    if (score.getItemType().equals(CommonConstants.TEMPLATE_QUES_P4) && score.getItemName() == null && !score.getIsNeed() && ArrayUtils.contains(p4Id, score.getPid())
                            && score.getContent().equals(CommonConstants.VDA_TEMPLATE_PRODUCT)) {
                        //属于P4的产品子项
                        score.setScore(null);
                    }
                });
            }

            // 计算P4项目过程平均分
            if (ValidationUtil.isNotEmpty(p4GCScores)) {
                for (Double p4Score : p4GCScores) {
                    sumP4GCScore += p4Score;
                }
                avdP4GCScore = sumP4GCScore / p4GCScores.size();
                allAvgScores.add(avdP4GCScore);
                double finalAvdP4GCScore = avdP4GCScore;
                resources.forEach(score -> {
                    if (score.getItemType().equals(CommonConstants.TEMPLATE_QUES_P4) && score.getItemName() == null && !score.getIsNeed() && ArrayUtils.contains(p4Id, score.getPid())
                            && score.getContent().equals(CommonConstants.VDA_TEMPLATE_PROCESS)) {
                        //属于P4的过程子项
                        score.setScore(Double.valueOf(df.format(finalAvdP4GCScore)));
                    }
                });
            } else {
                resources.forEach(score -> {
                    if (score.getItemType().equals(CommonConstants.TEMPLATE_QUES_P4) && score.getItemName() == null && !score.getIsNeed() && ArrayUtils.contains(p4Id, score.getPid())
                            && score.getContent().equals(CommonConstants.VDA_TEMPLATE_PROCESS)) {
                        //属于P4的过程子项
                        score.setScore(null);
                    }
                });
            }

            // 计算P5项目平均分
            if (ValidationUtil.isNotEmpty(p5Scores)) {
                for (Double p5Score : p5Scores) {
                    sumP5Score += p5Score;
                }
                avdP5Score = sumP5Score / p5Scores.size();
                // 收集P5项目平均分
                allAvgScores.add(avdP5Score);
                double finalAvdP5Score = avdP5Score;
                resources.forEach(score -> {
                    if (score.getItemType().equals(CommonConstants.TEMPLATE_QUES_P5) &&
                            score.getItemName().equals(CommonConstants.TEMPLATE_QUES_P5) && !score.getIsNeed()) {
                        //属于P5的子项
                        score.setScore(Double.valueOf(df.format(finalAvdP5Score)));
                    }
                });
            } else {
                resources.forEach(score -> {
                    if (score.getItemType().equals(CommonConstants.TEMPLATE_QUES_P5) &&
                            score.getItemName().equals(CommonConstants.TEMPLATE_QUES_P5) && !score.getIsNeed()) {
                        //属于P5的子项
                        score.setScore(null);
                    }
                });
            }

            // 计算P6项目各产线的平均分
            if (!map.isEmpty()) {
                for (String key : map.keySet()) {
                    double sumP6Score = 0;
                    List<Double> list = map.get(key);
                    for (Double p6Score : list) {
                        sumP6Score += p6Score;
                    }
                    double finalSumP6Score = sumP6Score;
                    resources.forEach(score -> {
                        if (score.getItemType().equals(CommonConstants.TEMPLATE_QUES_P6)
                                && !score.getIsNeed() && score.getContent().equals(key) && ArrayUtils.contains(p6Id, score.getPid())) {
                            score.setScore(Double.valueOf(df.format(finalSumP6Score / list.size())));
                            // 收集P6项目平均分
                            allAvgScores.add(Double.valueOf(df.format(finalSumP6Score / list.size())));
                        }
                    });
                }
            } else {
                for (String key : map.keySet()) {
                    resources.forEach(score -> {
                        if (score.getItemType().equals(CommonConstants.TEMPLATE_QUES_P6)
                                && !score.getIsNeed() && score.getContent().equals(key) && ArrayUtils.contains(p6Id, score.getPid())) {
                            score.setScore(null);
                        }
                    });
                }
            }

            // 计算P7项目平均分
            if (ValidationUtil.isNotEmpty(p7Scores)) {
                for (Double p7Score : p7Scores) {
                    sumP7Score += p7Score;
                }
                avdP7Score = sumP7Score / p7Scores.size();
                // 收集P7项目平均分
                allAvgScores.add(avdP7Score);
                double finalAvdP7Score = avdP7Score;
                resources.forEach(score -> {
                    if (score.getItemType().equals(CommonConstants.TEMPLATE_QUES_P7) &&
                            score.getItemName().equals(CommonConstants.TEMPLATE_QUES_P7) && !score.getIsNeed()) {
                        //属于P7的子项
                        score.setScore(Double.valueOf(df.format(finalAvdP7Score)));
                    }
                });
            } else {
                resources.forEach(score -> {
                    if (score.getItemType().equals(CommonConstants.TEMPLATE_QUES_P7) &&
                            score.getItemName().equals(CommonConstants.TEMPLATE_QUES_P7) && !score.getIsNeed()) {
                        //属于P7的子项
                        score.setScore(null);
                    }
                });
            }
            scoreRepository.saveAll(resources);

            // 计算总平均分
            if (ValidationUtil.isNotEmpty(allScores)) {
                double allScore = 0;
                for (Double score : allScores) {
                    allScore += score;
                }
                avgAllScore = allScore / allScores.size();
                // todo 判断级别
                if (avgAllScore >= 9.0) {
                    report.setResult("A");
                    // 1. 特殊项 - 星号问题判断
                    if (ValidationUtil.isNotEmpty(specialScores)) {
                        if (specialScores.contains(4.0)) {
                            report.setResult("B");
                        } else if (specialScores.contains(0.0)) {
                            report.setResult("C");
                        }
                    }
                    // 2. 非特殊项 - 有问题判断
                    if (report.getResult().equals("A") && ValidationUtil.isNotEmpty(commonScores)) {
                        if (commonScores.contains(0.0)) {
                            report.setResult("B");
                        }
                    }
                    // 3. P6下6.1-6.6的分数判断
                    if ((report.getResult().equals("A") || report.getResult().equals("B")) && ValidationUtil.isNotEmpty(p6AllScores)) {
                        double p6Avg = 0;
                        double sumP6 = p6AllScores.stream().mapToDouble(p6Score -> p6Score).sum();
                        p6Avg = sumP6 / p6AllScores.size();
                        if (8.0 < p6Avg && p6Avg < 9.0) {
                            report.setResult("B");
                        } else if (p6Avg < 7.0) {
                            report.setResult("C");
                        }
                    }
                    // todo 4. P6下各产线的分数判断
                    // 5. P2~P7分数判断：<8.0,B；<7.0,C
                    if ((report.getResult().equals("A") || report.getResult().equals("B"))) {
                        allAvgScores.forEach(av -> {
                            if (av < 8.0) {
                                report.setResult("B");
                            } else if (av < 7.0) {
                                report.setResult("C");
                            }
                        });
                    }
                } else if (avgAllScore >= 8.0) {
                    report.setResult("B");
                } else {
                    report.setResult("C");
                }
                report.setScore(Double.valueOf(df.format(avgAllScore)));
            }
            reportRepository.save(report);
        }
    }
}
