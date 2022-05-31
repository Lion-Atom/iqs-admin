package me.zhengjie.service;

import me.zhengjie.domain.CalibrationOrg;
import me.zhengjie.service.dto.CaliOrgQueryByExample;
import me.zhengjie.service.dto.CaliOrgQueryCriteria;
import me.zhengjie.service.dto.CalibrationOrgV2Dto;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2022/3/11 11:32
 */
public interface CaliOrgV2Service {

    /**
     * 查询全部数据
     *
     * @param criteria /
     * @return /
     */
    List<CalibrationOrg> queryAll(CaliOrgQueryCriteria criteria);

    /**
     * 分页查询
     *
     * @param criteria 条件
     * @param pageable 分页参数
     * @return /
     */
    Map<String, Object> queryAll(CaliOrgQueryCriteria criteria, Pageable pageable);

    /**
     * 导出数据
     *
     * @param queryAll 待导出的数据
     * @param response /
     * @throws IOException /
     */
    void download(List<CalibrationOrg> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 根据ID查询
     *
     * @param caliOrgId /
     * @return /
     */
    CalibrationOrg findById(Long caliOrgId);

    /**
     * 新增/创建
     *
     * @param resources /
     */
    void create(CalibrationOrgV2Dto resources);

    /**
     * 编辑
     *
     * @param resources /
     */
    void update(CalibrationOrg resources);

    /**
     * 删除
     *
     * @param ids /
     */
    void delete(Set<Long> ids);


    /**
     * @param queryByExample 启用状态
     * @return 校准机构信息
     */
    List<CalibrationOrg> queryByExample(CaliOrgQueryByExample queryByExample);
}
