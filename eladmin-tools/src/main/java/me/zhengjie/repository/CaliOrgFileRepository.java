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

import me.zhengjie.domain.CaliOrgFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;


/**
 * @author Tong Minjie
 * @date 2022-03-11
 */
@Repository
public interface CaliOrgFileRepository extends JpaRepository<CaliOrgFile, Long>, JpaSpecificationExecutor<CaliOrgFile> {


    /**
     * 根据仪器校准机构id删除附件信息
     *
     * @param caliOrgIds 仪器校准机构ids
     */
    @Modifying
    @Query(value = " delete  from cali_org_file where cali_org_id in ?1 ", nativeQuery = true)
    void deleteByCaliOrgIdIn(Set<Long> caliOrgIds);


    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * 根据仪器校准机构id查询相关附件
     *
     * @param caliOrgId 仪器校准机构id
     * @return 仪器校准机构附件信息列表
     */
    @Query(value = " select * from cali_org_file where cali_org_id = ?1 ", nativeQuery = true)
    List<CaliOrgFile> findByCaliOrgId(Long caliOrgId);

    /**
     * 删除文件
     *
     * @param caliOrgId 仪器校准机构id
     * @param realName  文件名称
     */
    @Modifying
    @Query(value = " delete from cali_org_file where cali_org_id = ?1 and real_name =?2", nativeQuery = true)
    void deleteByCaliOrgIdAndRealName(Long caliOrgId, String realName);
}
