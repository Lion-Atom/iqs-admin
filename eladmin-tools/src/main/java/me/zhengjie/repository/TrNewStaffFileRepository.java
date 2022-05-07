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
 * @date 2022-05-06
 */
@Repository
public interface TrNewStaffFileRepository extends JpaRepository<TrNewStaffFile, Long>, JpaSpecificationExecutor<TrNewStaffFile> {

    /**
     * 根据新员工培训ids删除附件信息
     *
     * @param trNewStaffIds 新员工培训ids
     */
    @Modifying
    @Query(value = " delete  from train_new_staff_file where new_staff_train_id in ?1 ", nativeQuery = true)
    void deleteByTrNewStaffIdIn(Set<Long> trNewStaffIds);

    /**
     * 根据新员工培训id删除附件信息
     *
     * @param trNewStaffId 新员工培训id
     */
    @Modifying
    @Query(value = " delete  from train_new_staff_file where new_staff_train_id = ?1 ", nativeQuery = true)
    void deleteByTrNewStaffId(Long trNewStaffId);

    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * 根据新员工培训id查询相关附件
     *
     * @param trNewStaffId 新员工培训id
     * @return 新员工培训附件信息列表
     */
    @Query(value = " select * from train_new_staff_file where new_staff_train_id = ?1 ", nativeQuery = true)
    List<TrNewStaffFile> findByTrNewStaffId(Long trNewStaffId);
}
