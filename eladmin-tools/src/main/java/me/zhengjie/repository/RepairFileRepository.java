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
public interface RepairFileRepository extends JpaRepository<RepairFile, Long>, JpaSpecificationExecutor<RepairFile> {

    /**
     * 根据设备维修ids删除附件信息
     *
     * @param repairIds 设备维修ids
     */
    @Modifying
    @Query(value = " delete  from equip_repair_file where repair_id in ?1 ", nativeQuery = true)
    void deleteByRepairIdIn(Set<Long> repairIds);

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
    @Query(value = " select * from equip_repair_file where repair_id = ?1 ", nativeQuery = true)
    List<RepairFile> findByRepairId(Long repairId);

    /**
     * 删除文件
     *
     * @param repairId 设备维修id
     * @param realName 文件名称
     */
    @Modifying
    @Query(value = " delete from equip_repair_file where repair_id = ?1 and real_name =?2", nativeQuery = true)
    void deleteByRepairIdAndRealName(Long repairId, String realName);

    /**
     * @param repairIds 设备维修IDs
     * @return 设备维修对应的确认单信息
     */
    @Query(value = " select * from equip_repair_file where repair_id in ?1 ", nativeQuery = true)
    List<RepairFile> findByRepairIdIn(Set<Long> repairIds);
}
