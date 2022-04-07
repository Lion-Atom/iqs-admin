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

import me.zhengjie.domain.AuditPlanExecute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;


/**
 * @author Tong Minjie
 * @date 2020-09-14
 */
@Repository
public interface AuditPlanExecuteRepository extends JpaRepository<AuditPlanExecute, Long>, JpaSpecificationExecutor<AuditPlanExecute> {


    /**
     * 根据审核计划ID删除附件信息
     *
     * @param planId 审核计划ID
     */
    @Modifying
    @Query(value = " delete  from plan_execute where plan_id = ?1 ", nativeQuery = true)
    void deleteByPlanId(Long planId);

    /**
     * 根据审核计划ID查询执行信息
     *
     * @param planId 审核计划ID
     * @return 审核信息
     */
    @Query(value = " select * from plan_execute where plan_id = ?1 ", nativeQuery = true)
    AuditPlanExecute findByPlanId(Long planId);

    /**
     * 根据审核计划id删除执行信息
     *
     * @param planIds 审核计划IDs
     */
    @Modifying
    @Query(value = " delete  from plan_execute where plan_id in ?1 ", nativeQuery = true)
    void deleteByPlanIdIn(Set<Long> planIds);
}
