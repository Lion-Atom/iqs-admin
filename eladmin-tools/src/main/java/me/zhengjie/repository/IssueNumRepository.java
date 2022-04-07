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

import me.zhengjie.domain.IssueNum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;


/**
 * @author Tong Minjie
 * @date 2020-07-22
 */
@Repository
public interface IssueNumRepository extends JpaRepository<IssueNum, Long>, JpaSpecificationExecutor<IssueNum> {

    /**
     * 根据问题id查询问题信息
     *
     * @param issueId 问题id
     * @return 问题下时间进程信息
     */
    @Query(value = "SELECT * FROM tool_issue_number where issue_id = ?1 ", nativeQuery = true)
    List<IssueNum> findByIssueId(Long issueId);

    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    @Query(value = "SELECT * FROM tool_issue_number where issue_id = ?1 and ca_part_num = ?2 ", nativeQuery = true)
    List<IssueNum> findByIssueIdAndCaPartNum(Long id, String caPartNum);

    /**
     * 反查问题标识
     *
     * @param id id
     * @return 问题标识
     */
    @Query(value = " select *  from tool_issue_number where number_id = ?1 ", nativeQuery = true)
    IssueNum findIssueIdById(Long id);

    /**
     * 根据问题id删除相关数据信息
     *
     * @param issueId 问题标识
     */
    @Modifying
    @Query(value = " delete  from tool_issue_number where issue_id = ?1 ", nativeQuery = true)
    void deleteByIssueId(Long issueId);
}
