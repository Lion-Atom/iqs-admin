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

import me.zhengjie.domain.TrExamDepartFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2022-05-10
 */
@Repository
public interface TrExamDepartFileRepository extends JpaRepository<TrExamDepartFile, Long>, JpaSpecificationExecutor<TrExamDepartFile> {

    /**
     * 根据关联部门ids删除附件信息
     *
     * @param departIds 关联部门ids
     */
    @Modifying
    @Query(value = " delete  from train_exam_depart_file where depart_id in ?1 ", nativeQuery = true)
    void deleteByDepartIdIn(Set<Long> departIds);

    /**
     * 根据关联部门id删除附件信息
     *
     * @param departId 关联部门id
     */
    @Modifying
    @Query(value = " delete  from train_exam_depart_file where depart_id = ?1 ", nativeQuery = true)
    void deleteByDepartId(Long departId);

    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * 根据关联部门id查询相关附件
     *
     * @param departId 关联部门id
     * @return 关联部门附件信息列表
     */
    @Query(value = " select * from train_exam_depart_file where depart_id = ?1 ", nativeQuery = true)
    List<TrExamDepartFile> findByDepartId(Long departId);

    @Query(value = " select * from train_exam_depart_file where depart_id = ?1 and name = ?2 ", nativeQuery = true)
    TrExamDepartFile findByDepartIdAndName(Long departId, String name);
}
