package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.TrainSchedule;
import me.zhengjie.repository.TrScheduleFileRepository;
import me.zhengjie.repository.TrainScheduleRepository;
import me.zhengjie.service.TrainScheduleService;
import me.zhengjie.service.dto.TrainScheduleDto;
import me.zhengjie.service.dto.TrainScheduleQueryCriteria;
import me.zhengjie.service.mapstruct.TrainScheduleMapper;
import me.zhengjie.utils.FileUtil;
import me.zhengjie.utils.PageUtil;
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
        Page<TrainSchedule> page = scheduleRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        return PageUtil.toPage(page);
    }

    @Override
    public TrainScheduleDto findById(Long id) {
        // todo  暂时用不到，不想写。。。
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(TrainSchedule resource) {
        initScheduleInfo(resource);
        scheduleRepository.save(resource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(TrainScheduleDto resource) {
        TrainSchedule schedule = scheduleMapper.toEntity(resource);
        initScheduleInfo(schedule);
        TrainSchedule newSchedule = scheduleRepository.save(schedule);
        // 文件列表
        if (ValidationUtil.isNotEmpty(resource.getFileList())) {
            resource.getFileList().forEach(file -> {
                file.setTrScheduleId(newSchedule.getId());
            });
            fileRepository.saveAll(resource.getFileList());
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
        scheduleRepository.deleteAllByIdIn(ids);
    }
}
