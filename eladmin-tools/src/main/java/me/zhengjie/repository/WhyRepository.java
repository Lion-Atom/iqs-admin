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

import me.zhengjie.domain.Why;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;


/**
 * @author Tong Minjie
 * @date 2020-07-22
 */
@Repository
public interface WhyRepository extends JpaRepository<Why, Long>, JpaSpecificationExecutor<Why> {

    /**
     * 根据原因id查询问题信息
     *
     * @param causeId 问题id
     * @return 问题下时间进程信息
     */
    @Query(value = "SELECT * FROM cause_why where cause_id = ?1 order by why_id asc ", nativeQuery = true)
    List<Why> findByCauseId(Long causeId);


    /**
     * 根据原因id删除5Whys信息
     *
     * @param causeId 原因标识
     */
    @Modifying
    @Query(value = " delete  from cause_why where cause_id = ?1 ", nativeQuery = true)
    void deleteByCauseId(Long causeId);

    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

}
