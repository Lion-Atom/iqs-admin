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

import me.zhengjie.domain.EquipAcceptanceDetail;
import me.zhengjie.domain.IssueScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;


/**
 * @author Tong Minjie
 * @date 2022-04-19
 */
@Repository
public interface EquipAcceptanceDetailRepository extends JpaRepository<EquipAcceptanceDetail, Long>, JpaSpecificationExecutor<EquipAcceptanceDetail> {

    /**
     * 根据设备验收ID查询设备验收明细信息
     *
     * @param acceptanceId 设备验收ID
     * @return 设备验收对应的明细信息
     */
    @Query(value = "SELECT * FROM equip_acceptance_detail where acceptance_id = ?1 order by detail_sort", nativeQuery = true)
    List<EquipAcceptanceDetail> findByAcceptanceId(Long acceptanceId);

    /**
     * 根据设备验收ID删除设备验收明细项目
     *
     * @param acceptanceIds 设备验收IDs
     */
    @Modifying
    @Query(value = " delete from equip_acceptance_detail where acceptance_id in ?1 ", nativeQuery = true)
    void deleteByAcceptanceIdIn(Set<Long>  acceptanceIds);
}
