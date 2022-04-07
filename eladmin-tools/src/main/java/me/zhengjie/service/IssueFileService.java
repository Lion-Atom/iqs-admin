package me.zhengjie.service;

import me.zhengjie.domain.IssueFile;
import me.zhengjie.service.dto.IssueBindFileDto;
import me.zhengjie.service.dto.IssueBindFileQueryDto;
import me.zhengjie.service.dto.IssueFileQueryDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/28 9:31
 */
public interface IssueFileService {

    /**
     * 根据条件查询8D附件
     *
     * @param queryDto 查询条件
     * @return 附件信息
     */
    List<IssueFile> findByCond(IssueFileQueryDto queryDto);

    /**
     * 根据条件查询8D与文控关联文件
     *
     * @param queryDto 查询条件
     * @return 关联附件信息
     */
    List<IssueFile> findByCondV2(IssueBindFileQueryDto queryDto);

    /**
     * 上传附件
     *
     * @param issueId  问题标识
     * @param stepName 所属步骤
     * @param file     文件
     * @return 附件信息
     */
    IssueFile create(Long issueId, String stepName, MultipartFile file);

    /**
     * @param ids 附件标识集合
     */
    void delete(Set<Long> ids);

    /**
     * 根据条件查询8D与文控关联文件
     *
     * @param queryDto 查询条件
     * @return 关联附件信息
     */
    List<IssueFile> getBindFilesByExample(IssueBindFileQueryDto queryDto);

    /**
     * 同步临时文件
     *
     * @param resources 临时文件信息
     */
    void syncTempFiles(List<IssueBindFileDto> resources);
}
