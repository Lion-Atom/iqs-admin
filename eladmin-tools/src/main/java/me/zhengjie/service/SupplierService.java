package me.zhengjie.service;

import me.zhengjie.domain.Supplier;
import me.zhengjie.service.dto.SupplierDto;
import me.zhengjie.service.dto.SupplierQueryCriteria;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/11/22 11:48
 */
public interface SupplierService {

    /**
     * 根据ID查询
     *
     * @param supplierId /
     * @return /
     */
    SupplierDto findById(Long supplierId);

    /**
     * 创建
     *
     * @param resources /
     */
    Supplier create(Supplier resources);

    /**
     * 编辑
     *
     * @param resources /
     */
    void update(Supplier resources);

    /**
     * 删除
     *
     * @param ids /
     */
    void delete(Set<Long> ids);


    /**
     * 分页查询
     *
     * @param criteria 条件
     * @param pageable 分页参数
     * @return /
     */
    Map<String, Object> queryAll(SupplierQueryCriteria criteria, Pageable pageable);

    /**
     * 查询全部数据
     *
     * @param criteria /
     * @return /
     */
    List<SupplierDto> queryAll(SupplierQueryCriteria criteria);

    /**
     * 导出数据
     *
     * @param queryAll 待导出的数据
     * @param response /
     * @throws IOException /
     */
    void download(List<SupplierDto> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 验证是否被用户关联
     *
     * @param ids /
     */
    void verification(Set<Long> ids);
}
