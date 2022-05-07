package me.zhengjie.service;

import me.zhengjie.domain.TrNewStaffFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2022/05/07 09:53
 */
public interface TrNewStaffFileService {

    /**
     * 上传新员工培训相关确认单子
     *
     * @param trNewStaffId 新员工培训ID
     * @param file     文件信息
     */
    void uploadFile(Long trNewStaffId, MultipartFile file);

    /**
     * 根据新员工培训ID查询相关附件
     *
     * @param trNewStaffId 新员工培训ID
     * @return /
     */
    List<TrNewStaffFile> getByTrNewStaffId(Long trNewStaffId);

    /**
     * @param ids 附件标识集合
     */
    void delete(Set<Long> ids);
    
}
