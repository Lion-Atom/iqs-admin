package me.zhengjie.service;

import me.zhengjie.domain.InstruCalibration;
import me.zhengjie.service.dto.InstruCalibrationDto;
import me.zhengjie.service.dto.InstruCalibrationQueryCriteria;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface InstruCalibrationServiceV2 {
    /**
     * 查询全部数据
     *
     * @param criteria /
     * @return /
     */
    List<InstruCalibrationDto> queryAll(InstruCalibrationQueryCriteria criteria);

    /**
     * 导出数据
     *
     * @param queryAll 待导出的数据
     * @param response /
     * @throws IOException /
     */
    void download(List<InstruCalibrationDto> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 分页查询
     *
     * @param criteria 条件
     * @param pageable 分页参数
     * @return /
     */
    Map<String, Object> queryAll(InstruCalibrationQueryCriteria criteria, Pageable pageable);

    /**
     * 根据ID查询仪器校准信息
     *
     * @param id /
     * @return /
     */
    InstruCalibration findById(Long id);

    /**
     * 更新
     *
     * @param resource 设备保养信息
     */
    void update(InstruCalibrationDto resource);

    /**
     * 新增设备保养信息
     *
     * @param resource 设备保养信息
     */
    void create(InstruCalibrationDto resource);

    /**
     * 删除
     *
     * @param ids /
     */
    void delete(Set<Long> ids);


    /**
     * @param instruId 仪器ID
     * @return 仪器校准信息
     */
    List<InstruCalibration> findByInstruId(Long instruId);
}
