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

import me.zhengjie.domain.SupplierFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;


/**
 * @author Tong Minjie
 * @date 2020-09-14
 */
@Repository
public interface SupplierFileRepository extends JpaRepository<SupplierFile, Long>, JpaSpecificationExecutor<SupplierFile> {

    /**
     * 根据供应商ID删除附件信息
     *
     * @param supplierIds 供应商IDs
     */
    @Modifying
    @Query(value = " delete  from supplier_file where supplier_id in ?1 ", nativeQuery = true)
    void deleteBySupplierIdIn(Set<Long> supplierIds);

    /**
     * 查询相关供应商附件
     *
     * @param supplierId 供应商ID
     * @param fileType   文件所属
     * @return 相关供应商附件
     */
    @Query(value = " select * from supplier_file where supplier_id = ?1 and file_type = ?2 and supplier_contact_id is null", nativeQuery = true)
    List<SupplierFile> findBySupplierIdAndFileType(Long supplierId, String fileType);

    /**
     * 查询相关供应商附件
     *
     * @param supplierId 供应商ID
     * @param fileType 文件类型
     * @return 相关供应商附件
     */
    @Query(value = " select * from supplier_file where supplier_id = ?1 " +
            " and file_type =  ?2 " +
            " and supplier_contact_id is null ", nativeQuery = true)
    List<SupplierFile> findBySupplierIdAndOtherIdIsNull(Long supplierId,String fileType);

    /**
     * 查询相关供应商客户审核附件
     *
     * @param templateId 供应商ID
     * @param contactId  供应商联系人ID
     * @return 供应商客户审核附件信息
     */
    @Query(value = " select * from supplier_file where supplier_id = ?1 and supplier_contact_id = ?2 and file_type = ?3", nativeQuery = true)
    List<SupplierFile> findBySupplierIdAndSupplierContactIdAndFileType(Long templateId, Long contactId,String fileType);

    /**
     * 根据id删除认证信息
     *
     * @param ids 问题标识集合
     */
    @Modifying
    @Query(value = " delete  from supplier_file where supplier_file_id in ?1 ", nativeQuery = true)
    void deleteByIdIn(Set<Long> ids);

    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    @Query(value = " select * from supplier_file where supplier_id = ?1 " +
            " and file_type in ?2 " +
            " and supplier_contact_id is not null  ", nativeQuery = true)
    List<SupplierFile> findBySupplierIdAndSupplierContactIdIsNotNullAndFileType(Long uId, String[] fileTypes);

    @Query(value = " select * from supplier_file where supplier_id = ?1 and file_type in ?2 and supplier_contact_id is null", nativeQuery = true)
    List<SupplierFile> findBySupplierIdAndFileTypeIn(Long uId, String[] fileTypes);

    @Query(value = " select * from supplier_file where supplier_id = ?1 and supplier_contact_id = ?2 and file_type in ?3", nativeQuery = true)
    List<SupplierFile> findBySupplierIdAndSupplierContactIdAndFileTypeIn(Long templateId, Long contactId,String[] fileTypeS);
}
