/*
 *  Copyright 2019-2020 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package me.zhengjie.service;

import me.zhengjie.base.CommonDTO;
import me.zhengjie.domain.AuditPlan;
import me.zhengjie.domain.FileLevel;
import me.zhengjie.service.dto.*;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author TongMinjie
 * @date 2021-09-06
 */
public interface AuditPlanService {

    /**
     * 分页查询
     *
     * @param criteria 条件
     * @param pageable 分页参数
     * @return /
     */
    Map<String, Object> queryAll(AuditPlanQueryCriteria criteria, Pageable pageable);

    /**
     * 查询全部数据
     *
     * @param criteria /
     * @return /
     */
    List<AuditPlanDto> queryAll(AuditPlanQueryCriteria criteria);

    /**
     * 根据ID查询
     *
     * @param id /
     * @return /
     */
    AuditPlanDto findById(Long id);

    /**
     * 创建
     *
     * @param resources /
     */
    void create(AuditPlan resources);

    /**
     * 编辑
     *
     * @param resources /
     */
    void update(AuditPlan resources);

    /**
     * 删除
     *
     * @param ids /
     */
    void delete(Set<Long> ids);

    /**
     * 导出数据
     *
     * @param queryAll 待导出的数据
     * @param response /
     * @throws IOException /
     */
    void download(List<AuditPlanDto> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 条件查询审核计划信息
     *
     * @param queryDto 查询条件
     * @return 审核计划信息
     */
    List<AuditPlan> findByExample(AuditPlanQueryDto queryDto);

    /**
     * 激活审核计划审核流程
     *
     * @param planId 审核计划标识
     */
    void activatedById(Long planId);

    /**
     * 检测是否具备执行修改权限
     *
     * @param planId 审核计划标识
     */
    void checkHasAuthExecute(Long planId);

    /**
     * 审核计划结案提交
     *
     * @param planId 审核计划标识
     */
    void submit(Long planId);

    /**
     * @return 审核计划执行情况
     */
    Map<String, Object> queryAuditPlansByStatus();

    /**
     * 根据年/月份条件查询计划信息
     *
     * @param dto 查询条件
     * @return 计划信息
     */
    Map<String, Object> queryAuditPlansByDate(ApQueryDto dto);

    /**
     * @return 获取审核计划年度分布
     */
    Map<String,Object> getRtdByYear();

    /**
     * @return 获取审核计划月度分布
     */
    Map<String,Object> getRtdByMonth();

    /**
     * 条件查询审核计划信息V2
     *
     * @param queryDto 查询条件V2
     * @return 审核计划信息
     */
    List<AuditPlan> findByExampleV2(AuditPlanV2QueryDto queryDto);
}