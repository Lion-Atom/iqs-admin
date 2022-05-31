/*
 *  Copyright 2019-2020 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package me.zhengjie.modules.quartz.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.*;
import me.zhengjie.domain.vo.EmailVo;
import me.zhengjie.modules.quartz.repository.QuartzLogRepository;
import me.zhengjie.modules.system.domain.User;
import me.zhengjie.modules.system.repository.UserRepository;
import me.zhengjie.repository.*;
import me.zhengjie.rest.TrCertificationController;
import me.zhengjie.service.EmailService;
import me.zhengjie.utils.CommonUtils;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 测试用
 *
 * @author Tong Minjie
 * @date 2021-07-08
 */
@Slf4j
@Component
@Service
@RequiredArgsConstructor
public class TestTask {

    private final LocalStorageRepository localStorageRepository;
    private final ToolsLogRepository toolsLogRepository;
    private final QuartzLogRepository quartzLogRepository;
    private final AuditorRepository auditorRepository;
    private final PreTrailRepository preTrailRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final AuditPlanRepository planRepository;
    private final AuditPlanReportRepository reportRepository;
    private final InstruCaliRepository caliRepository;
    private final InstruCaliFileRepository fileRepository;
    private final TrainCertificationRepository trainCertRepository;
    private final TrainScheduleRepository trainScheduleRepository;
    private final TrainTipRepository trainTipRepository;
    private final EquipmentRepository equipRepository;
    private final InstrumentRepository instrumentRepository;

    public void run() {
        log.info("run 执行成功");
    }

    public void run1(String str) {
        log.info("run1 执行成功，参数为： {}" + str);
    }

    public void run2() {
        log.info("run2 执行成功");
    }

    /**
     * 走查临时文件是否过期
     */
    @Transactional(rollbackFor = Exception.class)
    public void checkFileObsolete() {
        List<LocalStorage> list = localStorageRepository.findAllByTempStatus(CommonConstants.TEMP_STATUS, CommonConstants.NOT_DEL);
        if (ValidationUtil.isNotEmpty(list)) {
            list.forEach(dto -> {
                Date date = new Date();
                Timestamp now = new Timestamp(date.getTime());
                if (dto.getExpirationTime().before(now)) {
                    dto.setFileStatus(CommonConstants.OBSOLETE_STATUS);
                    dto.setChangeDesc("达到过期时间，文件从临时文件修改为已报废状态");
                    localStorageRepository.save(dto);
                    ToolsLog log = new ToolsLog();
                    log.setLogType(CommonConstants.LOG_TYPE_FILE);
                    log.setUsername(dto.getCreateBy());
                    log.setBindingId(dto.getId());
                    log.setDescription("临时文件过期");
                    log.setDescriptionDetail("达到过期时间，文件从临时文件修改为已报废状态");
                    toolsLogRepository.save(log);
                }
            });
        }
        log.info("CheckFileObsolete 执行成功");
    }

