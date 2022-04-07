package me.zhengjie.service;

import me.zhengjie.domain.SupplierFile;
import me.zhengjie.service.dto.SupplierFileQueryDto;
import me.zhengjie.service.dto.SupplierFileReplaceDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/11/29 10:09
 */
public interface SupplierFileService {
    /**
     * 查询满足条件的供应商相关附件
     *
     * @param dto 查询条件
     * @return /
     */
    List<SupplierFile> findByCond(SupplierFileQueryDto dto);

    /**
     * 上传供应商相关附件
     *
     * @param supplierId 供应商ID
     * @param fileType   文件类型
     * @param file       文件信息
     */
    void uploadFile(Long supplierId, Long contactId, String fileType, MultipartFile file);

    /**
     * 删除模板认证信息相关附件
     *
     * @param ids 模板认证消息相关附件标识
     */
    void delete(Set<Long> ids);

    /**
     * 更新供应商附件信息
     *
     * @param dto 初始化待替换信息
     */
    void updateFile(SupplierFileReplaceDto dto);
}
