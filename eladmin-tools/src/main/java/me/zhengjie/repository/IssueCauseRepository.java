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

import me.zhengjie.domain.IssueCause;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/28 17:56
 */
@Repository
public interface IssueCauseRepository extends JpaRepository<IssueCause, Long>, JpaSpecificationExecutor<IssueCause> {

    /**
     * 根据 PID 查询
     *
     * @param id pid
     * @return /
     */
    List<IssueCause> findByPid(Long id);

    /**
     * 获取顶级原因
     *
     * @return /
     */
    List<IssueCause> findByPidIsNull();


    /**
     * 判断是否存在子节点
     *
     * @param pid /
     * @return /
     */
    int countByPid(Long pid);

    /**
     * 根据ID更新sub_count
     *
     * @param count /
     * @param id    /
     */
    @Modifying
    @Query(value = " update issue_cause set sub_count = ?1 and is_exact = ?3 where cause_id = ?2 ", nativeQuery = true)
    void updateSubCntById(Integer count, Long id,Boolean isExact);


    /**
     * 根据名称查询原因信息
     *
     * @param name 原因名称
     * @return 原因信息
     */
    IssueCause findByName(String name);

    /**
     * 根据问题id查询问题信息
     *
     * @param issueId 问题id
     * @return 问题下时间进程信息
     */
    @Query(value = "SELECT * FROM issue_cause where issue_id = ?1 ", nativeQuery = true)
    List<IssueCause> findByIssueId(Long issueId);

    /**
     * 根据问题id查询问题信息
     *
     * @param issueId 问题id
     * @return 问题下时间进程信息
     */
    @Query(value = "SELECT * FROM issue_cause where issue_id = ?1 and pid is null ", nativeQuery = true)
    List<IssueCause> findByIssueIdAndPidIsNull(Long issueId);

    /**
     * 根据问题id删除缺陷定位信息
     *
     * @param issueId 问题标识
     */
    @Modifying
    @Query(value = " delete  from issue_cause where issue_id = ?1 ", nativeQuery = true)
    void deleteByIssueId(Long issueId);

    @Modifying
    @Query(value = " update issue_cause set is_exact = ?2 where issue_id = ?1 ", nativeQuery = true)
    void upToNotExactCntById(Long id, Boolean isExact);

    /**
     * 获取当前已有节点原因占比总值
     *
     * @param issueId 问题标识
     * @return 根节点原因占比总值
     */
    @Query(value = "SELECT SUM(contribution) FROM issue_cause where " +
            " issue_id = ?1 " +
            " and pid is null " +
            " and cause_id <> ?2", nativeQuery = true)
    Double getCountByPidIsNull(Long issueId, Long topId);

    @Modifying
    @Query(value = " update issue_cause set contribution = ?2  where cause_id = ?1 ", nativeQuery = true)
    void updateContribution(Long id, Double nowPer);

    /**
     * 查询已有兄弟节点原因之和
     *
     * @param pid 父节点标识
     * @return 兄弟原因占比之和
     */
    @Query(value = " select SUM(contribution) FROM issue_cause where pid = ?1 ", nativeQuery = true)
    Double getContributionSumByPId(Long pid);

    /**
     * 查询将来的兄弟节点原因之和
     *
     * @param pid 父节点标识
     * @return 兄弟原因占比之和
     */
    @Query(value = " select SUM(contribution) FROM issue_cause where pid = ?1 and cause_id <> ?2 ", nativeQuery = true)
    Double getBroContributionSumByPId(Long pid,Long id);
}