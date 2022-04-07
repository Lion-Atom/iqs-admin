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

import me.zhengjie.domain.ChangeFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;


/**
 * @author Tong Minjie
 * @date 2022-01-19
 */
@Repository
public interface ChangeFileRepository extends JpaRepository<ChangeFile, Long>, JpaSpecificationExecutor<ChangeFile> {

    /**
     * 根据变更ID删除附件信息
     *
     * @param changeIds 变更IDs
     */
    @Modifying
    @Query(value = " delete  from change_file where change_id in ?1 ", nativeQuery = true)
    void deleteByChangeIdIn(Set<Long> changeIds);

    /**
     * 查询相关变更附件
     *
     * @param changeId 变更ID
     * @param fileType 文件所属
     * @return 相关变更附件
     */
    @Query(value = " select * from change_file where change_id = ?1 and file_type = ?2 and factor_id is null", nativeQuery = true)
    List<ChangeFile> findByChangeIdAndFileType(Long changeId, String fileType);

    /**
     * 查询相关变更客户审核附件
     *
     * @param templateId 变更ID
     * @param factorId   变更因素ID
     * @return 变更客户审核附件信息
     */
    @Query(value = " select * from change_file where change_id = ?1 and factor_id = ?2 and file_type = ?3", nativeQuery = true)
    List<ChangeFile> findBySupplierIdAndFactorIdAndFileType(Long templateId, Long factorId, String fileType);

    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * @param factorIds 因素ids
     */
    @Modifying
    @Query(value = " delete  from change_file where factor_id in ?1 ", nativeQuery = true)
    void deleteByFactorIdIn(Set<Long> factorIds);
}
