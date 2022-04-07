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

import me.zhengjie.domain.TemplateScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * @author Tong Minjie
 * @date 2020-09-09
 */
@Repository
public interface TemplateScoreRepository extends JpaRepository<TemplateScore, Long>, JpaSpecificationExecutor<TemplateScore> {

    /**
     * 根据模板id查询模板问题清单分数分布
     *
     * @param templateId 模板ID
     * @param isActive   是否激活
     * @return 问题下时间进程信息
     */
    @Query(value = "SELECT * FROM template_score where template_id = ?1 and is_active = ?2", nativeQuery = true)
    List<TemplateScore> findByTemplateId(Long templateId, Boolean isActive);


    /**
     * 根据模板id查询模板问题清单分数分布
     *
     * @param templateId 模板ID
     * @return 问题下时间进程信息
     */
    @Query(value = "SELECT * FROM template_score where template_id = ?1", nativeQuery = true)
    List<TemplateScore> findAllByTemplateId(Long templateId);

    /**
     * 根据模板id查询模板问题清单分数分布
     *
     * @param templateId 模板ID
     * @param itemTypes  项目类型集合
     * @param isActive   是否激活
     * @return 问题下时间进程信息
     */
    @Query(value = "SELECT * FROM template_score where template_id = ?1 and item_type in ?2 and is_active = ?3 ", nativeQuery = true)
    List<TemplateScore> findByTempIdAndItemTypeIn(Long templateId, List<String> itemTypes, Boolean isActive);

    /**
     * 根据模板id查询模板问题清单分数分布
     *
     * @param templateId 模板ID
     * @param itemTypes  项目类型集合
     * @return 问题下时间进程信息
     */
    @Query(value = "SELECT * FROM template_score where template_id = ?1 and item_type in ?2 ", nativeQuery = true)
    List<TemplateScore> findAllByTempIdAndItemTypeIn(Long templateId, List<String> itemTypes);

    /**
     * 根据模板id删除模板问题清单分数分布
     *
     * @param templateId 模板ID
     */
    @Modifying
    @Query(value = " delete from template_score where template_id = ?1 ", nativeQuery = true)
    void deleteByTemplateId(Long templateId);

    @Query(value = "select plan_id from plan_template where template_id = ( " +
            " select template_id from template_score where template_score_id = ?1 " +
            " )", nativeQuery = true)
    Long findPlanIdById(Long scoreId);

    /**
     * 根据模板id删除指定类型的模板问题清单分数信息
     *
     * @param templateId 模板标识
     * @param itemType   项目类型
     */
    @Modifying
    @Query(value = " delete from template_score where template_id = ?1 and item_type = ?2 ", nativeQuery = true)
    void deleteByTemplateIdAndItemType(Long templateId, String itemType);
}
