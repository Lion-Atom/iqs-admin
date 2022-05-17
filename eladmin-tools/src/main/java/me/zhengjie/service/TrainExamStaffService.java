package me.zhengjie.service;

import me.zhengjie.domain.TrainExamStaff;
import me.zhengjie.service.dto.TrExamStaffDto;
import me.zhengjie.service.dto.TrainExamStaffQueryCriteria;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author TongMin Jie
 * @version V1
 * @date 2022/5/16 9:38
 */
public interface TrainExamStaffService {

    /**
     * 查询全部数据
     *
     * @param criteria /
     * @return /
     */
    List<TrExamStaffDto> queryAll(TrainExamStaffQueryCriteria criteria);

    /**
     * 导出数据
     *
     * @param queryAll 待导出的数据
     * @param response /
     * @throws IOException /
     */
    void download(List<TrExamStaffDto> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 分页查询
     *
     * @param criteria 条件
     * @param pageable 分页参数
     * @return /
     */
    Map<String, Object> queryAll(TrainExamStaffQueryCriteria criteria, Pageable pageable);

    /**
     * @param id 考生ID
     * @return 考生考试信息
     */
    TrExamStaffDto findById(Long id);

    /**
     * 新增
     *
     * @param resource 员工考试信息
     */
    void create(TrExamStaffDto resource);

    /**
     * 更新
     *
     * @param resource 新员工培训信息
     */
    void update(TrainExamStaff resource);

    /**
     * 删除
     *
     * @param ids 考生IDs
     */
    void delete(Set<Long> ids);
}
