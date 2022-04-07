package me.zhengjie.service;

import me.zhengjie.domain.ApQuestionFile;
import me.zhengjie.domain.TempCerFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/9/28 15:02
 */
public interface TempCerFileService {

    /**
     * 查询模板认证信息相关附件
     *
     * @param cerId 认证信息ID
     * @return /
     */
    List<TempCerFile> findByCerId(Long cerId);

    /**
     * 上传模板认证信息相关附件
     *
     * @param cerId 认证信息ID
     * @param file   文件信息
     */
    void uploadFile(Long cerId, MultipartFile file);

    /**
     * 删除模板认证信息相关附件
     *
     * @param ids 模板认证消息相关附件标识
     */
    void delete(Set<Long> ids);
}
