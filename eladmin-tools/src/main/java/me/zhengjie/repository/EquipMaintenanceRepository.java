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

import me.zhengjie.domain.EquipMaintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2022-04-12
 */
@Repository
public interface EquipMaintenanceRepository extends JpaRepository<EquipMaintenance, Long>, JpaSpecificationExecutor<EquipMaintenance> {

    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * @param equipId 设备ID
     * @return 设备信息
     */
    @Query(value = "SELECT * FROM equip_maintenance where equipment_id = ?1 order by maintain_date asc ", nativeQuery = true)
    List<EquipMaintenance> findByEquipId(Long equipId);

    /**
     * @param equipId 设备ID
     * @return 设备信息
     */
    @Query(value = "SELECT * FROM equip_maintenance where equipment_id = ?1 and maintain_date= (select max(maintain_date) from equip_maintenance where equipment_id = ?1) limit 1", nativeQuery = true)
    EquipMaintenance findMaxByEquipId(Long equipId);
}