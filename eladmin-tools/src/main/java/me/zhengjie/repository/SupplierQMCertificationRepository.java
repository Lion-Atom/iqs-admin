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

import me.zhengjie.domain.SupplierQMCertification;
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
public interface SupplierQMCertificationRepository extends JpaRepository<SupplierQMCertification, Long>, JpaSpecificationExecutor<SupplierQMCertification> {

    /**
     * 根据供应商ID删除质量管理认证信息
     *
     * @param supplierIds 供应商IDs
     */
    @Modifying
    @Query(value = " delete  from supplier_qm_certification where supplier_id in ?1 ", nativeQuery = true)
    void deleteBySupplierIdIn(Set<Long> supplierIds);

    /**
     * 查询相关供应商质量管理认证信息
     *
     * @param supplierId 供应商ID
     * @return 相关供应商质量管理认证信息
     */
    @Query(value = " select * from supplier_qm_certification where supplier_id = ?1", nativeQuery = true)
    List<SupplierQMCertification> findBySupplierId(Long supplierId);

    /**
     * 根据id删除认证信息
     *
     * @param ids 认证标识集合
     */
    @Modifying
    @Query(value = " delete  from supplier_qm_certification where certification_id in ?1 ", nativeQuery = true)
    void deleteByIdIn(Set<Long> ids);

    @Query(value = " select * from supplier_qm_certification where supplier_id = ?1 and item_name = ?2", nativeQuery = true)
    SupplierQMCertification findByItemNameAndSupplierId(Long supplierId, String itemName);
}
