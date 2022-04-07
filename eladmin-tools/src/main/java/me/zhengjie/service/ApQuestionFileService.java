package me.zhengjie.service;

import me.zhengjie.domain.ApQuestionFile;
import me.zhengjie.domain.AuditPlanFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/9/28 15:02
 */
public interface ApQuestionFileService {

    /**
     * 查询问题或问题下对应的行动相关的附件
     *
     * @param quesId 问题标识
     * @param actId  行动ID
     * @return /
     */
    List<ApQuestionFile> findByQuesIdAndActId(Long quesId, Long actId);

    /**
     * 上传问题或问题下对应的行动相关的附件
     *
     * @param quesId 问题标识
     * @param actId  行动ID
     * @param file   文件信息
     */
    void uploadFile(Long quesId, Long actId, MultipartFile file);

    /**
     * 删除问题或问题下对应的行动相关的附件
     *
     * @param ids 问题或问题下对应的行动相关的附件附件标识
     */
    void delete(Set<Long> ids);
}
