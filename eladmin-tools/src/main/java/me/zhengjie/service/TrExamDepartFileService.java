package me.zhengjie.service;

import me.zhengjie.domain.TrExamDepartFile;
import me.zhengjie.service.dto.TrExamDepartFileDto;
import me.zhengjie.service.dto.TrExamDepartFileQueryCriteria;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2022/05/10 10:17
 */
public interface TrExamDepartFileService {

    /**
     * 上传培训关联部门题库
     *
     * @param departId 培训关联部门ID
     * @param name     试卷名称
     * @param enabled  是否有效
     * @param fileDesc 试卷描述
     * @param file     文件信息
     */
    void uploadFile(Long departId, String name, Boolean enabled, String fileDesc, MultipartFile file);

    /**
     * 根据培训关联部门ID查询题库
     *
     * @param criteria 查询条件
     * @param pageable 分页器
     * @return /
     */
    Map<String, Object> query(TrExamDepartFileQueryCriteria criteria, Pageable pageable);

    /**
     * @param ids 附件标识集合
     */
    void delete(Set<Long> ids);

    /**
     * 编辑
     *
     * @param resources 文件信息
     */
    void update(TrExamDepartFile resources);

    /**
     * 查询全部数据
     *
     * @param criteria /
     * @return /
     */
    List<TrExamDepartFileDto> queryAll(TrExamDepartFileQueryCriteria criteria);

    void download(List<TrExamDepartFileDto> queryAll, HttpServletResponse response) throws IOException;
}
