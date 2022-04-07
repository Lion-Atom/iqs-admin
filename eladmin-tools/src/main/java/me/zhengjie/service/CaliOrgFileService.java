package me.zhengjie.service;

import me.zhengjie.domain.CaliOrgFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2022/3/11 14:53
 */
public interface CaliOrgFileService {

    /**
     * 上传仪校机构相关认证附件
     *
     * @param caliOrgId 仪校机构ID
     * @param file      文件信息
     */
    void uploadFile(Long caliOrgId, MultipartFile file);

    /**
     * 根据仪校机构ID查询相关附件
     *
     * @param caliOrgId 仪校机构ID
     * @return /
     */
    List<CaliOrgFile> findByCaliOrgId(Long caliOrgId);

    /**
     * @param ids 附件标识集合
     */
    void delete(Set<Long> ids);

    /**
     * @param caliOrgId /
     * @param realName  /
     */
    void delByCaliOrgIdAndName(Long caliOrgId, String realName);
}
