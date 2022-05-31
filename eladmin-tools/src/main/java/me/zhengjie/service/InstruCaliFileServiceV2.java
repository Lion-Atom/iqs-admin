package me.zhengjie.service;

import me.zhengjie.domain.InstruCaliFileV2;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * @author TongMin Jie
 * @version V1
 * @date 2022/5/30 17:24
 */
public interface InstruCaliFileServiceV2 {
    /**
     * 上传仪器校准相关报告
     *
     * @param caliId 仪器校准ID
     * @param file     文件信息
     */
    void uploadFile(Long caliId, MultipartFile file);

    /**
     * 根据仪器校准ID查询相关附件
     *
     * @param caliId 仪器校准ID
     * @return /
     */
    List<InstruCaliFileV2> getByCaliId(Long caliId);

    /**
     * @param ids 附件标识集合
     */
    void delete(Set<Long> ids);
}
