package me.zhengjie.service;

import me.zhengjie.domain.CsFeedbackFile;
import me.zhengjie.domain.TrNewStaffFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2022/05/07 09:53
 */
public interface CsFeedbackFileService {

    /**
     * 上传客户反馈相关确认单子
     *
     * @param csFeedbackId 客户反馈ID
     * @param file     文件信息
     */
    void uploadFile(Long csFeedbackId, MultipartFile file);

    /**
     * 根据客户反馈ID查询相关附件
     *
     * @param csFeedbackId 客户反馈ID
     * @return /
     */
    List<CsFeedbackFile> getByCsFeedbackId(Long csFeedbackId);

    /**
     * @param ids 附件标识集合
     */
    void delete(Set<Long> ids);
    
}
