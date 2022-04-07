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

import me.zhengjie.domain.FileLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * @author Zheng Jie
 * @date 2019-03-25
 */
@Repository
public interface FileLevelRepository extends JpaRepository<FileLevel, Long>, JpaSpecificationExecutor<FileLevel> {

    /**
     * 根据 PID 查询
     *
     * @param id pid
     * @return /
     */
    List<FileLevel> findByPid(Long id);

    /**
     * 获取顶级部门
     *
     * @return /
     */
    List<FileLevel> findByPidIsNull();


    /**
     * 判断是否存在子节点
     *
     * @param pid /
     * @return /
     */
    int countByPid(Long pid);

    /**
     * 根据ID更新sub_count
     *
     * @param count /
     * @param id    /
     */
    @Modifying
    @Query(value = " update tool_file_level set sub_count = ?1 where file_level_id = ?2 ", nativeQuery = true)
    void updateSubCntById(Integer count, Long id);


    /**
     * 根据名称查询等级信息
     *
     * @param name 等级名称
     * @return 等级信息
     */
    FileLevel findByName(String name);
}