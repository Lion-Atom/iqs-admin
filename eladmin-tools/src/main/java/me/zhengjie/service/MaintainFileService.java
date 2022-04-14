package me.zhengjie.service;

import me.zhengjie.domain.MaintainFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2022/3/11 14:53
 */
public interface MaintainFileService {

    /**
     * 上传设备保养相关确认单子
     *
     * @param maintainId 设备保养ID
     * @param file     文件信息
     */
    void uploadFile(Long maintainId, MultipartFile file);

    /**
     * 根据设备保养ID查询相关附件
     *
     * @param maintainId 设备保养ID
     * @return /
     */
    List<MaintainFile> getByMaintainId(Long maintainId);

    /**
     * @param ids 附件标识集合
     */
    void delete(Set<Long> ids);
}
