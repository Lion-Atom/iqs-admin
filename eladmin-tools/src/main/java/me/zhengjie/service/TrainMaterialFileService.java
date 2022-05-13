package me.zhengjie.service;

import me.zhengjie.domain.TrainMaterialFile;
import me.zhengjie.service.dto.TrainMaterialFileDto;
import me.zhengjie.service.dto.TrainMaterialFileQueryCriteria;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author TongMin Jie
 * @version V1
 * @date 2022/5/12 14:29
 */
public interface TrainMaterialFileService {

    /**
     * 条件查询培训材料
     *
     * @param criteria 查询条件
     * @param pageable 分页器
     * @return /
     */
    Map<String, Object> query(TrainMaterialFileQueryCriteria criteria, Pageable pageable);

    void uploadFile(String name,Long departId, String author, String version, Boolean isInternal, String toolType, String fileDesc, Boolean enabled, MultipartFile file);

    /**
     * 编辑
     *
     * @param resources 文件信息
     */
    void update(TrainMaterialFile resources);

    /**
     * @param ids 附件标识集合
     */
    void delete(Set<Long> ids);

    List<TrainMaterialFileDto> queryAll(TrainMaterialFileQueryCriteria criteria);

    void download(List<TrainMaterialFileDto> queryAll, HttpServletResponse response) throws IOException;


    void updateFile(Long id, String name, Long departId, String author, String version, Boolean isInternal, String toolType, String fileDesc, Boolean enabled, MultipartFile file);
}
