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

import me.zhengjie.domain.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2022-03-14
 */
@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long>, JpaSpecificationExecutor<Equipment> {

    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * @param equipNum 设备编号
     * @return 设备保养信息
     */
    @Query(value = "SELECT * FROM tools_equipment where equip_num = ?1", nativeQuery = true)
    Equipment findByEquipNum(String equipNum);

    /**
     * @param assetNum 资产号
     * @return 设备保养信息
     */
    @Query(value = "SELECT * FROM tools_equipment where asset_num = ?1", nativeQuery = true)
    Equipment findByAssetNum(String assetNum);

    /**
     * 查询需要提醒的设备保养信息列表
     *
     * @return 设备保养信息
     */
    @Query(value = "SELECT * FROM tools_equipment where is_remind = true ", nativeQuery = true)
    List<Equipment> findByIsRemind();

    @Query(value = "SELECT * FROM tools_equipment where equipment_id in ?1 ", nativeQuery = true)
    List<Equipment> findByIdIn(Set<Long> equipIds);

    @Modifying
    @Query(value = "update tools_equipment set status = '待验收' where equipment_id in (" +
            " select equipment_id from equip_acceptance where acceptance_id in ?1 ) ", nativeQuery = true)
    void rollbackEquipStatus(Set<Long> ids);


    /**
     * @return 保养中的设备信息
     */
    @Query(value = "select * from tools_equipment where maintain_due_date is not null", nativeQuery = true)
    List<Equipment> findByMaintainDueDateIsNotNull();
}