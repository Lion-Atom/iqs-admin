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

import me.zhengjie.domain.FileApprovalProcess;
import me.zhengjie.domain.PreTrail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;


/**
 * @author Tong Minjie
 * @date 2021-06-24
 */
@Repository
public interface FileApprovalProcessRepository extends JpaRepository<FileApprovalProcess, Long>, JpaSpecificationExecutor<FileApprovalProcess> {

    /**
     * 删除审批清单
     *
     * @param bindingId 标识
     */
    @Modifying
    @Query(value = " update sys_approval_process set is_del = 1 where binding_id = ?1 ", nativeQuery = true)
    void deleteAllByBindingId(Long bindingId);

    /**
     * 删除指定版本的审批清单
     *
     * @param bindingId  标识
     * @param version    版本号
     * @param changeType 变更类型
     */
    @Modifying
    @Query(value = " update sys_approval_process set is_del = 1 " +
            " where binding_id = ?1 " +
            " and version = ?2 " +
            " and change_type = ?3 ", nativeQuery = true)
    void deleteAllByBindingIdAndVersionAndChangeType(Long bindingId, String version, String changeType);


    /**
     * 删除同期指定版本的审批清单
     *
     * @param bindingId  标识
     * @param version    版本号
     * @param changeType 变更类型
     * @param createTime 创建时间
     */
    @Modifying
    @Query(value = " update sys_approval_process set is_del = 1 " +
            " where binding_id = ?1 " +
            " and create_time = ?2" +
            " and version = ?3 " +
            " and change_type = ?4 ", nativeQuery = true)
    void deleteAllByBindingIdAndVersionAndChangeType(Long bindingId, Timestamp createTime, String version, String changeType);

    /**
     * 删除指定版本的审批清单
     *
     * @param bindingId        标识
     * @param version          版本号
     * @param lastModifiedDate 最近一次修改的数据
     */
    @Modifying
    @Query(value = " update sys_approval_process set is_del = 1 " +
            " where binding_id = ?1 " +
            " and version = ?2 " +
            " and create_time > ?3", nativeQuery = true)
    void deleteAllByBindingIdAndVersionAndModifiedTime(Long bindingId, String version, Timestamp lastModifiedDate);

    /**
     * @param bindingId        目标标识
     * @param version          版本号
     * @param lastModifiedDate 最近一次修改的数据
     * @return 最近一次修改的有效数据
     */
    @Query(value = "select * from sys_approval_process where binding_id = ?1 " +
            " and version = ?2 " +
            " and create_time < ?3 " +
            " order by create_time desc", nativeQuery = true)
    List<FileApprovalProcess> findMinByModifiedTime(Long bindingId, String version, Timestamp lastModifiedDate);

    /**
     * @param bindingId        目标标识
     * @param version          版本号
     * @param lastModifiedDate 最近一次修改的数据
     * @return 最近一次修改的有效数据
     */
    @Query(value = "select * from sys_approval_process where binding_id = ?1 " +
            " and version = ?2 " +
            " and create_time > ?3 ", nativeQuery = true)
    List<FileApprovalProcess> findByBindingIdAndVersionAndModifiedTime(Long bindingId, String version, Timestamp lastModifiedDate);

    /**
     * @param fileId 文件标识
     * @return 文件信息
     */
    @Query(value = "SELECT * FROM sys_approval_process " +
            " where binding_id = ?1 " +
            " and is_del = ?2 " +
            " and create_time = (select max(create_time) from sys_approval_process where binding_id  = ?1 and is_del = ?2)  " +
            " limit 1", nativeQuery = true)
    List<FileApprovalProcess> findAllByLastCreateTime(Long fileId, Long notDel);

    /**
     * @param fileId 文件标识
     * @param notDel 有效性判断
     * @param typeList 类型集合
     * @return 文件信息
     */
    @Query(value = "SELECT * FROM sys_approval_process " +
            " where binding_id = ?1 " +
            " and is_del = ?2 " +
            " and type in ?3 " +
            " and create_time = (select max(create_time) from sys_approval_process where binding_id  = ?1 and is_del = ?2)  " +
            " limit 1", nativeQuery = true)
    FileApprovalProcess findByLastCreateTime(Long fileId, Long notDel,List<String> typeList);


    /**
     * @param fileId 文件标识
     * @return 文件信息
     */
    @Query(value = "SELECT * FROM sys_approval_process " +
            " where binding_id = ?1 " +
            " and is_del = ?2 " +
            " and process_no like ?3 " +
            "order by approval_process_id ", nativeQuery = true)
    List<FileApprovalProcess> findTeamProcessList(Long fileId, Long notDel, String process_no);

    @Query(value = "select * from sys_approval_process where binding_id = ?1 " +
            " and is_del = ?2 " +
            " and type in ?3 " +
            " order by version ", nativeQuery = true)
    List<FileApprovalProcess> findByBindingId(Long fileId, Long notDel,List<String> typeList);
}