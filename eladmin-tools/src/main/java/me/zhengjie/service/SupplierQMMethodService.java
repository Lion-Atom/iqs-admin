package me.zhengjie.service;

import me.zhengjie.domain.SupplierQMMethod;
import me.zhengjie.service.dto.SupplierQMMethodReplaceDto;

import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/12/2 11:09
 */
public interface SupplierQMMethodService {

    /**
     * 查询供应商对应的质量管理方法
     *
     * @param supplierId 供应商ID
     * @return 供应商质量管理方法
     */
    List<SupplierQMMethod> findBySupplierId(Long supplierId);

    /**
     * 新建供应商质量管理方法信息
     *
     * @param resources /
     * @return 供应商质量管理方法信息
     */
    SupplierQMMethod create(SupplierQMMethod resources);

    List<SupplierQMMethod> initMethod(Long uId);

    void update(SupplierQMMethod resources);

    void replaceMethod(SupplierQMMethodReplaceDto dto);

    void delete(Set<Long> ids);
}
