package me.zhengjie.modules.system.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.*;
import me.zhengjie.modules.system.domain.ApprovalProcess;
import me.zhengjie.modules.system.domain.ToolsTask;
import me.zhengjie.modules.system.domain.User;
import me.zhengjie.modules.system.repository.ApprovalProcessRepository;
import me.zhengjie.modules.system.repository.ToolsTaskRepository;
import me.zhengjie.modules.system.repository.UserRepository;
import me.zhengjie.modules.system.service.ToolsTaskService;
import me.zhengjie.modules.system.service.dto.TaskQueryCriteria;
import me.zhengjie.modules.system.service.dto.ToolsTaskDto;
import me.zhengjie.modules.system.service.mapstruct.ToolsTaskMapper;
import me.zhengjie.repository.*;
import me.zhengjie.utils.QueryHelp;
import me.zhengjie.utils.SecurityUtils;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/6/23 9:41
 */
@Service
@RequiredArgsConstructor
public class ToolsTaskServiceImpl implements ToolsTaskService {

    private final ToolsTaskMapper toolsTaskMapper;

    private final ToolsTaskRepository toolsTaskRepository;

    private final ApprovalProcessRepository approvalProcessRepository;

    private final LocalStorageRepository localStorageRepository;

    private final LocalStorageTempRepository localStorageTempRepository;

    private final BindingLocalStorageRepository bindingLocalStorageRepository;

    private final BindingDeptRepository bindingDeptRepository;

    private final BindingLocalStorageTempRepository bindingLocalStorageTempRepository;

    private final BindingDeptTempRepository bindingDeptTempRepository;

    private final UserRepository userRepository;

    private final AuditorRepository auditorRepository;

    private final AuditPlanRepository auditPlanRepository;


    @Override
    public Map<String, Object> queryAll(TaskQueryCriteria criteria, Pageable pageable) {
        if (!SecurityUtils.getIsAdmin()) {
            criteria.setApprovedBy(SecurityUtils.getCurrentUserId());
        } else {
            if (criteria.getSelfFlag()) {
                criteria.setApprovedBy(SecurityUtils.getCurrentUserId());
            }
        }
        Page<ToolsTask> page = toolsTaskRepository.findAll((root, query, cb) -> QueryHelp.getPredicate(root, criteria, cb), pageable);
        Map<String, Object> map = new HashMap<>();
        // 无处理，直接返回表数据
        // map = PageUtil.toPage(page.map(toolsTaskMapper::toDto));
        // 添加审批者名称和文件名称字段
        List<ToolsTaskDto> list = new ArrayList<>();
        long total = 0L;
        if (ValidationUtil.isNotEmpty(page.getContent())) {
            list = toolsTaskMapper.toDto(page.getContent());
            list.forEach(dto -> {
                // 获取文件名称
                if (dto.getType().equals(CommonConstants.TRAIL_TYPE_FILE)) {
                    if (dto.getChangeType() != null && dto.getChangeType().equals(CommonConstants.REVISE)) {
                        // 若是修改则需要关联文件更改正式文件并删除此次的修改
                        LocalStorageTemp temp = localStorageTempRepository.findById(dto.getStorageId()).orElseGet(LocalStorageTemp::new);
                        // LocalStorage storage = localStorageRepository.findById(temp.getStorageId()).orElseGet(LocalStorage::new);
                        ValidationUtil.isNull(temp.getId(), "LocalStorageTemp", "id", dto.getStorageId());
                        dto.setStorageName(temp.getName());
                    } else {
                        LocalStorage storage = localStorageRepository.findById(dto.getStorageId()).orElseGet(LocalStorage::new);
                        ValidationUtil.isNull(storage.getId(), "LocalStorage", "id", dto.getStorageId());
                        dto.setStorageName(storage.getName());
                    }

                } else {
                    // 文件+审核待批准
                    dto.setStorageName(dto.getRealName());
                }
                // 获取初始发起者姓名:任务提交者
                ToolsTask firstTask = toolsTaskRepository.findFirstByPreNo(dto.getPreTrailNo(), CommonConstants.NOT_DEL);
                dto.setOwnerName(firstTask.getCreateBy());

                // 获取审批人姓名
                User user = userRepository.findById(dto.getApprovedBy()).orElseGet(User::new);
                ValidationUtil.isNull(user.getId(), "User", "id", dto.getApprovedBy());
                if (user.getDept() != null) {
                    dto.setApprover(user.getDept().getName() + " - " + user.getUsername());
                } else {
                    dto.setApprover(user.getUsername());
                }
            });
            total = page.getTotalElements();
        }

        map.put("content", list);
        map.put("totalElements", total);
        return map;
    }

