package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.*;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.repository.*;
import me.zhengjie.service.TrParticipantService;
import me.zhengjie.service.dto.ParticipantQueryByExample;
import me.zhengjie.service.dto.ToolsUserDto;
import me.zhengjie.service.dto.TrainParticipantDto;
import me.zhengjie.service.mapstruct.ToolsUserMapper;
import me.zhengjie.service.mapstruct.TrParticipantMapper;
import me.zhengjie.utils.QueryHelp;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author TongMin Jie
 * @version V1
 * @date 2022/5/24 10:26
 */
@Service
@RequiredArgsConstructor
public class TrParticipantServiceImpl implements TrParticipantService {

    private final TrainParticipantRepository trParticipantRepository;
    private final TrainScheduleRepository trScheduleRepository;
    private final TrParticipantMapper trParticipantMapper;
    private final FileDeptRepository deptRepository;
    private final TrainNewStaffRepository staffRepository;
    private final ToolsUserRepository toolsUserRepository;
    private final ToolsUserMapper toolsUserMapper;


    @Override
    public List<TrainParticipantDto> getByTrScheduleId(Long trScheduleId) {
        List<TrainParticipantDto> list = new ArrayList<>();
        List<TrainParticipant> trParts = trParticipantRepository.findAllByTrScheduleId(trScheduleId);
        if (ValidationUtil.isNotEmpty(trParts)) {
            list = trParticipantMapper.toDto(trParts);
            Set<Long> deptIds = new HashSet<>();
            Map<Long, String> deptMap = new HashMap<>();
            list.forEach(part -> {
                deptIds.add(part.getParticipantDepart());
            });
            List<FileDept> deptList = deptRepository.findByIdIn(deptIds);
            if (ValidationUtil.isNotEmpty(deptList)) {
                deptList.forEach(dept -> {
                    deptMap.put(dept.getId(), dept.getName());
                });
                list.forEach(part -> {
                    part.setParticipantDepartName(deptMap.get(part.getParticipantDepart()));
                });
            }
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(TrainParticipant resource) {
        TrainSchedule schedule = trScheduleRepository.findById(resource.getTrScheduleId()).orElseGet(TrainSchedule::new);
        ValidationUtil.isNull(schedule.getId(), "TrainSchedule", "id", resource.getTrScheduleId());
        int diff = schedule.getTotalNum() - schedule.getCurNum();
        if (diff < 1) {
            throw new BadRequestException("当前【" + schedule.getTrainTitle() + "】已满员，请勿添加！");
        }
        TrainParticipant participant = trParticipantRepository.findByDepartIdAndPartName(resource.getParticipantDepart(), resource.getParticipantName());
        if (participant != null) {
            throw new EntityExistException(TrainParticipant.class, "participantName", resource.getParticipantName());
        }
        if (resource.getIsValid()) {
            schedule.setCurNum(schedule.getCurNum() + 1);
            trScheduleRepository.save(schedule);
            // 参与成功则自动生成相关培训内容
            initTrainStaffInfo(resource, schedule);
        }
        trParticipantRepository.save(resource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(TrainParticipant resource) {
        // 是否设置权限
        TrainParticipant old = trParticipantRepository.findById(resource.getId()).orElseGet(TrainParticipant::new);
        TrainSchedule schedule = trScheduleRepository.findById(resource.getTrScheduleId()).orElseGet(TrainSchedule::new);
        ValidationUtil.isNull(schedule.getId(), "TrainSchedule", "id", resource.getTrScheduleId());
        if (resource.getIsValid() && !old.getIsValid()) {
            int diff = schedule.getTotalNum() - schedule.getCurNum();
            if (diff < 1) {
                throw new BadRequestException("当前【" + schedule.getTrainTitle() + "】已满员，请勿添加！");
            } else {
                TrainParticipant participant = trParticipantRepository.findByDepartIdAndPartName(resource.getParticipantDepart(), resource.getParticipantName());
                if (participant != null && !participant.getId().equals(resource.getId())) {
                    throw new EntityExistException(TrainParticipant.class, "participantName", resource.getParticipantName());
                }
                schedule.setCurNum(schedule.getCurNum() + 1);
                trScheduleRepository.save(schedule);
            }
            // 参与成功则自动生成相关员工培训内容
            initTrainStaffInfo(resource, schedule);
            // todo 若需要考试则自动生成相关考试内容
            if(schedule.getIsExam()) {
                // 若需要考试则自动生成相关考试内容

            }
        } else if (!resource.getIsValid() && old.getIsValid()) {
            schedule.setCurNum(schedule.getCurNum() - 1);
            trScheduleRepository.save(schedule);
            // 参与成功后撤销则自动撤回相关培训内容
            staffRepository.deleteByDepartIdAndStaffName(resource.getParticipantDepart(), resource.getParticipantName());
            // todo 若需要考试则自动生成相关考试内容
        }
        trParticipantRepository.save(resource);
    }

    private void initTrainStaffInfo(TrainParticipant resource, TrainSchedule schedule) {
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
        TrainNewStaff staff = new TrainNewStaff();
        staff.setStaffName(resource.getParticipantName());
        staff.setDepartId(resource.getParticipantDepart());
        staff.setTrScheduleId(schedule.getId());
        staff.setStaffType(userDto.getStaffType());
        staff.setJobType(userDto.getJobType());
        staff.setJobName(userDto.getJobName());
        staff.setSuperior(userDto.getSuperiorName());
        staff.setHireDate(userDto.getHireDate());
        staff.setWorkshop(userDto.getWorkshop());
        staff.setTeam(userDto.getTeam());
        staff.setJobNum(userDto.getJobNum());
        staff.setIsFinished(false);
        staff.setReason("培训尚未结束");
        staffRepository.save(staff);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        // todo 关联查询操作，判断是否影响参与者数据
        trParticipantRepository.deleteAllByIdIn(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(List<TrainParticipant> resources) {
        if (ValidationUtil.isNotEmpty(resources)) {
            Long scheduleId = resources.get(0).getTrScheduleId();
            TrainSchedule schedule = trScheduleRepository.findById(scheduleId).orElseGet(TrainSchedule::new);
            ValidationUtil.isNull(schedule.getId(), "TrainSchedule", "id", scheduleId);
            int diff = schedule.getTotalNum() - schedule.getCurNum();
            // 批量人员姓名校准不好判断，前端配合后端已控制
            Iterator<TrainParticipant> iterator = resources.iterator();
            int toPartNum = 0;
            while (iterator.hasNext()) {
                TrainParticipant participant = iterator.next();
                if (!participant.getIsValid()) {
                    iterator.remove();
                } else {
                    toPartNum++;
                    // 参与成功则自动生成相关培训内容
                    initTrainStaffInfo(participant, schedule);
                    // todo 若需要考试则自动生成相关考试内容
                }
            }
            if (diff < toPartNum) {
                throw new BadRequestException("当前【" + schedule.getTrainTitle() + "】已满员，请勿添加！");
            } else {
                schedule.setCurNum(schedule.getCurNum() + toPartNum);
                trScheduleRepository.save(schedule);
            }
            trParticipantRepository.saveAll(resources);
        }
    }

    @Override
    public List<TrainParticipant> getByExample(ParticipantQueryByExample example) {
        return trParticipantRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, example, criteriaBuilder));
    }
}
