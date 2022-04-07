package me.zhengjie.service;

import me.zhengjie.domain.GridFile;
import me.zhengjie.service.dto.InstruGridFileQueryCriteria;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2022/3/14 13:03
 */
public interface GridFileService {


    /**
     * @param criteria 查询条件
     * @return 仪器校准报告列表
     */
    List<GridFile> queryByExample(InstruGridFileQueryCriteria criteria);

    /**
     * 上传仪器校准报告
     *
     * @param fileType  文件所属类型
     * @param file     仪器校准报告
     */
    void uploadGridFile(String fileType, MultipartFile file);

    /**
     * 删除报告
     *
     * @param ids 仪器校准报告ids
     */
    void deleteGridFiles(Set<Long> ids);
}
