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

import me.zhengjie.domain.SupplierAnnualAssessment;
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
public interface SupplierAnuAssessRepository extends JpaRepository<SupplierAnnualAssessment, Long>, JpaSpecificationExecutor<SupplierAnnualAssessment> {

    /**
     * 根据供应商ID删除年度评估信息
     *
     * @param supplierIds 供应商IDs
     */
    @Modifying
    @Query(value = " delete  from supplier_annual_assessment where supplier_id in ?1 ", nativeQuery = true)
    void deleteBySupplierIdIn(Set<Long> supplierIds);

    /**
     * 查询相关供应商年度评估信息
     *
     * @param supplierId 供应商ID
     * @return 相关供应商附件
     */
    @Query(value = " select * from supplier_annual_assessment where supplier_id = ?1 order by assess_date desc ", nativeQuery = true)
    List<SupplierAnnualAssessment> findBySupplierId(Long supplierId);

    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);


    /**
     * 查询同名供应商年度评估信息
     *
     * @param assessName 年度评估名称
     * @param supplierId 供应商ID
     * @return 供应商年度评估信息
     */
    @Query(value = " select * from supplier_annual_assessment where assess_name =?1 and supplier_id = ?2 ", nativeQuery = true)
    SupplierAnnualAssessment findByAssessNameAndSupplierId(String assessName, Long supplierId);
}
