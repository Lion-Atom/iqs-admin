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

import me.zhengjie.domain.RepairPart;
import me.zhengjie.domain.RepairFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2022-04-11
 */
@Repository
public interface RepairPartRepository extends JpaRepository<RepairPart, Long>, JpaSpecificationExecutor<RepairPart> {

    /**
     * 根据设备维修ids删除附件信息
     *
     * @param repairIds 设备维修ids
     */
    @Modifying
    @Query(value = " delete  from repair_part where repair_id in ?1 ", nativeQuery = true)
    void deleteByRepairIdIn(Set<Long> repairIds);

    @Modifying
    @Query(value = " delete from repair_part where repair_id = ?1 ", nativeQuery = true)
    void deleteByRepairId(Long repairId);

    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * 根据设备维修id查询相关附件
     *
     * @param repairId 设备维修id
     * @return 设备维修附件信息列表
     */
    @Query(value = " select * from repair_part where repair_id = ?1 ", nativeQuery = true)
    List<RepairPart> findByRepairId(Long repairId);


    /**
     * @param repairIds 设备维修IDs
     * @return 设备维修对应的确认单信息
     */
    @Query(value = " select * from repair_part where repair_id in ?1 ", nativeQuery = true)
    List<RepairPart> findByRepairIdIn(Set<Long> repairIds);
}
