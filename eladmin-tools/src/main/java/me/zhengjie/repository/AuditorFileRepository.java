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

import me.zhengjie.domain.AuditorFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;


/**
 * @author Tong Minjie
 * @date 2020-09-14
 */
@Repository
public interface AuditorFileRepository extends JpaRepository<AuditorFile, Long>, JpaSpecificationExecutor<AuditorFile> {


    /**
     * 根据用户id删除附件信息
     *
     * @param userId 用户ID
     */
    @Modifying
    @Query(value = " delete  from auditor_file where user_id = ?1 ", nativeQuery = true)
    void deleteByUserId(Long userId);


    /**
     * 根据审核人员id删除附件信息
     *
     * @param auditorId 审核人员ID
     */
    @Modifying
    @Query(value = " delete  from auditor_file where auditor_id = ?1 ", nativeQuery = true)
    void deleteTempByAuditorId(Long auditorId);

    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * 根据审核人员id查询相关附件
     *
     * @param auditorId 审核人员ID
     * @return 审核信息
     */
    @Query(value = " select * from auditor_file where auditor_id = ?1 ", nativeQuery = true)
    List<AuditorFile> findByAuditorId(Long auditorId);
}
