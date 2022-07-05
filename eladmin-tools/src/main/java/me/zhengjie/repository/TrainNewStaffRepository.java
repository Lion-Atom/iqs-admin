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
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2022-05-06
 */
@Repository
public interface TrainNewStaffRepository extends JpaRepository<TrainNewStaff, Long>, JpaSpecificationExecutor<TrainNewStaff> {

    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * @param trScheduleId 培训计划ID
     * @param userId       员工ID
     */
    @Modifying
    @Query(value = "delete from train_new_staff where train_schedule_id = ?1 and user_id = ?2 ", nativeQuery = true)
    void deleteByTrScheduleIdAndUserId(Long trScheduleId, Long userId);

    /**
     * @param userId       员工ID
     * @param trScheduleId 培训计划ID
     * @return 新员工集合
     */
    @Query(value = "select * from train_new_staff where train_schedule_id = ?1 and user_id = ?2 ", nativeQuery = true)
    TrainNewStaff findAllByTrScheduleIdAndUserId(Long trScheduleId, Long userId);

    /**
     * @param trScheduleId 培训计划ID
     * @return 员工培训信息列表
     */
    @Query(value = "select * from train_new_staff where train_schedule_id = ?1", nativeQuery = true)
    List<TrainNewStaff> findAllByTrScheduleId(Long trScheduleId);

    @Modifying
    @Query(value = "delete from train_new_staff where train_schedule_id = ?1", nativeQuery = true)
    void deleteAllByTrScheduleIdIn(Set<Long> ids);

    /**
     * 查询员工培训记录列表
     *
     * @param userId 员工ID
     * @return 员工培训记录信息列表
     */
    @Query(value = "select * from train_new_staff where user_id = ?1 ", nativeQuery = true)
    List<TrainNewStaff> findAllByUserId(Long userId);
}