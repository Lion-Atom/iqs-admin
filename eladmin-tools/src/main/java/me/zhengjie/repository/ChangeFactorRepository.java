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

import me.zhengjie.domain.ChangeFactor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2021-09-06
 */
@Repository
public interface ChangeFactorRepository extends JpaRepository<ChangeFactor, Long>, JpaSpecificationExecutor<ChangeFactor> {

    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * 根据变更id查询变更下影响因素信息
     *
     * @param changeId 变更id
     * @return 变更下影响因素信息
     */
    @Query(value = "SELECT * FROM change_factor where change_id = ?1 ", nativeQuery = true)
    List<ChangeFactor> findByChangeId(Long changeId);

    /**
     * 根据变更ids删除影响因素信息
     *
     * @param changeIds 变更ids
     */
    @Modifying
    @Query(value = "delete FROM change_factor where change_id in ?1 ", nativeQuery = true)
    void deleteByChangeIdIn(Set<Long> changeIds);

    @Query(value = "SELECT * FROM change_factor where change_id = ?1 and name = ?2 ", nativeQuery = true)
    ChangeFactor findByNameAndChangeId(Long changeId, String name);
}