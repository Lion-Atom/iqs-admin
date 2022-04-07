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

import me.zhengjie.domain.BindingDept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * @author TongMinjie
 * @date 2021-05-11
 */
@Repository
public interface BindingDeptRepository extends JpaRepository<BindingDept, Long>, JpaSpecificationExecutor<BindingDept> {

    /**
     * 根据文件标识解绑已绑定的文件
     *
     * @param storageId 文件标识
     */
    @Modifying
    @Query(value = " delete from tool_file_dept where storage_id = ?1 ", nativeQuery = true)
    void deleteByStorageId(Long storageId);

    /**
     * 根据绑定部门标识解绑目标文件
     *
     * @param deptId 被绑定的部门标识
     */
    @Modifying
    @Query(value = " delete from tool_file_dept where dept_id = ?1 ", nativeQuery = true)
    void updateAllByDeptId(Long deptId);
}