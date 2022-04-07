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

import me.zhengjie.domain.FileCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author TongMinjie
 * @date 2021-04-28
 */
@Repository
public interface FileCategoryRepository extends JpaRepository<FileCategory, Long>, JpaSpecificationExecutor<FileCategory> {

    /**
     * 根据 PID 查询
     *
     * @param id pid
     * @return /
     */
    List<FileCategory> findByPid(Long id);

    /**
     * 获取顶级分类
     *
     * @return /
     */
    List<FileCategory> findByPidIsNull();


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
    @Query(value = " update tool_file_category set sub_count = ?1 where file_category_id = ?2 ", nativeQuery = true)
    void updateSubCntById(Integer count, Long id);

    /**
     * @return 文件分类总数
     */
    @Query(value = "select count(file_category_id) from tool_file_category", nativeQuery = true)
    Integer getFileCategoryCount();

    /**
     * @return 当天文件分类新增数目
     */
    @Query(value = "select count(file_category_id) from tool_file_category  where date_format(create_time,'%Y-%m-%d') = ?1", nativeQuery = true)
    Integer getCountByDateTime(String time);


    /**
     * 根据名称查询文件分类信息
     *
     * @param name 文件分类名称
     * @return 文件分类信息
     */
    FileCategory findByName(String name);
}