package me.zhengjie.service;

import me.zhengjie.domain.TrCertificationFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2022/05/07 13:33
 */
public interface TrCertificationFileService {

    /**
     * 上传认证证书相关确认单子
     *
     * @param trCertificationId 认证证书ID
     * @param file     文件信息
     */
    void uploadFile(Long trCertificationId, MultipartFile file);

    /**
     * 根据认证证书ID查询相关附件
     *
     * @param trCertificationId 认证证书ID
     * @return /
     */
    List<TrCertificationFile> getByTrCertificationId(Long trCertificationId);

    /**
     * @param ids 附件标识集合
     */
    void delete(Set<Long> ids);
    
}
