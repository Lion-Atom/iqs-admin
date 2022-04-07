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

import me.zhengjie.domain.IssueConclusion;
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
public interface IssueConclusionRepository extends JpaRepository<IssueConclusion, Long>, JpaSpecificationExecutor<IssueConclusion> {

    /**
     * 根据问题id查询问题信息
     *
     * @param issueId 问题id
     * @return 问题下时间进程信息
     */
    @Query(value = "SELECT * FROM issue_conclusion where issue_id = ?1 ", nativeQuery = true)
    IssueConclusion findByIssueId(Long issueId);

    /**
     * 根据问题id删除缺陷定位信息
     *
     * @param issueId 问题标识
     */
    @Modifying
    @Query(value = " delete  from issue_conclusion where issue_id = ?1 ", nativeQuery = true)
    void deleteByIssueId(Long issueId);
}
