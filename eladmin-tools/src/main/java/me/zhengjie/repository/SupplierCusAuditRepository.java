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

import me.zhengjie.domain.SupplierCusAudit;
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
public interface SupplierCusAuditRepository extends JpaRepository<SupplierCusAudit, Long>, JpaSpecificationExecutor<SupplierCusAudit> {

    /**
     * 根据供应商ID删除附件信息
     *
     * @param supplierIds 供应商IDs
     */
    @Modifying
    @Query(value = " delete  from supplier_customer_audit where supplier_id in ?1 ", nativeQuery = true)
    void deleteBySupplierIdIn(Set<Long> supplierIds);

    /**
     * 查询相关供应商客户审核信息
     *
     * @param supplierId 供应商ID
     * @return 相关供应商附件
     */
    @Query(value = " select * from supplier_customer_audit where supplier_id = ?1 ", nativeQuery = true)
    List<SupplierCusAudit> findBySupplierId(Long supplierId);

    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);


    /**
     * 查询同名供应商客户审核信息
     *
     * @param customerName 被审核的客户名称
     * @param supplierId   供应商ID
     * @return 客户审核信息
     */
    @Query(value = " select * from supplier_customer_audit where customer_name=?1 and supplier_id = ?2 ", nativeQuery = true)
    SupplierCusAudit findByCustomerNameAndSupplierId(String customerName, Long supplierId);
}
