package me.zhengjie.service;

import me.zhengjie.domain.EquipmentFile;
import me.zhengjie.domain.MaintainFile;
import me.zhengjie.service.dto.EquipFileQueryByExample;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2022/7/13 14:53
 */
public interface EquipmentFileService {

    /**
     * 上传设备保养相关确认单子
     *
     * @param equipId  设备ID
     * @param fileType 附件所属类型
     * @param file     文件信息
     */
    void uploadFile(Long equipId, String fileType, MultipartFile file);

    /**
     * 根据设备保养ID查询相关附件
     *
     * @param queryByExample 条件查询
     * @return /
     */
    List<EquipmentFile> queryByExample(EquipFileQueryByExample queryByExample);

    /**
     * @param ids 附件标识集合
     */
    void delete(Set<Long> ids);

}
