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
package me.zhengjie.modules.system.repository;

import me.zhengjie.modules.system.domain.ApprovalProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;


/**
 * @author Tong Minjie
 * @date 2021-06-24
 */
@Repository
public interface ApprovalProcessRepository extends JpaRepository<ApprovalProcess, Long>, JpaSpecificationExecutor<ApprovalProcess> {


    /**
     * 根据审批人查询目标审批进度信息
     *
     * @param storageId  对象标识
     * @param version    版本号
     * @param approvedBy 审批人
     * @param isDone     是否已完成
     * @param type      任务类型
     * @return 审批进度信息
     */
    @Query(value = "SELECT * FROM sys_approval_process  WHERE " +
            " binding_id = ?1  " +
            " and version = ?2 " +
            " and approved_by = ?3 " +
            " and is_done = ?4 " +
            " and is_del = 0 " +
            " and type = ?5 ", nativeQuery = true)
    ApprovalProcess findByBindingIdAndAndTypeApproveBy(Long storageId, String version, Long approvedBy, Boolean isDone, String type);


    /**
     * 根据审批人查询目标审批进度信息
     *
     * @param storageId  对象标识
     * @param version    版本号
     * @param approvedBy 审批人
     * @param isDone     是否已完成
     * @param type      任务类型
     * @return 审批进度信息
     */
    @Query(value = "SELECT * FROM sys_approval_process  WHERE " +
            " binding_id = ?1  " +
            " and version = ?2 " +
            " and approved_by = ?3 " +
            " and is_done = ?4 " +
            " and is_del = 0 " +
            " and type = ?5 " +
            " and create_time = ?6", nativeQuery = true)
    ApprovalProcess findFormalByBindingIdAndAndTypeApproveBy(Long storageId, String version, Long approvedBy, Boolean isDone, String type, Timestamp createTime);

    /**
     * 查询未处理的审批进度信息
     *
     * @param storageId 对象标识
     * @param id        审批进度标识
     * @param isDone    是否已完成
     * @param type      任务类型
     * @return 剩余未审批进度记录
     */
    @Query(value = "SELECT * FROM sys_approval_process  WHERE " +
            " binding_id = ?1  " +
            " and approval_process_id > ?2 " +
            " and is_done = ?3 " +
            " and type = ?4 " +
            " and is_del = 0" +
            " order by approval_process_id", nativeQuery = true)
    List<ApprovalProcess> findAllByBindingIdAndLaterThanId(Long storageId, Long id, Boolean isDone, String type);
}