package me.zhengjie.service;

import me.zhengjie.domain.TrNewStaffFile;
import me.zhengjie.domain.TrScheduleFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2022/05/18 16:53
 */
public interface TrScheduleFileService {

    /**
     * 上传培训日程安排相关附件
     *
     * @param trScheduleId 培训日程安排ID
     * @param fileType     文件类型
     * @param name         文件名称
     * @param file         文件信息
     */
    void uploadFile(Long trScheduleId, String fileType, String name,MultipartFile file);

    /**
     * 根据培训日程安排ID查询相关附件
     *
     * @param trScheduleId 培训日程安排ID
     * @return /
     */
    List<TrScheduleFile> getByTrScheduleId(Long trScheduleId);

    /**
     * @param ids 附件标识集合
     */
    void delete(Set<Long> ids);

}