    @Override
    public Integer queryTaskCount() {
        Integer count;
        if (SecurityUtils.getIsAdmin()) {
            //获取全部
            // count = toolsTaskRepository.findAllCount();
            //更改为获取未完成任务
            count = toolsTaskRepository.findAllNotDoneCount(false);
        } else {
            //获取个人全部任务
            //count = toolsTaskRepository.findCountByUserId(SecurityUtils.getCurrentUserId());
            //更改为获取个人未完成任务
            count = toolsTaskRepository.findAllNotDoneCountByUserId(SecurityUtils.getCurrentUserId(), false);
        }
        return count;
    }

    @Override
    public void update(ToolsTask task) {
        ToolsTask toolsTask = toolsTaskRepository.findById(task.getId()).orElseGet(ToolsTask::new);
        ValidationUtil.isNull(toolsTask.getId(), "ToolsTask", "id", toolsTask.getId());
        toolsTask.copy(task);
        toolsTaskRepository.save(toolsTask);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(ToolsTask task) {
        // 提交当前审批任务:1.修改当前任务为已完成；2.更新对应的审批流程进度；3.指定下一个任务审批者（若此时审批人即登陆人为质量部Master则直接关闭任务）
        ToolsTask toolsTask = toolsTaskRepository.findById(task.getId()).orElseGet(ToolsTask::new);
        ValidationUtil.isNull(toolsTask.getId(), "ToolsTask", "id", toolsTask.getId());
        // storageId 已设置NotBlank，因此不需要空指针判断---不考虑开库情况
        Long storageId = toolsTask.getStorageId();
        String version = toolsTask.getVersion();
        Long taskId = toolsTask.getId();
        toolsTask.copy(task);
        toolsTask.setIsDone(true);
        toolsTaskRepository.save(toolsTask);

        //重新计算时长:当前时间-createTime
        String duration = "1天";
        Date now = new Date();
        long diff = now.getTime() - task.getCreateTime().getTime();
        // long diffSeconds = diff / 1000 % 60; //秒
        // long diffMinutes = diff / (60 * 1000) % 60; //分
        // long diffHours = diff / (60 * 60 * 1000) % 24; //时
        // 若是当天完成，则设置为1天
        int closeDuration = (int) (diff / (24 * 60 * 60 * 1000));//天
        if (closeDuration == 0) {
            closeDuration = (int) (diff / (60 * 60 * 1000) % 24);//时
            if (closeDuration == 0) {
                closeDuration = (int) (diff / (60 * 1000) % 60) == 0 ? 1 : (int) (diff / (60 * 1000) % 60);
                duration = closeDuration + "分";
            } else {
                duration = closeDuration + "时";
            }
        } else {
            duration = closeDuration + "天";
        }
        // 更新对应的审批流程进度,根本文件标识+版本号+is_del=0锁定唯一数据
        /*ApprovalProcess approvalProcess = new ApprovalProcess();
        if (toolsTask.getChangeType() != null && toolsTask.getChangeType().equals(CommonConstants.REVISE)) {
            LocalStorageTemp temp = localStorageTempRepository.findById(storageId).orElseGet(LocalStorageTemp::new);
            approvalProcess = approvalProcessRepository.findByBindingIdAndAndTypeApproveBy(temp.getStorageId(), version, task.getApprovedBy(), false, toolsTask.getFileType());
        } else {
            approvalProcess = approvalProcessRepository.findByBindingIdAndAndTypeApproveBy(storageId, version, task.getApprovedBy(), false, toolsTask.getFileType());
        }*/

        ApprovalProcess approvalProcess = approvalProcessRepository.findByBindingIdAndAndTypeApproveBy(storageId, version, task.getApprovedBy(), false, toolsTask.getFileType());

        List<ApprovalProcess> processList = new ArrayList<>();
        List<ApprovalProcess> formalList = new ArrayList<>();
        if (approvalProcess != null) {
            approvalProcess.setApprovedComment(toolsTask.getComment());
            approvalProcess.setApprovedResult(StringHander(toolsTask.getApproveResult()));
            approvalProcess.setIsDone(true);
            // int closeDuration = (int) (diff / (24 * 60 * 60 * 1000));
            approvalProcess.setDuration(duration);
            approvalProcess.setUpdateBy(SecurityUtils.getCurrentUsername());
            approvalProcessRepository.save(approvalProcess);
            if (approvalProcess.getChangeType() != null && approvalProcess.getChangeType().equals(CommonConstants.REVISE)) {
                // 若是修改则需要关联文件更改正式文件,同步修改正式文件的审批请求
                LocalStorageTemp temp = localStorageTempRepository.findById(storageId).orElseGet(LocalStorageTemp::new);
                ApprovalProcess formalProcess = approvalProcessRepository.findByBindingIdAndAndTypeApproveBy(temp.getStorageId(), version, task.getApprovedBy(), false, toolsTask.getFileType());
                if (formalProcess != null) {
                    formalProcess.setApprovedComment(toolsTask.getComment());
                    formalProcess.setApprovedResult(StringHander(toolsTask.getApproveResult()));
                    formalProcess.setIsDone(true);
                    formalProcess.setDuration(duration);
                    formalProcess.setUpdateBy(SecurityUtils.getCurrentUsername());
                    approvalProcessRepository.save(formalProcess);
                }
                formalList = approvalProcessRepository.findAllByBindingIdAndLaterThanId(temp.getStorageId(), approvalProcess.getId(), false, toolsTask.getFileType());
            }
            processList = approvalProcessRepository.findAllByBindingIdAndLaterThanId(storageId, approvalProcess.getId(), false, toolsTask.getFileType());
        }


        /*
         *
         * 如果后续还有审批进程，则分两大类情况讨论：
         * 1)驳回，则后续流程直接被中止；
         * 2)同意，则直接激活上级任务：即激活下一条审批进度记录并设更新人为当前登陆人
         * 如果后续没进度了，则什么操作都不需要
         */
        // 如果被驳回，则后续审批进度直接中断
        if (ValidationUtil.isNotEmpty(processList)) {
            // 未审批的进度信息设置为中止,剩余项均应设置为已审批---仅文件类型存在
            if (!task.getApproveResult()) {
                processList.forEach(p -> {
                    p.setApprovedResult("已中止");
                    p.setIsDone(true);
                });
                approvalProcessRepository.saveAll(processList);
                // 同步中止正式文件的修改进程
                formalList.forEach(p -> {
                    p.setApprovedResult("已中止");
                    p.setIsDone(true);
                });
                approvalProcessRepository.saveAll(formalList);
            } else {
                // 将下一条审批进度激活并发布任务---仅文件类型存在
                ApprovalProcess laterProcess = processList.get(0);
                ToolsTask laterTask = new ToolsTask();
                laterTask.setPreTrailNo(toolsTask.getPreTrailNo());
                laterTask.setSuffix(laterProcess.getSuffix());
                laterTask.setSize(laterProcess.getSize());
                laterTask.setRealName(laterProcess.getRealName());
                laterTask.setChangeType(laterProcess.getChangeType());
                laterTask.setChangeDesc(laterProcess.getChangeDesc());
                laterTask.setStorageId(laterProcess.getBindingId());
                laterTask.setVersion(laterProcess.getVersion());
                laterTask.setTarPath(laterProcess.getTarPath());
                laterTask.setSrcPath(laterProcess.getSrcPath());
                laterTask.setFileType(laterProcess.getType());
                laterTask.setType(toolsTask.getType());
                laterTask.setApprovedBy(laterProcess.getApprovedBy());
                laterTask.setIsDone(false);
                laterTask.setApproveResult(null);
                laterTask.setComment(null);
                laterTask.setCreateBy(toolsTask.getCreateBy());
                toolsTaskRepository.save(laterTask);
            }

        } else {
            //这是最后一条审批进程(质量部审批)，则审批进度走完
            if (task.getApproveResult() && task.getType().equals(CommonConstants.TRAIL_TYPE_FILE)) {
                // // 文件类型，审批通过，则更新文件：设置文件的状态改为“已发布”，审批状态更改为“已审批”
                LocalStorage localStorage = new LocalStorage();
                if (approvalProcess != null) {
                    if (approvalProcess.getChangeType() != null && approvalProcess.getChangeType().equals(CommonConstants.REVISE)) {
                        // 若是修改则需要关联文件更改正式文件并删除此次的修改
                        LocalStorageTemp temp = localStorageTempRepository.findById(storageId).orElseGet(LocalStorageTemp::new);
                        localStorage = localStorageRepository.findById(temp.getStorageId()).orElseGet(LocalStorage::new);
                        // 将临时信息同步到正式文件上
                        // bindingLocalStorageTempRepository.deleteByStorageTempId(temp.getId());
                        // bindingDeptTempRepository.deleteByStorageTempId(temp.getId());
                        syncTempToFormalFile(localStorage, temp);
                        if (!temp.getFileStatus().equals(CommonConstants.DRAFT_STATUS)) {
                            localStorage.setFileStatus(temp.getFileStatus());
                        } else {
                            localStorage.setFileStatus(CommonConstants.RELEASE_STATUS);
                        }
                        // 功成身退，删除临时信息
                        localStorageTempRepository.delById(temp.getId());
                        localStorage.setLocalStorageTemp(temp);
                        // localStorageTempRepository.deleteById(temp.getId());
                    } else {
                        localStorage = localStorageRepository.findById(storageId).orElseGet(LocalStorage::new);
                        if (!localStorage.getFileStatus().equals(CommonConstants.DRAFT_STATUS)) {
                            localStorage.setFileStatus(localStorage.getFileStatus());
                        } else {
                            localStorage.setFileStatus(CommonConstants.RELEASE_STATUS);
                        }
                    }
                    localStorage.setApprovalStatus(CommonConstants.APPROVED_STATUS);
                    localStorage.setVersion(approvalProcess.getVersion());
                    localStorage.setSuffix(approvalProcess.getSuffix());
                    localStorage.setSize(approvalProcess.getSize());
                    localStorage.setPath(approvalProcess.getTarPath());
                    localStorage.setType(approvalProcess.getType());
                }
                localStorageRepository.save(localStorage);
            } else if (task.getApproveResult() && task.getType().equals(CommonConstants.TRAIL_TYPE_AUDITOR)) {
                // 审核人员批准类型
                Auditor auditor = auditorRepository.findById(task.getStorageId()).orElseGet(Auditor::new);
                if (auditor != null) {
                    auditor.setApprovedTime(new Timestamp(new Date().getTime()));
                    auditor.setApprovalStatus(CommonConstants.AUDIT_PLAN_STATUS_APPROVED);
                    auditorRepository.save(auditor);
                }

            } else if (task.getApproveResult() && task.getType().equals(CommonConstants.TRAIL_TYPE_AUDIT_PLAN)) {
                // 审核人员批准类型
                AuditPlan auditPlan = auditPlanRepository.findById(task.getStorageId()).orElseGet(AuditPlan::new);
                if (auditPlan != null) {
                    auditPlan.setApprovedTime(new Timestamp(new Date().getTime()));
                    auditPlan.setApprovalStatus(CommonConstants.AUDIT_PLAN_STATUS_APPROVED);
                    auditPlanRepository.save(auditPlan);
                }

            }
        }
        // 审批不通过 - 文件审批状态设置为“已作废”,8D? ,审核人员/审核计划状态设置为“被驳回”
        if (!task.getApproveResult() && task.getType().equals(CommonConstants.TRAIL_TYPE_FILE)) {
            // 文件类型
            LocalStorage localStorage = new LocalStorage();
            if (task.getChangeType() != null && task.getChangeType().equals(CommonConstants.REVISE)) {
                // 若是修改则需要关联文件更改正式文件并删除此次的修改
                LocalStorageTemp temp = localStorageTempRepository.findById(storageId).orElseGet(LocalStorageTemp::new);
                localStorage = localStorageRepository.findById(temp.getStorageId()).orElseGet(LocalStorage::new);
                if (temp.getFileStatus().equals(CommonConstants.DRAFT_STATUS)) {
                    // 如果是草稿状态则应该返回到修改后的状态
                    syncTempToFormalFile(localStorage, temp);
                }
                // 功成身退，删除临时信息
                localStorageTempRepository.delById(temp.getId());
                localStorage.setLocalStorageTemp(temp);
            } else {
                localStorage = localStorageRepository.findById(storageId).orElseGet(LocalStorage::new);
            }
            localStorage.setApprovalStatus(CommonConstants.OBSOLETED_STATUS);
//            localStorage.setLocalStorageTemp(null);
            localStorageRepository.save(localStorage);
        } else if (!task.getApproveResult() && task.getType().equals(CommonConstants.TRAIL_TYPE_AUDITOR)) {
            // 审核人员批准类型
            Auditor auditor = auditorRepository.findById(task.getStorageId()).orElseGet(Auditor::new);
            if (auditor != null) {
                auditor.setApprovedTime(new Timestamp(new Date().getTime()));
                auditor.setApprovalStatus(CommonConstants.AUDIT_PLAN_STATUS_REFUSED);
                auditor.setRejectComment(task.getComment());
                auditorRepository.save(auditor);
            }
        } else if (!task.getApproveResult() && task.getType().equals(CommonConstants.TRAIL_TYPE_AUDIT_PLAN)) {
            // 审核计划批准类型
            AuditPlan auditPlan = auditPlanRepository.findById(task.getStorageId()).orElseGet(AuditPlan::new);
            if (auditPlan != null) {
                auditPlan.setApprovedTime(new Timestamp(new Date().getTime()));
                auditPlan.setApprovalStatus(CommonConstants.AUDIT_PLAN_STATUS_REFUSED);
                // 添加驳回理由
                auditPlan.setRejectComment(task.getComment());
                auditPlanRepository.save(auditPlan);
            }
        }
    }

    private void syncTempToFormalFile(LocalStorage localStorage, LocalStorageTemp temp) {
        localStorage.setChangeDesc(temp.getChangeDesc());
        localStorage.setName(temp.getName());
        localStorage.setFileDept(temp.getFileDept());
        localStorage.setFileCategory(temp.getFileCategory());
        localStorage.setFileLevel(temp.getFileLevel());
        localStorage.setFileDesc(temp.getFileDesc());
        localStorage.setFileType(temp.getFileType());
        localStorage.setSecurityLevel(temp.getSecurityLevel());
        localStorage.setPath(temp.getPath());
        localStorage.setSuffix(temp.getSuffix());
        localStorage.setSize(temp.getSize());
        localStorage.setType(temp.getType());
        localStorage.setRealName(temp.getRealName());
        // 文件或发生变化，不再是“临时文件”，取消过期时间
        if (!temp.getFileStatus().equals("temp")) {
            localStorage.setExpirationTime(null);
        }
        localStorage.setExpirationTime(temp.getExpirationTime());
        //删除旧数据，只保存现有数据
        bindingLocalStorageRepository.deleteByStorageId(localStorage.getId());
        bindingDeptRepository.deleteByStorageId(localStorage.getId());

        //绑定文件--需要先清空之前先装载最新数据
        Set<BindingLocalStorageTemp> bindTempFiles = temp.getBindTempFiles();
        List<BindingLocalStorage> bindFiles = new ArrayList<>();
        bindTempFiles.forEach(bindTemp -> {
            BindingLocalStorage bind = new BindingLocalStorage();
            bind.setStorageId(temp.getStorageId());
            bind.setBindingStorageId(bindTemp.getBindingStorageId());
            bindFiles.add(bind);
        });
        bindingLocalStorageRepository.saveAll(bindFiles);
        localStorage.getBindFiles().clear();
        localStorage.getBindFiles().addAll(bindFiles);

        //绑定开放部门--需要先清空之前先装载最新数据
        Set<BindingDept> bindDepts = new HashSet<>();
        Set<BindingDeptTemp> bindTempDepts = temp.getBindTempDepts();
        bindTempDepts.forEach(bind -> {
            BindingDept dept = new BindingDept();
            dept.setStorageId(temp.getStorageId());
            dept.setDeptId(bind.getDeptId());
            bindDepts.add(dept);
        });
        bindingDeptRepository.saveAll(bindDepts);
        localStorage.getBindDepts().clear();
        localStorage.getBindDepts().addAll(bindDepts);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSubmit(List<ToolsTask> tasks) {
        // 批量处理任务
        if (ValidationUtil.isNotEmpty(tasks)) {
            // 过滤8D的审批请求
            tasks.removeIf(task -> task.getType().equals(CommonConstants.TRAIL_TYPE_8D));
            tasks.forEach(task -> {
                // 一键同意
                task.setApproveResult(true);
                submit(task);
            });
        }
    }

    private String StringHander(Boolean approveResult) {
        if (approveResult) {
            return "同意";
        } else {
            return "驳回";
        }
    }
}
