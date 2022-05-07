package me.zhengjie.service;

import me.zhengjie.domain.TrainNewStaff;
import me.zhengjie.service.dto.TrainNewStaffDto;
import me.zhengjie.service.dto.TrainNewStaffQueryCriteria;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author TongMin Jie
 * @date 2022-05-06
 */
public interface TrainNewStaffService {

    /**
     * 查询全部数据
     *
     * @param criteria /
     * @return /
     */
    List<TrainNewStaffDto> queryAll(TrainNewStaffQueryCriteria criteria);

    /**
     * 导出数据
     *
     * @param queryAll 待导出的数据
     * @param response /
     * @throws IOException /
     */
    void download(List<TrainNewStaffDto> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 分页查询
     *
     * @param criteria 条件
     * @param pageable 分页参数
     * @return /
     */
    Map<String, Object> queryAll(TrainNewStaffQueryCriteria criteria, Pageable pageable);

    /**
     * 根据ID查询仪器校准信息
     *
     * @param id /
     * @return /
     */
    TrainNewStaffDto findById(Long id);

    /**
     * 更新
     *
     * @param resource 仪器校准信息
     */
    void update(TrainNewStaff resource);

    /**
     * 新增仪器校准信息
     *
     * @param resource 仪器校准信息
     */
    void create(TrainNewStaffDto resource);

    /**
     * 删除
     *
     * @param ids /
     */
    void delete(Set<Long> ids);
}
