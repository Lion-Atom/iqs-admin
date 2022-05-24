package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.TrainParticipant;
import me.zhengjie.domain.TrainSchedule;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.TrScheduleFileRepository;
import me.zhengjie.repository.TrainParticipantRepository;
import me.zhengjie.repository.TrainScheduleRepository;
import me.zhengjie.service.TrainScheduleService;
import me.zhengjie.service.dto.TrainScheduleDto;
import me.zhengjie.service.dto.TrainScheduleQueryCriteria;
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
            map.put("涉及部门", dto.getDepartment());
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
                if (schedule.getDepartment() != null) {
                    schedule.setDepartTags(schedule.getDepartment().split(","));
                }
                List<TrainParticipant> parts = participantRepository.findAllByTrScheduleId(schedule.getId());
                schedule.setPartList(parts);
            });
            total = page.getTotalElements();
        }
        map.put("content", list);
        map.put("totalElements", total);
        return map;
    }

    @Override
    public TrainScheduleDto findById(Long id) {
        // todo  暂时用不到，不想写。。。
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(TrainSchedule resource) {
        // 验证修改权限
        checkEditAuthorized(resource);
        initScheduleInfo(resource);
        judgeScheduleStatus(resource);
        scheduleRepository.save(resource);
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
        // 文件列表
        if (ValidationUtil.isNotEmpty(resource.getFileList())) {
            resource.getFileList().forEach(file -> {
                file.setTrScheduleId(newSchedule.getId());
            });
            fileRepository.saveAll(resource.getFileList());
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
    }
}
