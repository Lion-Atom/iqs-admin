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

import me.zhengjie.domain.FileDept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * @author TongMinjie
 * @date 2021-05-14
 */
@Repository
public interface FileDeptRepository extends JpaRepository<FileDept, Long>, JpaSpecificationExecutor<FileDept> {

    /**
     * 根据 PID 查询
     *
     * @param id pid
     * @return /
     */
    List<FileDept> findByPid(Long id);

    @Query(value = "SELECT * FROM `sys_dept` where dept_id = ( " +
            " select dept_id from sys_user where user_id = ?1)", nativeQuery = true)
    FileDept findBySuperiorId(Long superior);

    /**
     * 根据标识集合查询部门信息
     *
     * @param idList 标识集合
     * @return 部门信息
     */
    @Query(value = "SELECT name FROM `sys_dept` where dept_id in ?1", nativeQuery = true)
    List<String> findByIdIn(List<Long> idList);

    /**
     * 根据标识集合查询部门信息
     *
     * @param idList 标识集合
     * @return 部门信息
     */
    @Query(value = "SELECT * FROM `sys_dept` where dept_id in ?1", nativeQuery = true)
    List<FileDept> findByIdIn(Set<Long> idList);
}