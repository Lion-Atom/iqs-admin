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

import me.zhengjie.domain.ScheduleBindingDept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * @author TongMinjie
 * @date 2022-06-06
 */
@Repository
public interface ScheduleBindingDeptRepository extends JpaRepository<ScheduleBindingDept, Long>, JpaSpecificationExecutor<ScheduleBindingDept> {

    /**
     * 根据培训计划标识解绑涉及部门
     *
     * @param trScheduleId 培训计划标识
     */
    @Modifying
    @Query(value = " delete from tool_schedule_dept where train_schedule_id = ?1 ", nativeQuery = true)
    void deleteByTrScheduleId(Long trScheduleId);

    /**
     * 根据绑定部门标识解绑目标文件
     *
     * @param deptId 被绑定的部门标识
     */
    @Modifying
    @Query(value = " delete from tool_schedule_dept where dept_id = ?1 ", nativeQuery = true)
    void updateAllByDeptId(Long deptId);
}