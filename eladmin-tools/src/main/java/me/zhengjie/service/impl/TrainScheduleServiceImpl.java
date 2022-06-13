package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.*;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.*;
import me.zhengjie.service.TrainScheduleService;
import me.zhengjie.service.dto.ToolsUserDto;
import me.zhengjie.service.dto.TrainParticipantDto;
import me.zhengjie.service.dto.TrainScheduleDto;
import me.zhengjie.service.dto.TrainScheduleQueryCriteria;
import me.zhengjie.service.mapstruct.ToolsUserMapper;
import me.zhengjie.service.mapstruct.TrParticipantMapper;
import me.zhengjie.service.mapstruct.TrainScheduleMapper;
import me.zhengjie.utils.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author TongMin Jie
 * @version V1
 * @date 2022/5/18 15:01
 */
@Service
@RequiredArgsConstructor
public class TrainScheduleServiceImpl implements TrainScheduleService {

    private final TrainScheduleRepository scheduleRepository;
    private final TrScheduleFileRepository fileRepository;
    private final TrainScheduleMapper scheduleMapper;
    private final TrainParticipantRepository participantRepository;
    private final ScheduleBindingDeptRepository bindingDeptRepository;
    private final FileDeptRepository deptRepository;
    private final TrParticipantMapper trParticipantMapper;
    private final TrainExamDepartRepository examDepartRepository;
    private final TrainExamStaffRepository examStaffRepository;
    private final ToolsUserRepository toolsUserRepository;
    private final ToolsUserMapper toolsUserMapper;
    private final TrainCertificationRepository certificationRepository;

    @Override
    public List<TrainScheduleDto> queryAll(TrainScheduleQueryCriteria criteria) {
        List<TrainScheduleDto> list = new ArrayList<>();
        List<TrainSchedule> schedules = scheduleRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
        if (ValidationUtil.isNotEmpty(schedules)) {
            list = scheduleMapper.toDto(schedules);
            // todo 或填充附件信息、与会人员信息
        }
        return list;
    }

