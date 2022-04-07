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

import me.zhengjie.domain.IssueAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * @author Tong Minjie
 * @date 2020-07-27
 */
@Repository
public interface IssueActionRepository extends JpaRepository<IssueAction, Long>, JpaSpecificationExecutor<IssueAction> {

    /**
     * 根据问题id查询措施信息
     *
     * @param issueId 问题id
     * @return 问题下时间进程信息
     */
    @Query(value = "SELECT * FROM issue_action where issue_id = ?1 ", nativeQuery = true)
    List<IssueAction> findByIssueId(Long issueId);

    /**
     * 根据问题id删除指定措施信息
     *
     * @param issueId 问题标识
     */
    @Modifying
    @Query(value = " delete  from issue_action where issue_id = ?1 and type in ?2", nativeQuery = true)
    void deleteByIssueIdAndInStepNames(Long issueId,List<String> stepNames);

    /**
     * 根据问题id删除措施信息
     *
     * @param issueId 问题标识
     */
    @Modifying
    @Query(value = " delete  from issue_action where issue_id = ?1 ", nativeQuery = true)
    void deleteByIssueId(Long issueId);

    /**
     * 根据名称查询措施信息
     *
     * @param name 措施名称
     * @return 措施信息
     */
    @Query(value = " select * from issue_action where name=?1 and issue_id = ?2 ", nativeQuery = true)
    IssueAction findByNameAndIssueId(String name, Long issueId);

    /**
     * @param issueId 问题标识
     * @param status  措施状态
     * @param type    步骤名称
     * @param isCon   是否是围堵措施
     * @return
     */
    @Query(value = "SELECT * FROM issue_action where " +
            " issue_id = ?1 " +
            " and (status <> ?2 or status is null)  " +
            " and type = ?3 " +
            " and is_con = ?4 ", nativeQuery = true)
    List<IssueAction> findCanRemoveByIssueId(Long issueId, String status, String type, Boolean isCon);

    /**
     * 根据分析ID查询永久措施
     *
     * @param analysisId 分析ID
     * @return 永久措施
     */
    @Query(value = "SELECT u.* FROM issue_action u, analysis_action r WHERE" +
            " u.action_id = r.action_id AND r.analysis_id = ?1", nativeQuery = true)
    List<IssueAction> findByAnalysisId(Long analysisId);

    /**
     * @param currentUserId 个人ID
     * @return 个人8D任务
     */
    @Query(value = " select * from issue_action where responsible_id = ?1 ", nativeQuery = true)
    List<IssueAction> findByResponsibleId(Long currentUserId);
}
