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

import me.zhengjie.domain.IssueScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * @author Tong Minjie
 * @date 2020-09-09
 */
@Repository
public interface IssueScoreRepository extends JpaRepository<IssueScore, Long>, JpaSpecificationExecutor<IssueScore> {

    /**
     * 根据问题id查询8D分数分布
     *
     * @param issueId 问题id
     * @return 问题下时间进程信息
     */
    @Query(value = "SELECT * FROM issue_score where issue_id = ?1 ", nativeQuery = true)
    List<IssueScore> findByIssueId(Long issueId);

    /**
     * 根据问题id删除8D分数分布
     *
     * @param issueId 问题标识
     */
    @Modifying
    @Query(value = " delete from issue_score where issue_id = ?1 ", nativeQuery = true)
    void deleteByIssueId(Long issueId);
}
