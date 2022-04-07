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

import me.zhengjie.domain.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2020-07-22
 */
@Repository
public interface IssueRepository extends JpaRepository<Issue, Long>, JpaSpecificationExecutor<Issue> {


    /**
     * 根据问题标题查询问题信息
     *
     * @param issueTitle 问题标题
     * @return 问题信息
     */
    @Query(value = "SELECT * FROM tool_issue where issue_title = ?1 ", nativeQuery = true)
    Issue findByIssueTitle(String issueTitle);

    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    @Query(value = "select * from tool_issue where date_format(create_time,'%Y-%m-%d')= DATE_SUB(curdate(),INTERVAL 0 DAY)", nativeQuery = true)
    List<Issue> getIssueByCreateTime();

    /**
     * 根据问题id清空8D痕迹
     *
     * @param id 问题标识
     */
    @Modifying
    @Query(value = " update tool_issue set " +
            " close_time = null " +
            " and leader_id = null " +
            " and score = null " +
            " and clean_time = ?2 " +
            " where issue_id = ?1 ", nativeQuery = true)
    void clearByIssueId(Long id, Timestamp clearTime);

    /**
     * 根据问题执行选择查询问题信息列表
     *
     * @param hasReport 执行选择
     * @return 问题信息
     */
    @Query(value = "SELECT * FROM tool_issue where has_report = ?1 ", nativeQuery = true)
    List<Issue> findByHasReport(String hasReport);


    /**
     * 根据问题执行选择查询问题信息列表
     *
     * @return 问题信息
     */
    @Query(value = "SELECT * FROM tool_issue where has_report is null ", nativeQuery = true)
    List<Issue> findByHasReportIsNull();

    /**
     * @return 当天文件分类新增数目
     */
    @Query(value = "select count(issue_id) from tool_issue  where date_format(create_time,'%Y-%m-%d') = ?1", nativeQuery = true)
    Integer getCountByDateTime(String time);

    /**
     * @return 文件分类总数
     */
    @Query(value = "select count(issue_id) from tool_issue", nativeQuery = true)
    Integer getIssueCount();
}
