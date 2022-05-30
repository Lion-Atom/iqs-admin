package me.zhengjie.service;

import me.zhengjie.domain.InstrumentFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2022/05/27 16:23
 */
public interface InstruFileService {

    /**
     * 上传仪器报废单
     *
     * @param instruId 仪器id
     * @param name     名称
     * @param file     文件信息
     */
    void uploadFile(Long instruId, String name, MultipartFile file);

    /**
     * 根据仪器仪表ID查询相关附件
     *
     * @param instruId 仪器仪表ID
     * @return /
     */
    List<InstrumentFile> getByInstruId(Long instruId);

    /**
     * @param ids 附件标识集合
     */
    void delete(Set<Long> ids);

    /**
     * 上传仪器报废单
     *
     * @param instruId 仪器id
     * @param file     文件信息
     */
    void uploadFileV2(Long instruId, MultipartFile file);
}
