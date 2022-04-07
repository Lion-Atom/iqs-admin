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

import me.zhengjie.domain.TimeManagement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


/**
 * @author Tong Minjie
 * @date 2020-07-22
 */
@Repository
public interface TimeManagementRepository extends JpaRepository<TimeManagement, Long>, JpaSpecificationExecutor<TimeManagement> {

    /**
     * 根据问题id查询问题信息
     *
     * @param issueId 问题id
     * @return 问题下时间进程信息
     */
    @Query(value = "SELECT * FROM tool_time_management where issue_id = ?1 ", nativeQuery = true)
    TimeManagement findByIssueId(Long issueId);

    /**
     * 根据问题id删除时间进程
     *
     * @param issueId 问题标识
     */
    @Modifying
    @Query(value = " delete  from  tool_time_management where issue_id = ?1 ", nativeQuery = true)
    void deleteByIssueId(Long issueId);
}
