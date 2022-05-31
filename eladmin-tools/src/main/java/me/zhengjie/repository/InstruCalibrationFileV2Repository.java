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

import me.zhengjie.domain.CalibrationFile;
import me.zhengjie.domain.InstruCaliFileV2;
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
public interface InstruCalibrationFileV2Repository extends JpaRepository<InstruCaliFileV2, Long>, JpaSpecificationExecutor<InstruCaliFileV2> {


    /**
     * 根据仪器校准id删除报告信息
     *
     * @param caliIds 仪器校准ids
     */
    @Modifying
    @Query(value = " delete  from instru_calibration_file where instru_calibration_id in ?1 ", nativeQuery = true)
    void deleteByCaliIdIn(Set<Long> caliIds);


    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * 根据仪器校准id查询相关报告
     *
     * @param caliId 仪器校准id
     * @return 仪器校准报告信息列表
     */
    @Query(value = " select * from instru_calibration_file where instru_calibration_id = ?1 ", nativeQuery = true)
    List<InstruCaliFileV2> findByCaliId(Long caliId);
}
