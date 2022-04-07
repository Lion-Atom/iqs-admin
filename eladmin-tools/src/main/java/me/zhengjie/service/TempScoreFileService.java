package me.zhengjie.service;

import me.zhengjie.domain.TempScoreFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/9/28 15:02
 */
public interface TempScoreFileService {

    /**
     * 查询模板打分信息相关附件
     *
     * @param scoreId 模板问题打分清单ID
     * @return /
     */
    List<TempScoreFile> findByScoreId(Long scoreId);

    /**
     * 上传模板打分信息相关附件
     *
     * @param scoreId 模板问题打分清单ID
     * @param file   文件信息
     */
    void uploadFile(Long scoreId, MultipartFile file);

    /**
     * 删除模板认证信息相关附件
     *
     * @param ids 模板认证消息相关附件标识
     */
    void delete(Set<Long> ids);
}
