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

import me.zhengjie.modules.system.domain.Dept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

/**
 * @author Zheng Jie
 * @date 2019-03-25
 */
public interface DeptRepository extends JpaRepository<Dept, Long>, JpaSpecificationExecutor<Dept> {

    /**
     * 根据 PID 查询
     *
     * @param id pid
     * @return /
     */
    List<Dept> findByPid(Long id);

    /**
     * 获取顶级部门
     *
     * @return /
     */
    List<Dept> findByPidIsNull();

    /**
     * 根据角色ID 查询
     *
     * @param roleId 角色ID
     * @return /
     */
    @Query(value = "select d.* from sys_dept d, sys_roles_depts r where " +
            "d.dept_id = r.dept_id and r.role_id = ?1", nativeQuery = true)
    Set<Dept> findByRoleId(Long roleId);

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
    @Query(value = " update sys_dept set sub_count = ?1 where dept_id = ?2 ", nativeQuery = true)
    void updateSubCntById(Integer count, Long id);

    /**
     * @return 部门总数
     */
    @Query(value = "select count(dept_id) from sys_dept", nativeQuery = true)
    Integer getDeptCount();

    /**
     * @return 当天部门新增数目
     */
    @Query(value = "select count(dept_id) from sys_dept  where date_format(create_time,'%Y-%m-%d') = ?1", nativeQuery = true)
    Integer getCountByDateTime(String time);

    /**
     * @param name 部门名称
     * @return 对应的顶级部门
     */
    @Query(value = "select * from sys_dept where  pid is null and name = ?1", nativeQuery = true)
    List<Dept> findByNameAndPidIsNull(String name);

    /**
     * @param name 部门名称
     * @param id   原部门标识
     * @return 同名顶级部门
     */
    @Query(value = "select * from sys_dept where  pid is null and name = ?1 and dept_id != ?2", nativeQuery = true)
    List<Dept> findByNameAndPidIsNullAndIsNotSelf(String name, Long id);

    /**
     * @param name  部门名称
     * @param rList 其他部门标识集合
     * @return 其他部门
     */
    @Query(value = "select name from sys_dept where  name = ?1 and dept_id in ?2", nativeQuery = true)
    List<String> findAllByIdAndName(String name, Set<Long> rList);


    /**
     * @param rList 部门标识集合
     * @return 其他部门
     */
    @Query(value = "select * from sys_dept where  dept_id in ?1", nativeQuery = true)
    List<Dept> findAllByIdIn(Set<Long> rList);
}