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
package me.zhengjie.modules.system.repository;

import me.zhengjie.modules.system.domain.DictDetail;
import me.zhengjie.modules.system.domain.ToolsTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
* @author Tong Minjie
* @date 2021-06-23
*/
public interface ToolsTaskRepository extends JpaRepository<ToolsTask, Long>, JpaSpecificationExecutor<ToolsTask> {

    /**
     * @return 返回所有的有效任务数目
     */
    @Query(value = "SELECT count(pre_trail_id) FROM tool_pre_trail  WHERE is_del = 0", nativeQuery = true)
    Integer findAllCount();

    /**
     * @return 返回所有的有效未完成的任务数目
     */
    @Query(value = "SELECT count(pre_trail_id) FROM tool_pre_trail  WHERE is_done=?1 and is_del = 0", nativeQuery = true)
    Integer findAllNotDoneCount(Boolean isDone);

    @Query(value = "SELECT count(pre_trail_id) FROM tool_pre_trail  WHERE approved_by = ?1 and is_del = 0", nativeQuery = true)
    Integer findCountByUserId(Long currentUserId);

    @Query(value = "SELECT count(pre_trail_id) FROM tool_pre_trail  WHERE approved_by = ?1 and is_done=?2 and is_del = 0", nativeQuery = true)
    Integer findAllNotDoneCountByUserId(Long currentUserId,Boolean isDone);

    /**
     * @return 个人当天任务新增数目
     */
    @Query(value = "select count(pre_trail_id) from tool_pre_trail  where date_format(create_time,'%Y-%m-%d') = ?1 and approved_by = ?2 and is_del=0", nativeQuery = true)
    Integer getPersonalTaskCountByDateTime(String time,Long currentUserId);

    /**
     * @return 所有任务新增数目
     */
    @Query(value = "select count(pre_trail_id) from tool_pre_trail  where date_format(create_time,'%Y-%m-%d') = ?1 and is_del=0", nativeQuery = true)
    Integer getTaskCountByDateTime(String time);

    @Query(value = "select  * from tool_pre_trail  where pre_trail_no = ?1 " +
            " and is_del= ?2 " +
            " order by pre_trail_id asc limit 1", nativeQuery = true)
    ToolsTask findFirstByPreNo(String preTrailNo, Long notDel);
}