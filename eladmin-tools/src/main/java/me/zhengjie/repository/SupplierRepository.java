package me.zhengjie.repository;

import me.zhengjie.domain.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/11/22 11:36
 */
public interface SupplierRepository extends JpaRepository<Supplier, Long>, JpaSpecificationExecutor<Supplier> {


    /**
     * 根据名称查询供应商信息
     *
     * @param name 供应商名称
     * @return 供应商信息
     */
    Supplier findByName(String name);

    @Query(value = "select count(*) from tool_supplier where to_days(create_time) = to_days(now()) ", nativeQuery = true)
    Integer findTodayCountByCreateTime();


    /**
     * 删除供应商信息
     *
     * @param ids 供应商ID
     */
    void deleteAllByIdIn(Set<Long> ids);
}
