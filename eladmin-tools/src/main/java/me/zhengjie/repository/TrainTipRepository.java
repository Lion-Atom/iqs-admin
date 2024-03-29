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

import me.zhengjie.domain.CalibrationOrg;
import me.zhengjie.domain.TrainTip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2022-03-11
 */
@Repository
public interface TrainTipRepository extends JpaRepository<TrainTip, Long>, JpaSpecificationExecutor<TrainTip> {

    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * @param bindingIds 绑定IDS
     * @param trainType 培训类型
     */
    @Modifying
    @Query(value = "delete from train_tip where binding_id in ?1 and train_type = ?2", nativeQuery = true)
    void deleteAllByBindingIdInAndTrainType(Set<Long> bindingIds, String trainType);
}