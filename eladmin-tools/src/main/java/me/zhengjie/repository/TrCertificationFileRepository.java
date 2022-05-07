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

import me.zhengjie.domain.TrCertificationFile;
import me.zhengjie.domain.TrNewStaffFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2022-05-07
 */
@Repository
public interface TrCertificationFileRepository extends JpaRepository<TrCertificationFile, Long>, JpaSpecificationExecutor<TrCertificationFile> {

    /**
     * 根据认证ids删除附件信息
     *
     * @param trCertificationIds 认证ids
     */
    @Modifying
    @Query(value = " delete  from train_certification_file where train_certification_id in ?1 ", nativeQuery = true)
    void deleteByTrNewStaffIdIn(Set<Long> trCertificationIds);

    /**
     * 根据认证id删除附件信息
     *
     * @param trCertificationId 认证id
     */
    @Modifying
    @Query(value = " delete  from train_certification_file where train_certification_id = ?1 ", nativeQuery = true)
    void deleteByTrCertificationId(Long trCertificationId);

    /**
     * 根据Id删除
     *
     * @param ids /
     */
    @Modifying
    @Query(value = " update train_certification_file set is_del = 1 where tr_certification_file_id in ?1 ", nativeQuery = true)
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * 根据认证id查询相关附件
     *
     * @param trCertificationId 认证id
     * @return 认证附件信息列表
     */
    @Query(value = " select * from train_certification_file where train_certification_id = ?1 and is_del = 0 ", nativeQuery = true)
    List<TrCertificationFile> findByTrCertificationId(Long trCertificationId);
}
