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

import me.zhengjie.domain.InstruCali;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2022-03-14
 */
@Repository
public interface InstruCaliRepository extends JpaRepository<InstruCali, Long>, JpaSpecificationExecutor<InstruCali> {

    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    @Query(value = "SELECT * FROM tools_calibration where instru_name = ?1", nativeQuery = true)
    InstruCali findByInstruName(String instruName);

    /**
     * @param innerId 内部ID
     * @return 仪器校准信息
     */
    @Query(value = "SELECT * FROM tools_calibration where inner_id = ?1", nativeQuery = true)
    InstruCali findByInnerID(String innerId);

    /**
     * @param assetNum 资产号
     * @return 仪器校准信息
     */
    @Query(value = "SELECT * FROM tools_calibration where asset_num = ?1", nativeQuery = true)
    InstruCali findByAssetNum(String assetNum);

    /**
     * 查询需要提醒的仪器校准信息列表
     *
     * @return 仪器校准信息
     */
    @Query(value = "SELECT * FROM tools_calibration where is_remind = true ", nativeQuery = true)
    List<InstruCali> findByIsRemind();

    /**
     * @return 有效仪器校准信息列表
     */
    @Query(value = "SELECT * FROM tools_calibration where is_droped = false ", nativeQuery = true)
    List<InstruCali> findByIsNotDroped();
}