package me.zhengjie.service;

import me.zhengjie.domain.CsFeedback;
import me.zhengjie.domain.TrainNewStaff;
import me.zhengjie.service.dto.CsFeedbackDto;
import me.zhengjie.service.dto.CsFeedbackQueryCriteria;
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
 * @date 2022-07-15
 */
public interface CsFeedbackService {

    /**
     * 查询全部数据
     *
     * @param criteria /
     * @return /
     */
    List<CsFeedback> queryAll(CsFeedbackQueryCriteria criteria);

    /**
     * 导出数据
     *
     * @param queryAll 待导出的数据
     * @param response /
     * @throws IOException /
     */
    void download(List<CsFeedback> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 分页查询
     *
     * @param criteria 条件
     * @param pageable 分页参数
     * @return /
     */
    Map<String, Object> queryAll(CsFeedbackQueryCriteria criteria, Pageable pageable);


    /**
     * 更新
     *
     * @param resource 新员工培训信息
     */
    void update(CsFeedback resource);

    /**
     * 新增新员工培训信息
     *
     * @param resource 新员工培训信息
     */
    void create(CsFeedbackDto resource);

    /**
     * 删除
     *
     * @param ids /
     */
    void delete(Set<Long> ids);
}
