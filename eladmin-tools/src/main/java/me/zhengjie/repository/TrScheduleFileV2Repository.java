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

import me.zhengjie.domain.TrScheduleFile;
import me.zhengjie.domain.TrScheduleFileV2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2022-05-18
 */
@Repository
public interface TrScheduleFileV2Repository extends JpaRepository<TrScheduleFileV2, Long>, JpaSpecificationExecutor<TrScheduleFileV2> {

    /**
     * 根据培训日程安排ids删除附件信息
     *
     * @param trScheduleIds 培训日程安排ID
     */
    @Modifying
    @Query(value = " delete from train_schedule_file where train_schedule_id in ?1 ", nativeQuery = true)
    void deleteByTrScheduleIdIn(Set<Long> trScheduleIds);

    /**
     * 根据培训日程安排id删除附件信息
     *
     * @param trScheduleId 培训日程安排id
     */
    @Modifying
    @Query(value = " delete from train_schedule_file where train_schedule_id = ?1 ", nativeQuery = true)
    void deleteByTrScheduleId(Long trScheduleId);

    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * 根据培训日程安排id查询相关附件
     *
     * @param trScheduleId 培训日程安排id
     * @return 培训日程安排附件信息列表
     */
    @Query(value = " select * from train_schedule_file where train_schedule_id = ?1 order by file_type ", nativeQuery = true)
    List<TrScheduleFileV2> findByTrScheduleId(Long trScheduleId);

    /**
     * @param trScheduleId 培训计划ID
     * @param fileType     文件类型
     * @return /
     */
    @Query(value = " select * from train_schedule_file where train_schedule_id = ?1 and file_type =?2 order by file_type ", nativeQuery = true)
    List<TrScheduleFileV2> findByTrScheduleIdAndType(Long trScheduleId, String fileType);

    /**
     * 条件删除指定出处的培训计划附件信息
     *
     * @param trScheduleId 培训计划ID
     * @param selected     已有项
     * @param fileType     文件类型
     */
    @Modifying
    @Query(value = " delete from train_schedule_file where train_schedule_id = ?1 and file_source = ?2  and file_type = ?3 ", nativeQuery = true)
    void deleteByTrScheduleIdAndFileSourceAndFileType(Long trScheduleId, String selected, String fileType);
}
