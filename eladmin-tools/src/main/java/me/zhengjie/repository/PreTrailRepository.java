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

import me.zhengjie.domain.PreTrail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2021-06-07
 */
@Repository
public interface PreTrailRepository extends JpaRepository<PreTrail, Long>, JpaSpecificationExecutor<PreTrail> {

    /**
     * @param id 文件标识
     * @return 文件信息
     */
    @Query(value = "select * from tool_pre_trail where storage_id = ?1 and is_del=?2 order by create_time desc", nativeQuery = true)
    List<PreTrail> findAllByStorageId(Long id, Long isDel);

    /**
     * @param id 文件标识
     * @return 文件信息
     */
    @Query(value = "select * from tool_pre_trail where " +
            " storage_id = ?1 " +
            " and type = ?2" +
            " and is_del = ?3 " +
            " and is_done = ?4 ", nativeQuery = true)
    PreTrail findTaskByStorageId(Long id, String type, Long isDel, Boolean isDone);

    /**
     * @param id 文件标识
     * @return 文件信息
     */
    @Query(value = "select * from tool_pre_trail where storage_id = ?1 " +
            " and version = ?2 " +
            " and is_del = ?3 " +
            " order by create_time desc", nativeQuery = true)
    List<PreTrail> findAllByStorageIdAndVersion(Long id, String version, Long isDel);

    /**
     * @param id 文件标识
     * @return 文件信息
     */
    @Query(value = "select * from tool_pre_trail where storage_id = ?1 and is_del=?2 order by version asc", nativeQuery = true)
    List<PreTrail> findAllByStorageIdSortByVersion(Long id, Long isDel);

    /**
     * @return 当天文件待审批新增数目
     */
    @Query(value = "select count(pre_trail_id) from tool_pre_trail  where date_format(create_time,'%Y-%m-%d') = ?1", nativeQuery = true)
    Integer getCountByDateTime(String time);

    /**
     * 删除待审批项
     *
     * @param id 标识
     */
    @Modifying
    @Query(value = " update tool_pre_trail set is_del = 1 where pre_trail_id = ?1 ", nativeQuery = true)
    void delById(Long id);

    /**
     * 删除待审批项
     *
     * @param storageId  对象标识
     * @param version    版本号
     * @param changeType 变更类型
     */
    @Modifying
    @Query(value = " update tool_pre_trail set is_del = 1 where " +
            " storage_id = ?1 " +
            " and version = ?2 " +
            " and change_type = ?3 ", nativeQuery = true)
    void delByStorageIdAndVersionAndChangeType(Long storageId, String version, String changeType);

    /**
     * 删除待审批项
     *
     * @param storageId        对象标识
     * @param version          版本号
     * @param lastModifiedDate 最近一次修改的数据
     */
    @Modifying
    @Query(value = " delete form tool_pre_trail where " +
            " storage_id = ?1 " +
            " and version = ?2 " +
            " and create_time > ?3", nativeQuery = true)
    void delByStorageIdAndVersionAndModifiedTime(Long storageId, String version, Timestamp lastModifiedDate);


    /**
     * @param id 文件标识
     * @return 文件信息
     */
    @Query(value = "select * from tool_pre_trail where storage_id = ?1 " +
            " and version = ?2 " +
            " and create_time > ?3 ", nativeQuery = true)
    List<PreTrail> findAllByStorageIdAndVersionAndModifiedTime(Long id, String version, Timestamp lastModifiedDate);

    /**
     * @param storageId        文件标识
     * @param lastModifiedDate 最近一次修改的数据
     * @return 最近一次修改的有效数据
     */
    @Query(value = "select * from tool_pre_trail where storage_id = ?1 " +
            " and version = ?2 " +
            " and create_time < ?3 " +
            " order by create_time desc", nativeQuery = true)
    List<PreTrail> findMinByModifiedTime(Long storageId, String version, Timestamp lastModifiedDate);

    /**
     * 删除待审批项
     *
     * @param storageId 标识
     */
    @Modifying
    @Query(value = " update tool_pre_trail set is_del = 1 where storage_id = ?1 ", nativeQuery = true)
    void deleteAllByStorageId(Long storageId);

    /**
     * 删除待审批项
     *
     * @param storageId 标识
     */
    @Modifying
    @Query(value = " delete from tool_pre_trail where storage_id = ?1 ", nativeQuery = true)
    void deleteByStorageId(Long storageId);

    /**
     * @param fileId 文件标识
     * @return 文件信息
     */
    @Query(value = "select * from tool_pre_trail where " +
            " storage_id = ?1 " +
            " and is_del = ?2 " +
            " and change_type is not null " +
            " and pre_trail_no = ( " +
            " select distinct pre_trail_no from tool_pre_trail where " +
            " storage_id = ?1 " +
            " and is_del = ?2 " +
            " and create_time= (select max(create_time) from tool_pre_trail where storage_id = ?1 and is_del = ?2) " +
            " );", nativeQuery = true)
    List<PreTrail> findFileLatestRecords(Long fileId, Long notDel);


    /**
     * @param id    文件标识
     * @param isDel 是否有效
     * @param type  任务类型
     * @return 文件信息
     */
    @Query(value = "select * from tool_pre_trail where " +
            " storage_id = ?1  " +
            " and is_del= ?2 " +
            " and type = ?3 " +
            " order by create_time desc", nativeQuery = true)
    PreTrail findAllByIssueIdAndType(Long id, Long isDel, String type);

    /**
     * 删除待审批项
     *
     * @param ids 标识集合
     */
    @Modifying
    @Query(value = " update tool_pre_trail set is_del = 1 where storage_id in ?1 ", nativeQuery = true)
    void deleteAllByStorageIdIn(Set<Long> ids);

    /**
     * 删除审批项
     *
     * @param ids 标识集合
     */
    @Modifying
    @Query(value = " delete from tool_pre_trail  where storage_id in ?1 ", nativeQuery = true)
    void physicalDeleteAllByStorageIdIn(Set<Long> ids);

    @Modifying
    @Query(value = "delete from tool_pre_trail where " +
            " storage_id = ?1 " +
            " and type = ?2" +
            " and is_del = ?3 " +
            " and is_done = ?4 ", nativeQuery = true)
    void delByStorageIdAndIsDel(Long auditorId, String trailTypeAuditor, Long isDel, boolean b);
}