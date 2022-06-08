package me.zhengjie.service;

import me.zhengjie.domain.TrainParticipant;
import me.zhengjie.service.dto.ParticipantQueryByExample;
import me.zhengjie.service.dto.TrainParticipantDto;

import java.util.List;
import java.util.Set;

/**
 * @author TongMin Jie
 * @version V1
 * @date 2022/5/24 10:12
 */
public interface TrParticipantService {

    /**
     * 查询日程安排下参与者信息
     *
     * @param trScheduleId 培训日程安排ID
     * @return 培训日程安排参与者信息
     */
    List<TrainParticipantDto> getByTrScheduleId(Long trScheduleId);

    /**
     * 新增
     *
     * @param resource 培训日程安排参与者信息
     */
    void create(TrainParticipant resource);

    /**
     * 更新
     *
     * @param resource 培训日程安排参与者信息
     */
    void update(TrainParticipant resource);

    /**
     * 删除
     *
     * @param ids 培训日程安排参与者IDS
     */
    void delete(Set<Long> ids);

    /**
     * 批量保存参与信息
     *
     * @param resources 培训日程列表信息
     */
    void batchSave(List<TrainParticipant> resources);

    /**
     * 条件查询参与人员
     *
     * @param example 查询条件
     * @return 参与者信息列表
     */
    List<TrainParticipant> getByExample(ParticipantQueryByExample example);
}
