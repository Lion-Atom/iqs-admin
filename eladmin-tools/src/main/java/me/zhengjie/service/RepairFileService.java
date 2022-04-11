package me.zhengjie.service;

import me.zhengjie.domain.RepairFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2022/3/11 14:53
 */
public interface RepairFileService {

    /**
     * 上传设备维修相关确认单子
     *
     * @param repairId 设备维修ID
     * @param file     文件信息
     */
    void uploadFile(Long repairId, MultipartFile file);

    /**
     * 根据设备维修ID查询相关附件
     *
     * @param repairId 设备维修ID
     * @return /
     */
    List<RepairFile> getByRepairId(Long repairId);

    /**
     * @param ids 附件标识集合
     */
    void delete(Set<Long> ids);

    /**
     * @param repairId /
     * @param realName /
     */
    void delByRepairIdAndName(Long repairId, String realName);
}
