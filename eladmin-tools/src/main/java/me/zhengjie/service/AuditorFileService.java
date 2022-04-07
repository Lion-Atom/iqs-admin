package me.zhengjie.service;

import me.zhengjie.domain.AuditorFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/9/14 16:01
 */
public interface AuditorFileService {

    /**
     * 上传审核人员相关认证附件
     *
     * @param auditorId 审核人员标识
     * @param file   文件信息
     */
    void uploadFile(Long auditorId, MultipartFile file);

    /**
     * 根据问题ID查询
     *
     * @param auditorId 审核人员标识
     * @return /
     */
    List<AuditorFile> findByAuditorId(Long auditorId);

    /**
     * @param ids 附件标识集合
     */
    void delete(Set<Long> ids);
}
