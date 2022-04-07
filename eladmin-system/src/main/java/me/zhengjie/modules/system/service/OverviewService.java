package me.zhengjie.modules.system.service;

import me.zhengjie.base.CommonDTO;
import me.zhengjie.modules.system.service.dto.OverviewQueryCriteria;

import java.util.List;
import java.util.Map;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/5/24 17:43
 */
public interface OverviewService {

    /**
     * @return 概览信息
     */
    Map<String, Object> queryAll();

    /**
     * @param isAdmin 是否有管理员权限
     * @return 文件分类对应的文件数目
     */
    Map<String, Object> queryFilesByType(Boolean isAdmin);

    /**
     * @param isAdmin 是否有管理员权限
     * @return 文件分类对应的文件数目
     */
    Map<String, Object> queryFilesByLevel(Boolean isAdmin);


    /**
     * @param isAdmin 是否有管理员权限
     * @return 部门对应的文件数目
     */
    Map<String, Object> queryFilesByFileDept(Boolean isAdmin);

    /**
     * 查询文件信息
     *
     * @param criteria 模糊查询条件
     * @return 对应信息
     */
    Map<String, Object> queryAllByCond(OverviewQueryCriteria criteria);

    /**
     * @return 执行选择对应的问题数目
     */
    Map<String, Object> queryIssuesByExecuteType();

    /**
     * @return 审核类型执行情况分布
     */
    Map<String, Object> queryAuditPlansByType();

    /**
     * @return 审核体系审核员分布
     */
    Map<String, Object> queryAuditorBySystem();

    /**
     * @return 审核计划原因分布
     */
    Map<String, Object> queryAuditorByReason();
}
