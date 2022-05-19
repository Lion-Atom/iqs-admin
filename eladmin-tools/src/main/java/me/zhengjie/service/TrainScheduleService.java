package me.zhengjie.service;

import me.zhengjie.domain.TrainSchedule;
import me.zhengjie.service.dto.TrainScheduleDto;
import me.zhengjie.service.dto.TrainScheduleQueryCriteria;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author TongMin Jie
 * @version V1
 * @date 2022/5/18 14:33
 */
public interface TrainScheduleService {
    /**
     * 查询全部数据
     *
     * @param criteria /
     * @return /
     */
    List<TrainScheduleDto> queryAll(TrainScheduleQueryCriteria criteria);

    /**
     * 导出数据
     *
     * @param queryAll 待导出的数据
     * @param response /
     * @throws IOException /
     */
    void download(List<TrainScheduleDto> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 分页查询
     *
     * @param criteria 条件
     * @param pageable 分页参数
     * @return /
     */
    Map<String, Object> queryAll(TrainScheduleQueryCriteria criteria, Pageable pageable);

    /**
     * 根据ID查询新员工培训信息
     *
     * @param id /
     * @return /
     */
    TrainScheduleDto findById(Long id);

    /**
     * 更新
     *
     * @param resource 新员工培训信息
     */
    void update(TrainSchedule resource);

    /**
     * 新增新员工培训信息
     *
     * @param resource 新员工培训信息
     */
    void create(TrainScheduleDto resource);

    /**
     * 删除
     *
     * @param ids /
     */
    void delete(Set<Long> ids);
}
