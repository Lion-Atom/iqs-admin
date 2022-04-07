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

import me.zhengjie.domain.ApReportQuestion;
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
public interface ApReportQuestionRepository extends JpaRepository<ApReportQuestion, Long>, JpaSpecificationExecutor<ApReportQuestion> {


    /**
     * 根据审核计划ID删除附件信息
     *
     * @param reportId 审核报告ID
     */
    @Modifying
    @Query(value = " delete  from plan_report_question where plan_report_id = ?1 ", nativeQuery = true)
    void deleteByReportId(Long reportId);

    /**
     * 根据审核计划ID查询执行信息
     *
     * @param reportId 审核报告ID
     * @return 审核信息
     */
    @Query(value = " select * from plan_report_question where plan_report_id = ?1 ", nativeQuery = true)
    List<ApReportQuestion> findByReportId(Long reportId);

    /**
     * 根据审核计划id删除报告信息
     *
     * @param ids 问题标识集合
     */
    @Modifying
    @Query(value = " delete  from plan_report_question where report_question_id in ?1 ", nativeQuery = true)
    void deleteByIdIn(Set<Long> ids);

    /**
     * 根据名称查询问题信息
     *
     * @param name 问题标题
     * @return 等级信息
     */
    ApReportQuestion findByName(String name);

    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * 反查审核计划ID
     *
     * @param quesId 问题ID
     * @return /
     */
    @Query(value = "select plan_id from plan_report where " +
            " plan_report_id = (  " +
            " SELECT plan_report_id FROM plan_report_question where report_question_id = ?1 )", nativeQuery = true)
    Long findPlanIdByQuesId(Long quesId);

    /**
     * 根据审核计划id删除报告信息
     *
     * @param planIds 审核计划IDs
     */
    @Modifying
    @Query(value = " delete from plan_report_question where plan_report_id in (" +
            " select plan_report_id from plan_report where  " +
            "  plan_id in ?1 ) ", nativeQuery = true)
    void deleteByPlanIdIn(Set<Long> planIds);
}