    /**
     * 走查审核人员证书是否到期或者临近到期
     */
    @Transactional(rollbackFor = Exception.class)
    public void checkAuditorStatus() {
        List<Auditor> list = auditorRepository.findAll();
        if (ValidationUtil.isNotEmpty(list)) {
            list.forEach(auditor -> {
                // 审核人员分为三类：过期、即将过期、过期
//                if (!auditor.getNextCertificationTime().equals(auditor.getNextCertificationTime())) {
                // 粗略计算，误差一天内
                Timestamp validLine = auditor.getNextCertificationTime();
                Date now = new Date();
                if (validLine.getTime() > now.getTime()) {
                    long diff = validLine.getTime() - now.getTime();
                    int validTime = (int) (diff / (24 * 60 * 60 * 1000)) == 0 ? 1 : (int) (diff / (24 * 60 * 60 * 1000));
                    if (validTime > CommonConstants.AUDIT_DAYS_MONTH && CommonConstants.AUDIT_DAYS_SEASON >= validTime) {
                        // 30~90天为有效
                        auditor.setStatus(CommonConstants.AUDIT_PLAN_STATUS_VALID);
                    } else if (CommonConstants.AUDIT_DAYS_MONTH >= validTime) {
                        // 30天内即将过期
                        auditor.setStatus(CommonConstants.AUDITOR_STATUS_SOON_TO_EXPIRE);
                        // 7天内到期每天一封邮件,发送邮件给该审核人员
                        if (7 >= validTime) {
                            EmailVo emailVo = new EmailVo();
                            emailVo.setIsAdminSend(true);
                            emailVo.setContent("审核证书" + validTime + "天后到期，请尽快处理！");
                            emailVo.setSubject("审核证书即将到期提醒");
                            User user = userRepository.findById(auditor.getUserId()).orElseGet(User::new);
                            if (user != null) {
                                List<String> strings = new ArrayList<>();
                                strings.add(user.getEmail());
                                emailVo.setTos(strings);
                            }
                            // todo 暂时注释发邮件通知
                            emailService.send(emailVo, emailService.find());
                        }
                    } else {
                        auditor.setStatus(CommonConstants.AUDIT_PLAN_STATUS_VALID);
                    }
                } else {
                    // 过期的则需要删除其审批任务
                    auditor.setStatus(CommonConstants.AUDITOR_STATUS_EXPIRE);
                    auditor.setApprovalStatus(CommonConstants.AUDIT_PLAN_STATUS_UNABLE_ACTIVATED);
                    preTrailRepository.physicalDeleteAllByStorageIdIn(Collections.singleton(auditor.getId()));
                    EmailVo emailVo = new EmailVo();
                    emailVo.setIsAdminSend(true);
                    emailVo.setContent("提醒！！！您的审核证书已过期，请尽快处理！");
                    emailVo.setSubject("审核证书过期提醒");
                    User user = userRepository.findById(auditor.getUserId()).orElseGet(User::new);
                    if (user != null) {
                        List<String> strings = new ArrayList<>();
                        strings.add(user.getEmail());
                        emailVo.setTos(strings);
                    }
                    // log.info("走查到有审核员证书过期");
                    // todo 暂时注释发邮件通知
                    emailService.send(emailVo, emailService.find());
                }
//                }
                auditorRepository.save(auditor);
            });
        }
    }

    /**
     * 走查审核计划结案时间是否超时
     */
    @Transactional(rollbackFor = Exception.class)
    public void checkAuditPlanStatus() {
        // 获取非计划中的审核计划
        List<AuditPlan> plans = planRepository.findAllByStatusNotEqual(CommonConstants.AUDIT_PLAN_STATUS_TO);
        if (ValidationUtil.isNotEmpty(plans)) {
            plans.forEach(plan -> {
                // 获取报告信息
                AuditPlanReport report = reportRepository.findByPlanId(plan.getId());
                if (!plan.getStatus().equals(CommonConstants.AUDIT_PLAN_STATUS_FINISHED)) {
                    // 获取
                    if (report.getFinalDeadline() != null && CommonUtils.getNow().getTime() > report.getFinalDeadline().getTime()) {
                        plan.setIsOverdue(true);
                    } else {
                        plan.setIsOverdue(false);
                    }
                    plan.setCloseTime(null);
                    planRepository.save(plan);
                }
            });
        }
    }

    /**
     * 走查仪器校准提前邮件提醒
     */
    @Transactional(rollbackFor = Exception.class)
    public void checkCaliRemind() {
        List<InstruCali> calis = caliRepository.findByIsRemind();
        if (ValidationUtil.isNotEmpty(calis)) {
            // 设置时间
            calis.forEach(cali -> {
                // 监控需要提醒日期与今日相比，确定是否开启邮件通知
                long time = cali.getNextCaliDate().getTime() - cali.getRemindDays() * 24 * 3600 * 1000;
                long now = System.currentTimeMillis();
                if (time <= now) {
                    cali.setInstruName(cali.getInnerId() + "-" + cali.getInstruName());
                    EmailVo emailVo = new EmailVo();
                    emailVo.setIsAdminSend(true);
                    emailVo.setContent("提醒！！！【" + cali.getInstruName() + "】仪器校准已提上日程，请尽快处理！");
                    emailVo.setSubject("【" + cali.getInstruName() + "】仪器校准提醒");
                    User user = userRepository.findByUsername(cali.getUseBy());
                    if (user != null) {
                        List<String> strings = new ArrayList<>();
                        strings.add(user.getEmail());
                        emailVo.setTos(strings);
                    }
                    // log.info("走查到有审核员证书过期");
                    // 暂时注释发邮件通知
                    emailService.send(emailVo, emailService.find());
                }
            });
        }
    }

