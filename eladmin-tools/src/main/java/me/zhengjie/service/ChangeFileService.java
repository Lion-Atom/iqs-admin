package me.zhengjie.service;

import me.zhengjie.domain.ChangeFile;
import me.zhengjie.service.dto.ChangeFileQueryDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2022/01/19
 */
public interface ChangeFileService {

    /**
     * 查询满足条件的供应商相关附件
     *
     * @param dto 查询条件
     * @return /
     */
    List<ChangeFile> findByCond(ChangeFileQueryDto dto);

    /**
     * 上传供应商相关附件
     *
     * @param changeId 变更ID
     * @param factorId 变更因素ID
     * @param fileType 文件类型
     * @param file     文件信息
     */
    void uploadFile(Long changeId, Long factorId, String fileType, MultipartFile file);

    /**
     * 删除模板认证信息相关附件
     *
     * @param ids 模板认证消息相关附件标识
     */
    void delete(Set<Long> ids);

}
