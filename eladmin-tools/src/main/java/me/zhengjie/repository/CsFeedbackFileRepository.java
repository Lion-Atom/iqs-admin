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

import me.zhengjie.domain.CaliOrgFile;
import me.zhengjie.domain.CsFeedbackFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;


/**
 * @author Tong Minjie
 * @date 2022-07-15
 */
@Repository
public interface CsFeedbackFileRepository extends JpaRepository<CsFeedbackFile, Long>, JpaSpecificationExecutor<CsFeedbackFile> {


    /**
     * 根据客户反馈id删除附件信息
     *
     * @param csFeedbackIds 客户反馈IDS
     */
    @Modifying
    @Query(value = " delete  from cs_feedback_file where cs_feedback_id in ?1 ", nativeQuery = true)
    void deleteByCsFeedbackIdIn(Set<Long> csFeedbackIds);


    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * 根据客户反馈id查询相关附件
     *
     * @param csFeedbackId 客户反馈id
     * @return 客户反馈附件信息列表
     */
    @Query(value = " select * from cs_feedback_file where cs_feedback_id = ?1 ", nativeQuery = true)
    List<CsFeedbackFile> findByCsFeedbackId(Long csFeedbackId);

}