    /**
     * 走查仪器校准状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void checkInsCaliIsOverDue() {
        List<InstruCali> calis = caliRepository.findByIsNotDroped();
        if (ValidationUtil.isNotEmpty(calis)) {
            calis.forEach(cali -> {
                long time = cali.getNextCaliDate().getTime();
                // now时间定义为今天的前一毫秒
                long current = System.currentTimeMillis();//当前时间毫秒数
                long zero = current / (1000 * 3600 * 24) * (1000 * 3600 * 24) - TimeZone.getDefault().getRawOffset();
                // 下次校准时间超出，判定为超时未校准
                if (time < zero) {
                    cali.setStatus(CommonConstants.INSTRU_CALI_STATUS_OVERDUE);
                    fileRepository.updateToOld(cali.getId());
                    caliRepository.save(cali);
                }
            });
        }
    }

    /**
     * 走查培训认证证书生效状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void checkTrCertificationIsOverDue() {
        List<TrainCertification> certs = trainCertRepository.findAll();
        if (ValidationUtil.isNotEmpty(certs)) {
            certs.forEach(cert -> {
                long time = cert.getDueDate().getTime();
                long current = System.currentTimeMillis();//当前时间毫秒数
                long zero = current / (1000 * 3600 * 24) * (1000 * 3600 * 24) - TimeZone.getDefault().getRawOffset();
                int diff = (int) ((time - zero) / (24 * 60 * 60 * 1000));
                // 下次校准时间超出，判定为超时未校准
                if (diff < 1) {
                    cert.setCertificationStatus(CommonConstants.CERTIFICATION_STATUS_OVERDUE);
                } else if (diff <= 30) {
                    cert.setCertificationStatus(CommonConstants.CERTIFICATION_STATUS_SOON_TO_EXPIRE);
                } else {
                    cert.setCertificationStatus(CommonConstants.CERTIFICATION_STATUS_VALID);
                }
                trainCertRepository.save(cert);
            });
        }
    }

    /**
     * 走查仪器校准状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void checkInstruCaliIsOverdueV2() {
        List<Instrument> list = instrumentRepository.findByIsNotDroped(CommonConstants.INSTRUMENT_STATUS_DROP);
        List<Instrument> newList = new ArrayList<>();
        if (ValidationUtil.isNotEmpty(list)) {
            list.forEach(instrument -> {
                if (instrument.getNextCaliDate() != null) {
                    long current = System.currentTimeMillis();//当前时间毫秒数
                    // 今日零点前一毫秒
                    long zero = current - (current + TimeZone.getDefault().getRawOffset()) % (1000 * 3600 * 24);
                    long dueDate = instrument.getNextCaliDate().getTime();
                    int diff = (int) Math.ceil((double) (dueDate - zero) / (24 * 60 * 60 * 1000));
                    // 校准过期时间与当前时间对比，若是小于当前时间则认定为过期未保养
                    if (diff > 0) {
                        if (instrument.getRemindDays() != null && diff <= instrument.getRemindDays()) {
                            instrument.setCaliStatus(CommonConstants.INSTRU_CALI_STATUS_SOON_OVERDUE);
                        } else {
                            instrument.setCaliStatus(CommonConstants.INSTRU_CALI_STATUS_FINISHED);
                        }
                    } else {
                        instrument.setCaliStatus(CommonConstants.INSTRU_CALI_STATUS_OVERDUE);
                    }
                    newList.add(instrument);
                }
            });
        }
        instrumentRepository.saveAll(newList);
    }

    /**
     * 走查设备保养状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void checkEquipMtIsOverdue() {
        List<Equipment> list = equipRepository.findByMaintainDueDateIsNotNull();
        List<Equipment> newList = new ArrayList<>();
        if (ValidationUtil.isNotEmpty(list)) {
            list.forEach(equip -> {
                long current = System.currentTimeMillis();//当前时间毫秒数
                // 今日零点前一毫秒
                long zero = current - (current + TimeZone.getDefault().getRawOffset()) % (1000 * 3600 * 24) - 1;
                long time = equip.getMaintainDueDate().getTime();
                int diff = (int) (time - zero);
                // 保养过期时间与当前时间对比，若是小于当前时间则认定为过期未保养
                if (diff <= 0) {
                    equip.setMaintainStatus(CommonConstants.MAINTAIN_STATUS_VALID);
                } else {
                    equip.setMaintainStatus(CommonConstants.MAINTAIN_STATUS_OVERDUE);
                }
                newList.add(equip);
            });
        }
        equipRepository.saveAll(newList);
    }

    /**
     * 走查培训安排开放关闭状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void checkTrScheduleIsOpened() {
        List<TrainSchedule> schedules = trainScheduleRepository.findAll();
        if (ValidationUtil.isNotEmpty(schedules)) {
            schedules.forEach(schedule -> {
                long time = 0L;
                if (schedule.getIsDelay()) {
                    time = schedule.getNewTrainTime().getTime();
                } else {
                    time = schedule.getTrainTime().getTime();
                }
                // now时间定义为今天的前一毫秒
                long current = System.currentTimeMillis();//当前时间毫秒数
                // 下次校准时间超出，判定为超时未校准
                if (time < current) {
                    schedule.setScheduleStatus(CommonConstants.SCHEDULE_STATUS_CLOSED);
                } else {
                    schedule.setScheduleStatus(CommonConstants.SCHEDULE_STATUS_OPENED);
                }
                trainScheduleRepository.save(schedule);
            });
        }
    }

    /**
     * 走查培训记录是否到了提前提醒日期
     */
    @Transactional(rollbackFor = Exception.class)
    public void checkIsToTrain() {
        checkTrScheduleIsOpened();
        checkTrCertificationIsOverDue();
        // 实时清空（更新）提醒栏目
        trainTipRepository.deleteAll();
        List<TrainTip> tips = new ArrayList<>();
        // 走查任务到了设置日期就提醒
        // 1.认证证书过期检测
        List<TrainCertification> certs = trainCertRepository.findAllByIsRemind(true);
        if (ValidationUtil.isNotEmpty(certs)) {
            certs.forEach(cert -> {
                // 监控需要提醒日期与今日相比，确定是否开启邮件通知
                long now = System.currentTimeMillis();
                int remainDays = (int) Math.ceil((double) (cert.getDueDate().getTime() - now) / (24 * 60 * 60 * 1000));
                if (remainDays <= cert.getRemindDays()) {
                    TrainTip certTip = new TrainTip();
                    certTip.setBindingId(cert.getId());
                    certTip.setTrainType(CommonConstants.TRAIN_TIP_TYPE_CERTIFICATION);
                    certTip.setDeadline(cert.getDueDate());
                    certTip.setRemainDays(remainDays);
                    certTip.setStatus(cert.getCertificationStatus());
                    tips.add(certTip);
                }
            });
        }
        // 2.培训安排到期提醒
        List<TrainSchedule> schedules = trainScheduleRepository.findAllByIsRemind(true);
        if (ValidationUtil.isNotEmpty(schedules)) {
            schedules.forEach(schedule -> {
                // 监控需要提醒日期与今日相比，确定是否开启邮件通知
                long time = 0L;
                long now = System.currentTimeMillis();
                if (schedule.getIsDelay()) {
                    time = schedule.getNewTrainTime().getTime();
                } else {
                    time = schedule.getTrainTime().getTime();
                }
                int remainDays = (int) Math.ceil((double) (time - now) / (24 * 60 * 60 * 1000));
                // 剩余时间小于设置的时间则需要显示到提示列表中去
                if (remainDays <= schedule.getRemindDays()) {
                    TrainTip scheduleTip = new TrainTip();
                    scheduleTip.setBindingId(schedule.getId());
                    scheduleTip.setTrainType(CommonConstants.TRAIN_TIP_TYPE_SCHEDULE);
                    scheduleTip.setDeadline(schedule.getIsDelay() ? schedule.getNewTrainTime() : schedule.getTrainTime());
                    scheduleTip.setRemainDays(remainDays);
                    scheduleTip.setStatus(schedule.getScheduleStatus());
                    tips.add(scheduleTip);
                }
            });
        }
        trainTipRepository.saveAll(tips);
    }

    /**
     * 定时清空任务日志记录
     */
    public void clearQuartzLog() {
        quartzLogRepository.deleteAll();
    }
}
