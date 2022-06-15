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

import me.zhengjie.domain.TrainCertification;
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
 * @date 2022-05-07
 */
@Repository
public interface TrainCertificationRepository extends JpaRepository<TrainCertification, Long>, JpaSpecificationExecutor<TrainCertification> {

    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * @param departId  部门ID
     * @param staffName 员工姓名
     * @return 新员工集合
     */
    @Query(value = "select * from train_certification where depart_id = ?1 and staff_name = ?2 ", nativeQuery = true)
    List<TrainCertification> findAllByDepartIdAndStaffName(Long departId, String staffName);

    /**
     * @param certificationType 认证类型
     * @param jobType           工种类型
     * @param staffName         员工姓名
     * @return 新员工集合
     */
    @Query(value = "select * from train_certification where certification_type=?1 and job_type=?2 and staff_name = ?3 ", nativeQuery = true)
    TrainCertification findAllByCertTypeAndJobTypeAndStaffName(String certificationType, String jobType, String staffName);

    /**
     * @param certificationType 认证类型
     * @param trScheduleId      培训计划ID
     * @param staffName         员工姓名
     * @return 新员工集合
     */
    @Query(value = "select * from train_certification where certification_type=?1 and train_schedule_id=?2 and staff_name = ?3 ", nativeQuery = true)
    TrainCertification findAllByCertTypeAndTrScheduleIdAndStaffName(String certificationType, Long trScheduleId, String staffName);

    /**
     * @param b 是否需要提前提醒
     * @return 需要提前提星星的培训认证证书信息
     */
    @Query(value = "select * from train_certification where is_remind = ?1 ", nativeQuery = true)
    List<TrainCertification> findAllByIsRemind(Boolean b);

    /**
     * 删除培训计划下考试证书信息
     *
     * @param trScheduleId 培训计划ID
     */
    @Modifying
    @Query(value = "delete from train_certification where train_schedule_id = ?1", nativeQuery = true)
    void deleteAllByTrScheduleId(Long trScheduleId);

    /**
     * 删除培训计划下考试证书信息
     *
     * @param trScheduleId 培训计划ID
     */
    @Modifying
    @Query(value = "delete from train_certification where certification_type=?1 and train_schedule_id=?2 and staff_name = ?3 ", nativeQuery = true)
    void deleteAllByCertTypeAndTrScheduleIdAndStaffName(String certificationType, Long trScheduleId, String staffName);

    /**
     * 删除培训计划下考试证书
     * @param trScheduleIds 培训计划IDS
     */
    @Modifying
    @Query(value = "delete from train_certification where train_schedule_id in ?1 ", nativeQuery = true)
    void deleteAllByTrScheduleIdIn(Set<Long> trScheduleIds);
}