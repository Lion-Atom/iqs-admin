package me.zhengjie.service;

import me.zhengjie.domain.AuditPlanExecute;
import me.zhengjie.service.dto.AuditPlanExecuteDto;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/23 10:10
 */
public interface AuditPlanExecuteService {

    /**
     * 根据计划ID查询执行信息
     *
     * @param planId /计划标识
     * @return /
     */
    AuditPlanExecuteDto findByPlanId(Long planId);

    /**
     * 创建
     *
     * @param resources /
     */
    void create(AuditPlanExecute resources);

}
