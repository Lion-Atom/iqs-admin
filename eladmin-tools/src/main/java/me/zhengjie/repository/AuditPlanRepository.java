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

import me.zhengjie.domain.AuditPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2021-09-06
 */
@Repository
public interface AuditPlanRepository extends JpaRepository<AuditPlan, Long>, JpaSpecificationExecutor<AuditPlan> {

    /**
     * 根据名称查询审核计划信息
     *
     * @param realName 审核计划全称
     * @return 等级信息
     */
    AuditPlan findByRealName(String realName);

    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    @Query(value = "select count(*) from audit_plan where to_days(create_time) = to_days(now()) ", nativeQuery = true)
    Integer findTodayCountByCreateTime();

    @Query(value = "SELECT count(plan_id) FROM audit_plan where status = ?1 ", nativeQuery = true)
    int getCountByStatus(String status);

    @Query(value = "SELECT count(plan_id) FROM audit_plan", nativeQuery = true)
    int getAuditorCount();

    @Query(value = "SELECT * FROM audit_plan where status <> ?1", nativeQuery = true)
    List<AuditPlan> findAllByStatusNotEqual(String status);

    @Query(value = "SELECT * FROM audit_plan where type = ?1 ", nativeQuery = true)
    List<AuditPlan> findByType(String type);

    @Query(value = "SELECT count(plan_id) FROM audit_plan where reason = ?1 ", nativeQuery = true)
    int findByReason(String reason);

    @Query(value = "SELECT * FROM audit_plan order by create_time asc limit 1  ", nativeQuery = true)
    AuditPlan findFirstByCreateTime();

    @Query(value = "SELECT * FROM audit_plan order by create_time desc limit 1  ", nativeQuery = true)
    AuditPlan findLastByCreateTime();

    @Query(value = " SELECT * FROM audit_plan where plan_id = ( " +
            " select plan_id from plan_report where plan_report_id = ?1 ) ", nativeQuery = true)
    AuditPlan findByReportId(Long planReportId);

    @Query(value = "SELECT count(*) FROM audit_plan where year(create_time) = ?1 and status = ?2 ", nativeQuery = true)
    int findCountByYearAndStatus(int year,String status);


    @Query(value = "SELECT count(*) FROM audit_plan where month(create_time) = ?1 and status = ?2 ", nativeQuery = true)
    int findCountByMonthAndStatus(int month,String status);
}