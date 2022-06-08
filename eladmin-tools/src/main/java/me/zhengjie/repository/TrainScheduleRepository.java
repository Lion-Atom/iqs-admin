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

import me.zhengjie.domain.TrainNewStaff;
import me.zhengjie.domain.TrainSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2022-05-18
 */
@Repository
public interface TrainScheduleRepository extends JpaRepository<TrainSchedule, Long>, JpaSpecificationExecutor<TrainSchedule> {

    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * @param b 是否需要提前提醒
     * @return 需要提前提星星的培训日程安排信息
     */
    @Query(value = "select * from train_schedule where is_remind = ?1 ", nativeQuery = true)
    List<TrainSchedule> findAllByIsRemind(boolean b);

    /**
     * 根据标识集合查询培训计划信息
     *
     * @param scheduleIds 标识集合
     * @return 培训计划信息
     */
    @Query(value = "SELECT * FROM train_schedule where train_schedule_id in ?1", nativeQuery = true)
    List<TrainSchedule> findByIdIn(Set<Long> scheduleIds);
}