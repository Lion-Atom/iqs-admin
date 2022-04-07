package me.zhengjie.service;

import me.zhengjie.domain.SupplierContact;
import me.zhengjie.service.dto.SupplierContactDto;
import me.zhengjie.service.dto.SupplierContactQueryCriteria;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

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
public interface SupplierContactService {

    /**
     * 根据供应商ID查询
     *
     * @param supplierId /
     * @return /
     */
    List<SupplierContact> findBySupplierId(Long supplierId);

    /**
     * 根据ID查询
     *
     * @param contactId /
     * @return /
     */
    SupplierContactDto findById(Long contactId);

    /**
     * 创建
     *
     * @param resources /
     */
    SupplierContact create(SupplierContact resources);

    /**
     * 编辑
     *
     * @param resources /
     */
    void update(SupplierContact resources);

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
    Map<String, Object> queryAll(SupplierContactQueryCriteria criteria, Pageable pageable);

    /**
     * 查询全部数据
     *
     * @param criteria /
     * @return /
     */
    List<SupplierContact> queryAll(SupplierContactQueryCriteria criteria);

    /**
     * 导出数据
     *
     * @param queryAll 待导出的数据
     * @param response /
     * @throws IOException /
     */
    void download(List<SupplierContact> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 验证是否被用户关联
     *
     * @param ids /
     */
    void verification(Set<Long> ids);

    /**
     * 修改联系人照片
     *
     * @param contactId 联系人标识
     * @param file 文件
     * @return /
     */
    @Deprecated
    Map<String, String> updateAvatar(Long contactId, MultipartFile file);
}
