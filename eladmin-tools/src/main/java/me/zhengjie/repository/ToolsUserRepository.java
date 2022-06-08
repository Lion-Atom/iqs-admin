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

import me.zhengjie.domain.ToolsUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author Zheng Jie
 * @date 2018-11-22
 */
public interface ToolsUserRepository extends JpaRepository<ToolsUser, Long>, JpaSpecificationExecutor<ToolsUser> {

    /**
     * 根据用户名查询
     *
     * @param username 用户名
     * @return /
     */
    ToolsUser findByUsername(String username);
    /**
     * 根据角色中的部门查询
     *
     * @param deptId /
     * @return /
     */
    @Query(value = "SELECT u.* FROM sys_user u, sys_dept d WHERE " +
            " u.dept_id = d.dept_id " +
            " AND d.dept_id = ?1 " +
            " AND u.is_depart_master = ?2 " +
            " AND u.enabled = true ", nativeQuery = true)
    ToolsUser findByDeptIdAndIsMaster(Long deptId, Boolean isDepartMaster);
}
