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

import me.zhengjie.domain.LocalStorageTemp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2021-10-20
 */
@Repository
public interface LocalStorageTempRepository extends JpaRepository<LocalStorageTemp, Long>, JpaSpecificationExecutor<LocalStorageTemp> {

    /**
     * 根据元文件ID查询文件备份信息
     *
     * @param storageId 文件ID
     * @return 文件备份信息
     */
    @Query(value = " select * from tool_local_storage_temp where storage_id = ?1 ", nativeQuery = true)
    LocalStorageTemp findByStorageId(Long storageId);

    /**
     * 根据文件标识删除临时信息
     *
     * @param storageId 文件标识
     */
    @Modifying
    @Query(value = "update tool_local_storage_temp set is_del = 1  where storage_id = ?1 ", nativeQuery = true)
    void deleteByStorageId(Long storageId);

    /**
     * 删除待审批项
     *
     * @param id 标识
     */
    @Modifying
    @Query(value = " update tool_local_storage_temp set is_del = 1 where storage_temp_id = ?1 ", nativeQuery = true)
    void delById(Long id);
}