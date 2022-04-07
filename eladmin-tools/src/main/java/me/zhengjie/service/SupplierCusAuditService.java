package me.zhengjie.service;

import me.zhengjie.domain.SupplierCusAudit;
import me.zhengjie.service.dto.SupplierCusAuditReplaceDto;

import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/12/1 14:02
 */
public interface SupplierCusAuditService {

    /**
     * 查询供应商下客户审核信息
     *
     * @param supplierId 供应商ID
     * @return 客户审核信息
     */
    List<SupplierCusAudit> findBySupplierId(Long supplierId);


    /**
     * 新建客户审核信息
     *
     * @param resources /
     * @return 新客户审核信息
     */
    SupplierCusAudit create(SupplierCusAudit resources);


    /**
     * 更新客户审核信息
     *
     * @param resources /
     */
    void update(SupplierCusAudit resources);


    /**
     * @param dto UID和目标SupplierId
     */
    void replaceBindSupplierId(SupplierCusAuditReplaceDto dto);


    /**
     * 删除客户审核信息
     *
     * @param ids 审核ids
     */
    void delete(Set<Long> ids);
}
