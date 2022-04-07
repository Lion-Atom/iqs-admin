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

import me.zhengjie.domain.TeamMember;
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
public interface TeamMembersRepository extends JpaRepository<TeamMember, Long>, JpaSpecificationExecutor<TeamMember> {

    /**
     * 根据问题id查询问题信息
     *
     * @param issueId 问题id
     * @return 问题下时间进程信息
     */
    @Query(value = "SELECT * FROM team_members where issue_id = ?1 order by is_leader desc", nativeQuery = true)
    List<TeamMember> findByIssueId(Long issueId);

    /**
     * 根据问题id查询问题信息
     *
     * @param issueId 问题id
     * @param userId  用户id
     * @return 问题下时间进程信息
     */
    @Query(value = "SELECT * FROM team_members where issue_id = ?1 and user_id = ?2 ", nativeQuery = true)
    List<TeamMember> findByIssueIdAndUserId(Long issueId, Long userId);

    /**
     * 根据问题id查询问题信息
     *
     * @param issueId   问题id
     * @param userId    用户id
     * @param teamRoles 成员角色
     * @return 问题下时间进程信息
     */
    @Query(value = "SELECT * FROM team_members where " +
            " issue_id = ?1  " +
            " and user_id = ?2 " +
            " and team_role in ?3", nativeQuery = true)
    List<TeamMember> findByIssueIdAndUserIdAndRole(Long issueId, Long userId, List<String> teamRoles);

    /**
     * 根据问题id删除小组成员信息
     *
     * @param issueId 问题标识
     */
    @Modifying
    @Query(value = " delete  from team_members where issue_id = ?1 ", nativeQuery = true)
    void deleteByIssueId(Long issueId);

    @Query(value = "SELECT * FROM team_members where " +
            " issue_id = ?1 " +
            " and team_id = ?2 " +
            " and user_id = ?3 ", nativeQuery = true)
    TeamMember findByIssueIdAndTeamIdAndUserId(Long issueId, Long teamId, Long userId);

    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * 反查问题标识
     *
     * @param id id
     * @return 问题标识
     */
    @Query(value = " select issue_id  from team_members where member_id = ?1 ", nativeQuery = true)
    Long findIssueIdById(Long id);
}
