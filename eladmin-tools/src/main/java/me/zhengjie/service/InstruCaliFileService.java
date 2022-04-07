package me.zhengjie.service;

import me.zhengjie.domain.CalibrationFile;
import me.zhengjie.service.dto.InstruCaliFileQueryCriteria;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2022/3/14 13:03
 */
public interface InstruCaliFileService {


    /**
     * @param criteria 查询条件
     * @return 仪器校准报告列表
     */
    List<CalibrationFile> queryAll(InstruCaliFileQueryCriteria criteria);

    /**
     * 上传仪器校准报告
     *
     * @param caliId     仪器校准ID
     * @param isLatest   是否是最新报告
     * @param caliResult 校准结果
     * @param failDesc   不合格原因描述
     * @param file       仪器校准报告
     */
    void uploadFile(Long caliId, Boolean isLatest, String caliResult, String failDesc, MultipartFile file);

    /**
     * 删除报告
     *
     * @param ids 仪器校准报告ids
     */
    void delete(Set<Long> ids);
}
