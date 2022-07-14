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

import me.zhengjie.domain.EquipmentFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;


/**
 * @author Tong Minjie
 * @date 2022-03-29
 */
@Repository
public interface EquipmentFileRepository extends JpaRepository<EquipmentFile, Long>, JpaSpecificationExecutor<EquipmentFile> {


    /**
     * 根据设备id删除报告信息
     *
     * @param equipIds 设备ids
     */
    @Modifying
    @Query(value = " delete  from equipment_file where equipment_id in ?1 ", nativeQuery = true)
    void deleteByEquipIn(Set<Long> equipIds);


    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * 根据设备id查询相关报告
     *
     * @param equipId 设备id
     * @return 设备信息列表
     */
    @Query(value = " select * from equipment_file where equipment_id = ?1 ", nativeQuery = true)
    List<EquipmentFile> findByCaliId(Long equipId);
}
