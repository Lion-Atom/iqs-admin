package me.zhengjie.service;

import me.zhengjie.domain.ApReportQuestion;
import me.zhengjie.domain.AuditPlanReport;
import me.zhengjie.service.dto.ApQuestionQueryCriteria;
import me.zhengjie.service.dto.ApReportQuestionDto;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/23 10:10
 */
public interface ApReportQuestionService {

    /**
     * 根据计划ID查询问题信息
     *
     * @param reportId /报告标识
     * @return /
     */
    List<ApReportQuestionDto> findByReportId(Long reportId);

    /**
     * 创建审核报告信息
     *
     * @param resources /
     */
    void create(ApReportQuestion resources);

    /**
     * 更新审核报告信息
     *
     * @param resources /
     */
    void update(ApReportQuestion resources);

    /**
     * 删除
     *
     * @param ids /
     */
    void delete(Set<Long> ids);

    /**
     * 问题改善完成修改
     *
     * @param id 问题标识
     */
    void completedById(Long id);


    /**
     * 分页查询
     *
     * @param criteria 条件
     * @param pageable 分页参数
     * @return /
     */
    Map<String, Object> queryAll(ApQuestionQueryCriteria criteria, Pageable pageable);

    /**
     * 查询全部数据
     *
     * @param criteria /
     * @return /
     */
    List<ApReportQuestionDto> queryAll(ApQuestionQueryCriteria criteria);

    /**
     * 导出数据
     *
     * @param queryAll 待导出的数据
     * @param response /
     * @throws IOException /
     */
    void download(List<ApReportQuestionDto> queryAll, HttpServletResponse response) throws IOException;


/*    *//**
     * 查询审核计划下
     * @param planId 审核计划ID
     * @return 审核计划下问题点集合
     *//*
    List<ApReportQuestionDto> findByPlanId(Long planId);*/
}