    @Override
    public void download(List<TrainScheduleDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (TrainScheduleDto dto : queryAll) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("培训标题", dto.getTrainTitle());
            if (dto.getIsDelay()) {
                map.put("培训时间", dto.getNewTrainTime());
            } else {
                map.put("培训时间", dto.getTrainTime());
            }
            map.put("培训类型", dto.getTrainType());
            map.put("培训内容", dto.getTrainContent());
            map.put("培训人", dto.getTrainer());
            map.put("培训地点", dto.getTrainLocation());
            map.put("涉及部门", dto.getBindDeptStr());
            map.put("报名截止时间", dto.getRegDeadline());
            map.put("培训机构", dto.getTrainIns());
            map.put("培训费用", dto.getCost());
            map.put("人数限制", dto.getTotalNum());
            map.put("现与会人数", dto.getCurNum());
            map.put("创建日期", dto.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public Map<String, Object> queryAll(TrainScheduleQueryCriteria criteria, Pageable pageable) {
        Map<String, Object> map = new HashMap<>();
        Page<TrainSchedule> page = scheduleRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        List<TrainScheduleDto> list = new ArrayList<>();
        long total = 0L;
        if (ValidationUtil.isNotEmpty(page.getContent())) {
            list = scheduleMapper.toDto(page.getContent());
            list.forEach(schedule -> {
                if (!schedule.getBindDepts().isEmpty()) {
                    // todo 处理涉及部门
                    List<String> deptNames = new ArrayList<>();
                    schedule.getBindDepts().forEach(dept -> {
                        deptNames.add(dept.getName());
                    });
                    schedule.setBindDeptStr(StringUtils.join(deptNames, ","));
                }
                schedule.setFileScopeTags(Arrays.asList(schedule.getFileScope().split(",")));
                List<TrainParticipant> parts = participantRepository.findAllByTrScheduleId(schedule.getId());
                List<TrainParticipantDto> partList = trParticipantMapper.toDto(parts);
                if (ValidationUtil.isNotEmpty(partList)) {
                    initTrPartInfo(partList);
                }
                schedule.setPartList(partList);
            });
            total = page.getTotalElements();
        }
        map.put("content", list);
        map.put("totalElements", total);
        return map;
    }

    private void initTrPartInfo(List<TrainParticipantDto> partList) {
        Set<Long> deptIds = new HashSet<>();
        Map<Long, String> deptMap = new HashMap<>();
        partList.forEach(part -> {
            deptIds.add(part.getParticipantDepart());
        });
        List<FileDept> deptList = deptRepository.findByIdIn(deptIds);
        if (ValidationUtil.isNotEmpty(deptList)) {
            deptList.forEach(dept -> {
                deptMap.put(dept.getId(), dept.getName());
            });
            partList.forEach(part -> {
                part.setParticipantDepartName(deptMap.get(part.getParticipantDepart()));
            });
        }
    }

    @Override
    public TrainSchedule findById(Long id) {
        // todo  暂时用不到，不想写。。。
        TrainSchedule schedule = scheduleRepository.findById(id).orElseGet(TrainSchedule::new);
        ValidationUtil.isNull(schedule.getId(), "TrainSchedule", "id", id);
        return schedule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(TrainSchedule resource) {
        // 验证修改权限
        checkEditAuthorized(resource);
        initScheduleInfo(resource);
        judgeScheduleStatus(resource);
        TrainSchedule schedule = scheduleRepository.findById(resource.getId()).orElseGet(TrainSchedule::new);
        ValidationUtil.isNull(schedule.getId(), "TrainSchedule", "id", resource.getId());
        //  先判断是否需要同步考试数据,依据：是否考试标志发生变化
        if (resource.getIsExam() && !schedule.getIsExam()) {
            List<TrainParticipant> parts = participantRepository.findAllByTrScheduleIdAndIsValid(schedule.getId(), true);
            if (ValidationUtil.isNotEmpty(parts)) {
                // 若更改为需要考试则需要自动生成考试信息
                parts.forEach(part -> {
                    initExamInfo(part, schedule);
                });
            }
        } else if (!resource.getIsExam() && schedule.getIsExam()) {
            // 若更改为不需要考试则自动删除已有考试记录
            examStaffRepository.deleteAllByTrScheduleId(resource.getId());
            // 若更改为不需要考试则自动删除已有证书记录
            certificationRepository.deleteAllByTrScheduleId(resource.getId());
        }
        scheduleRepository.save(resource);
    }

    private void initExamInfo(TrainParticipant resource, TrainSchedule schedule) {
        // 若需要考试则自动生成相关考试内容
        // 1.判断/生成考试所在部门
        TrainExamDepart examDepart = examDepartRepository.findByDepartId(resource.getParticipantDepart());
        if (examDepart != null) {
            // 若该部门未启用则置为启用状态
            if (!examDepart.getEnabled()) {
                examDepart.setEnabled(true);
                examDepartRepository.save(examDepart);
            }
        } else {
            TrainExamDepart newDept = new TrainExamDepart();
            newDept.setDepartId(resource.getParticipantDepart());
            newDept.setEnabled(true);
            examDepartRepository.save(newDept);
        }
        // 2.生成考试信息
        initExamStaffInfo(resource, schedule);
    }

    private void initExamStaffInfo(TrainParticipant resource, TrainSchedule schedule) {
        ToolsUserDto userDto = getToolsUserDto(resource);
        TrainExamStaff examStaff = new TrainExamStaff();
        examStaff.setDepartId(resource.getParticipantDepart());
        examStaff.setHireDate(userDto.getHireDate());
        examStaff.setStaffType(userDto.getStaffType());
        examStaff.setJobNum(userDto.getJobNum());
        examStaff.setJobName(userDto.getJobName());
        examStaff.setJobType(userDto.getJobType());
        examStaff.setSuperior(userDto.getSuperiorName());
        examStaff.setWorkshop(userDto.getWorkshop());
        examStaff.setTeam(userDto.getTeam());
        examStaff.setStaffName(resource.getParticipantName());
        examStaff.setTrScheduleId(schedule.getId());
        examStaff.setIsAuthorize(false);
        examStaffRepository.save(examStaff);
    }

    private ToolsUserDto getToolsUserDto(TrainParticipant resource) {
        ToolsUser user = toolsUserRepository.findByUsername(resource.getParticipantName());
        ToolsUserDto userDto = toolsUserMapper.toDto(user);
        if (ValidationUtil.isNotEmpty(Collections.singletonList(user.getJobs()))) {
            List<ToolsJob> jobList = new ArrayList<>(user.getJobs());
            userDto.setJobName(jobList.get(0).getName());
        }
        if (user.getSuperiorId() != null) {
            ToolsUser sup = toolsUserRepository.findById(user.getSuperiorId()).orElseGet(ToolsUser::new);
            ValidationUtil.isNull(sup.getId(), "ToolsUser", "id", user.getSuperiorId());
            userDto.setSuperiorName(sup.getUsername());
        } else if (user.getDept() != null) {
            if (user.getDept().getPid() != null) {
                ToolsUser sup = toolsUserRepository.findByDeptIdAndIsMaster(user.getDept().getPid(), true);
                if (sup != null) {
                    userDto.setSuperiorName(sup.getUsername());
                }
            } else {
                userDto.setSuperiorName(userDto.getUsername());
            }
        }
        return userDto;
    }

    private void initScheduleBindDepts(TrainSchedule resource, Set<ScheduleBindingDept> bindDepts) {
        if (ValidationUtil.isNotEmpty(Collections.singletonList(bindDepts))) {
            List<ScheduleBindingDept> bindingDeptList = new ArrayList<>();
            bindDepts.forEach(bindDeptId -> {
                ScheduleBindingDept bind = new ScheduleBindingDept();
                bind.setTrScheduleId(resource.getId());
                bind.setDeptId(bindDeptId.getDeptId());
                bindingDeptList.add(bind);
            });
            bindingDeptRepository.saveAll(bindingDeptList);
            // 重新装载，防止数据丢失
//            resource.getBindDepts().addAll(bindingDeptList);
        }
    }

    private void checkEditAuthorized(TrainSchedule schedule) {
        Boolean isAdmin = SecurityUtils.isAdmin();
        String username = SecurityUtils.getCurrentUsername();
        if (!schedule.getCreateBy().equals(username) && !isAdmin) {
            // 非创建者亦非管理员则无权限修改和删除
            throw new BadRequestException("No Access!抱歉，您暂无权更改此项！");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(TrainScheduleDto resource) {
        TrainSchedule schedule = scheduleMapper.toEntity(resource);
        initScheduleInfo(schedule);
        judgeScheduleStatus(schedule);
        TrainSchedule newSchedule = scheduleRepository.save(schedule);
        // 添加涉及部门
//        initScheduleBindDepts(newSchedule, resource.getBindDepts());
        // 文件列表
        if (ValidationUtil.isNotEmpty(resource.getMaterialFileList())) {
            resource.getMaterialFileList().forEach(file -> {
                file.setTrScheduleId(newSchedule.getId());
            });
            fileRepository.saveAll(resource.getMaterialFileList());
            // todo 同步培训材料

        }
        if (ValidationUtil.isNotEmpty(resource.getExamFileList())) {
            resource.getExamFileList().forEach(file -> {
                file.setTrScheduleId(newSchedule.getId());
            });
            fileRepository.saveAll(resource.getExamFileList());
            // todo 同步培训题库
        }
    }

    private void judgeScheduleStatus(TrainSchedule resource) {
        long current = new Date().getTime();//当前时间毫秒数
        long time = resource.getIsDelay() ? resource.getNewTrainTime().getTime() : resource.getTrainTime().getTime();
        int diff = (int) (time - current);
        // 培训时间与当前时间对比，若是小于当前时间则自动关闭
        if (diff <= 0) {
            resource.setScheduleStatus(CommonConstants.SCHEDULE_STATUS_CLOSED);
        } else {
            resource.setScheduleStatus(CommonConstants.SCHEDULE_STATUS_OPENED);
        }
    }

    private void initScheduleInfo(TrainSchedule schedule) {
        if (schedule.getIsDelay() != null && !schedule.getIsDelay()) {
            schedule.setNewTrainTime(null);
            schedule.setDelayDesc(null);
        }
        if (schedule.getIsRemind() != null && !schedule.getIsRemind()) {
            schedule.setRemindDays(null);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        for (Long id : ids) {
            TrainSchedule schedule = scheduleRepository.findById(id).orElseGet(TrainSchedule::new);
            ValidationUtil.isNull(schedule.getId(), "TrainSchedule", "id", id);
            checkEditAuthorized(schedule);
        }
        scheduleRepository.deleteAllByIdIn(ids);
        // 删除参与者信息
        participantRepository.deleteAllByTrScheduleIdIn(ids);
        // 删除考生考试信息
        examStaffRepository.deleteAllByTrScheduleIdIn(ids);
        // todo 删除用户认证信息

    }
}
