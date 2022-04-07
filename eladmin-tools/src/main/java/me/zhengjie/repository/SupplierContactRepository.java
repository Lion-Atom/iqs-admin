package me.zhengjie.repository;

import me.zhengjie.domain.Supplier;
import me.zhengjie.domain.SupplierContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/11/23 16:19
 */
public interface SupplierContactRepository extends JpaRepository<SupplierContact, Long>, JpaSpecificationExecutor<SupplierContact> {


    /**
     * 根据名称查询供应商联系人信息
     *
     * @param name 供应商名称
     * @return 供应商信息
     */
    SupplierContact findByName(String name);

    /**
     * 删除供应商联系人信息
     *
     * @param ids 供应商联系人ID
     */
    void deleteAllByIdIn(Set<Long> ids);


    /**
     * 根据供应商ID删除联系人信息
     *
     * @param supplierId 供应商ID
     */
    @Modifying
    @Query(value = " delete  from supplier_contact where supplier_id = ?1 ", nativeQuery = true)
    void deleteBySupplierId(Long supplierId);

    /**
     * 根据供应商ID删除联系人信息
     *
     * @param supplierIds 供应商ID
     */
    @Modifying
    @Query(value = " delete  from supplier_contact where supplier_id in ?1 ", nativeQuery = true)
    void deleteBySupplierIdIn(Set<Long> supplierIds);

    /**
     * 查询供应商下的联系人信息
     *
     * @param supplierId 供应商ID
     * @return 对应的联系人信息
     */
    @Query(value = " select * from supplier_contact where supplier_id = ?1 ", nativeQuery = true)
    List<SupplierContact> findBySupplierId(Long supplierId);

    @Query(value = " select * from supplier_contact where name=?1 and supplier_id = ?2 ", nativeQuery = true)
    SupplierContact findByNameAndSupplierId(String contactName, Long supplierId);
}
