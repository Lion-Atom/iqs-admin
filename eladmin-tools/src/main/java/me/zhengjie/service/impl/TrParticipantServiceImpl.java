package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.TrainParticipant;
import me.zhengjie.domain.TrainSchedule;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.repository.TrainParticipantRepository;
import me.zhengjie.repository.TrainScheduleRepository;
import me.zhengjie.service.TrParticipantService;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

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


    @Override
    public List<TrainParticipant> getByTrScheduleId(Long trScheduleId) {
        return trParticipantRepository.findAllByTrScheduleId(trScheduleId);
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
        TrainParticipant participant = trParticipantRepository.findByDepartNameAndPartName(resource.getParticipantDepart(), resource.getParticipantName());
        if (participant != null) {
            throw new EntityExistException(TrainParticipant.class, "participantName", resource.getParticipantName());
        }
        if (resource.getIsValid()) {
            schedule.setCurNum(schedule.getCurNum() + 1);
            trScheduleRepository.save(schedule);
        }
        trParticipantRepository.save(resource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(TrainParticipant resource) {
        // todo 是否设置权限

        TrainParticipant old = trParticipantRepository.findById(resource.getId()).orElseGet(TrainParticipant::new);
        TrainSchedule schedule = trScheduleRepository.findById(resource.getTrScheduleId()).orElseGet(TrainSchedule::new);
        ValidationUtil.isNull(schedule.getId(), "TrainSchedule", "id", resource.getTrScheduleId());
        if (resource.getIsValid() && !old.getIsValid()) {
            int diff = schedule.getTotalNum() - schedule.getCurNum();
            if (diff < 1) {
                throw new BadRequestException("当前【" + schedule.getTrainTitle() + "】已满员，请勿添加！");
            } else {
                TrainParticipant participant = trParticipantRepository.findByDepartNameAndPartName(resource.getParticipantDepart(), resource.getParticipantName());
                if (participant != null && !participant.getId().equals(resource.getId())) {
                    throw new EntityExistException(TrainParticipant.class, "participantName", resource.getParticipantName());
                }
                schedule.setCurNum(schedule.getCurNum() + 1);
                trScheduleRepository.save(schedule);
            }
        } else if (!resource.getIsValid() && old.getIsValid()) {
            schedule.setCurNum(schedule.getCurNum() - 1);
            trScheduleRepository.save(schedule);
        }
        trParticipantRepository.save(resource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        trParticipantRepository.deleteAllByIdIn(ids);
    }
}
