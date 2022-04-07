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

import me.zhengjie.domain.LocalStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Zheng Jie
 * @date 2019-09-05
 */
@Repository
public interface LocalStorageRepository extends JpaRepository<LocalStorage, Long>, JpaSpecificationExecutor<LocalStorage> {


    /**
     * @param ids 文件标识集合
     * @return 文件信息
     */
    @Query(value = "select * from tool_local_storage where storage_id in ?1 and is_del=0", nativeQuery = true)
    List<LocalStorage> findAllByIds(Long[] ids);

    /**
     * @param deptId         所属部门标识
     * @param fileCategoryId 所属分类标识
     * @return 文件的名称
     */
    @Query(value = "select * from tool_local_storage where " +
            " dept_id = ?1 " +
            " and file_category_id = ?2 " +
            " and real_name like %?3% " +
            " and suffix = ?4 " +
            " and is_del=0 ", nativeQuery = true)
    List<LocalStorage> findRealNameByDeptIdAndCategoryIdAndRealName(Long deptId, Long fileCategoryId, String keyword, String initType);

    /**
     * 根据部门查询文件
     *
     * @param deptIds /
     * @return /
     */
    @Query(value = "SELECT count(1) FROM tool_local_storage f WHERE f.dept_id IN ?1 and is_del=0", nativeQuery = true)
    int countByDepts(Set<Long> deptIds);

    /**
     * 根据文件所在等级查询
     *
     * @param levelIds 文件等级标识集合
     * @return 文件等级对应的文件数目
     */
    @Query(value = "SELECT count(1) FROM tool_local_storage f WHERE f.file_level_id IN ?1 and is_del=0", nativeQuery = true)
    int countByLevelIds(Set<Long> levelIds);

    /**
     * 根据文件所在分类查询
     *
     * @param cateIds 文件分类标识集合
     * @return 文件分类对应的文件数目
     */
    @Query(value = "SELECT count(1) FROM tool_local_storage f WHERE f.file_category_id IN ?1 and is_del=0", nativeQuery = true)
    int countByCateIds(Set<Long> cateIds);

    /**
     * @return 文件总数
     */
    @Query(value = "SELECT count(storage_id) FROM tool_local_storage where is_del=0", nativeQuery = true)
    Integer getFileCount();


    /**
     * 根据文件用途查询对应数目
     *
     * @param fileType 文件用途类型
     * @return 文件数目
     */
    @Query(value = "SELECT count(storage_id) FROM tool_local_storage where file_type = ?1 and is_del=0", nativeQuery = true)
    Integer getCountByFileType(String fileType);

    /**
     * 根据文件级别查询对应数目
     *
     * @param fileLevelId 文件级别标识
     * @return 文件数目
     */
    @Query(value = "SELECT count(storage_id) FROM tool_local_storage where file_level_id = ?1 and is_del=0", nativeQuery = true)
    Integer getCountByFileLevelId(Long fileLevelId);

    /**
     * 查询部门下文件数目
     *
     * @param deptIds 部门标识集合
     * @return 部门对应的文件数目
     */
    @Query(value = "SELECT count(storage_id) FROM tool_local_storage where dept_id in ?1 and is_del=0", nativeQuery = true)
    Integer getCountByDeptIdIn(Set<Long> deptIds);

    /**
     * 根据文件名称查询文件信息
     *
     * @param name 文件名称
     * @return 文件信息
     */
    @Query(value = "SELECT * FROM tool_local_storage where name = ?1 and is_del=0", nativeQuery = true)
    LocalStorage findByName(String name);

    /**
     * 根据文件名称查询同部门、同分类文件信息
     *
     * @param name   文件名称
     * @param deptId 部门标识
     * @param cateId 文件分类标识
     * @return 文件信息
     */
    @Query(value = "SELECT * FROM tool_local_storage where name = ?1 " +
            " and dept_id = ?2 " +
            " and file_category_id = ?3 " +
            " and is_del=0", nativeQuery = true)
    LocalStorage findByNameAndDeptIdAndCategoryId(String name, Long deptId, Long cateId);

    /**
     * 删除待审批项
     *
     * @param id 标识
     */
    @Modifying
    @Query(value = " update tool_local_storage set is_del = 1 where storage_id = ?1 ", nativeQuery = true)
    void delById(Long id);

    /**
     * 根据文件名称查询文件信息
     *
     * @param id 文件标识
     * @return 文件信息
     */
    @Query(value = "SELECT * FROM tool_local_storage where storage_id = ?1 and is_del=0", nativeQuery = true)
    Optional<LocalStorage> findById(Long id);

    /**
     * 部门下不同等级的文件数目
     *
     * @param levelId    等级标识
     * @param deptIdList 部门标识集合
     * @return 文件数目
     */
    @Query(value = "SELECT count(storage_id) FROM tool_local_storage where file_level_id = ?1 and dept_id in ?2 and is_del=0", nativeQuery = true)
    Integer getCountByFileLevelIdAndDeptIdIn(Long levelId, List<Long> deptIdList);

    /**
     * 根据文件用途查询指定部门对应数目
     *
     * @param fileType   文件用途类型
     * @param deptIdList 部门标识集合
     * @return 文件数目
     */
    @Query(value = "SELECT count(storage_id) FROM tool_local_storage where file_type = ?1 and dept_id in ?2 and is_del=0", nativeQuery = true)
    Integer getCountByFileTypeAndDeptIdIn(String fileType, List<Long> deptIdList);

    /**
     * 根据时间点查询指定部门对应数目
     *
     * @param time       时间点
     * @param deptIdList 部门标识集合
     * @return 文件数目
     */
    @Query(value = "SELECT count(storage_id) FROM tool_local_storage where date_format(create_time,'%Y-%m-%d') = ?1 and dept_id in ?2 and is_del=0", nativeQuery = true)
    Integer getDepartCountByDateTime(String time, List<Long> deptIdList);

    @Query(value = "SELECT * FROM tool_local_storage where file_status = ?1 and is_del= ?2", nativeQuery = true)
    List<LocalStorage> findAllByTempStatus(String tempStatus, Long notDel);
}