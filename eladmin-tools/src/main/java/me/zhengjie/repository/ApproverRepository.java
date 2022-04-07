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

import me.zhengjie.domain.Approver;
import me.zhengjie.service.dto.AuditorSmallDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2020-06-30
 */
public interface ApproverRepository extends JpaRepository<Approver, Long>, JpaSpecificationExecutor<Approver> {


    /**
     * 根据角色中的部门查询
     *
     * @param deptId /
     * @return /
     */
    @Query(value = "SELECT u.* FROM sys_user u, sys_dept d WHERE " +
            "u.dept_id = d.dept_id AND d.dept_id = ?1 AND u.is_depart_master = ?2 ", nativeQuery = true)
    List<Approver> findByDeptIdAndIsMaster(Long deptId, Boolean isDepartMaster);

    @Query(value = "SELECT u.* FROM sys_user u, sys_dept d WHERE " +
            "u.dept_id = d.dept_id AND u.user_id in ?1 ", nativeQuery = true)
    List<Approver> findByIdIn(List<Long> userIds);

    @Query(value = "SELECT u.* FROM sys_user u, sys_dept d WHERE " +
            "u.dept_id = d.dept_id AND u.user_id in ?1 ", nativeQuery = true)
    List<Approver> findByIdIn(Set<Long> userIds);
}
