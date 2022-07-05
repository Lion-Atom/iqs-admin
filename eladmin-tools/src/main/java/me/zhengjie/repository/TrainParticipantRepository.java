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
package me.zhengjie.repository;

import me.zhengjie.domain.TrainParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2022-05-24
 */
@Repository
public interface TrainParticipantRepository extends JpaRepository<TrainParticipant, Long>, JpaSpecificationExecutor<TrainParticipant> {

    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * @param trScheduleId 培训日程安排ID
     * @return 培训日程安排参与者信息
     */
    @Query(value = "select * from train_participant where train_schedule_id = ?1 order by is_valid desc,train_participant_id asc", nativeQuery = true)
    List<TrainParticipant> findAllByTrScheduleId(Long trScheduleId);

    /**
     * @param trScheduleId 培训日程安排ID
     * @param isValid 有效性
     * @return 培训日程安排参与者信息
     */
    @Query(value = "select * from train_participant where train_schedule_id = ?1 and is_valid = ?2 order by train_participant_id asc", nativeQuery = true)
    List<TrainParticipant> findAllByTrScheduleIdAndIsValid(Long trScheduleId,Boolean isValid);

    /**
     * 查询参与者信息
     *
     * @param userId   参与者ID
     * @return 参与者信息
     */
    @Query(value = "select * from train_participant where train_schedule_id = ?1 and user_id = ?2 ", nativeQuery = true)
    TrainParticipant findByTrScheduleIdAndUserId(Long trScheduleId,Long userId);

    /**
     * @param trScheduleIds 培训日程安排IDS
     */
    @Modifying
    @Query(value = "delete from train_participant where train_schedule_id in ?1 ", nativeQuery = true)
    void deleteAllByTrScheduleIdIn(Set<Long> trScheduleIds);
}