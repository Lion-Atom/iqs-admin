package me.zhengjie.service;

import me.zhengjie.domain.SupplierQMCertification;
import me.zhengjie.service.dto.SupplierQMCerReplaceDto;
import me.zhengjie.service.dto.SupplierQMCertificationDto;

import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/11/30 10:30
 */
public interface SupplierQMCertificationService {


    /**
     * 查询质量管理认证信息
     *
     * @param supplierId 供应商ID
     * @return 供应商质量管理认证信息
     */
    List<SupplierQMCertificationDto> findBySupplierId(Long supplierId);

    /**
     * 新增
     *
     * @param resources 质量管理认证信息
     */
    SupplierQMCertification create(SupplierQMCertification resources);

    /**
     * 更新
     *
     * @param resources 质量管理认证信息
     */
    void update(SupplierQMCertification resources);

    /**
     * 删除
     *
     * @param ids /
     */
    void delete(Set<Long> ids);

    /**
     * 初始化认证信息
     *
     * @param uId 认证标识
     * @return 认证初始信息
     */
    List<SupplierQMCertification> initCertification(Long uId);


    void replaceCer(SupplierQMCerReplaceDto dto);
}
