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

import me.zhengjie.domain.IssueCause;
import me.zhengjie.service.dto.IssueCauseDto;
import me.zhengjie.service.dto.IssueCauseQueryCriteria;
import me.zhengjie.service.dto.IssueCauseQueryDto;

import java.util.List;
import java.util.Set;

/**
 * @author Zheng Jie
 * @date 2019-03-25
 */
public interface IssueCauseService {

    /**
     * 查询所有数据
     *
     * @param criteria 条件
     * @param isQuery  /
     * @return /
     * @throws Exception /
     */
    List<IssueCauseDto> queryAll(IssueCauseQueryCriteria criteria, Boolean isQuery) throws Exception;

    /**
     * 根据ID查询
     *
     * @param id /
     * @return /
     */
    IssueCauseDto findById(Long id);

    /**
     * 创建
     *
     * @param resources /
     */
    void create(IssueCause resources);

    /**
     * 编辑
     *
     * @param resources /
     */
    void update(IssueCause resources);

    /**
     * 删除
     *
     * @param IssueCauseDtos /
     */
    void delete(Set<IssueCauseDto> IssueCauseDtos);

    /**
     * 根据PID查询
     *
     * @param pid /
     * @return /
     */
    List<IssueCause> findByPid(long pid);

    /**
     * 获取待删除的文件等级
     *
     * @param IssueCauseList /
     * @param IssueCauseDtos /
     * @return /
     */
    Set<IssueCauseDto> getDeleteIssueCauses(List<IssueCause> IssueCauseList, Set<IssueCauseDto> IssueCauseDtos);

    /**
     * 根据ID获取同级与上级数据
     *
     * @param IssueCauseDto /
     * @param IssueCauses   /
     * @return /
     */
    List<IssueCauseDto> getSuperior(IssueCauseDto IssueCauseDto, List<IssueCause> IssueCauses);

    /**
     * 构建树形数据
     *
     * @param IssueCauseDtos /
     * @return /
     */
    Object buildTree(List<IssueCauseDto> IssueCauseDtos);

    /**
     * 根据条件查询根本原因信息
     *
     * @param queryDto 查询条件
     * @return 根本原因信息
     */
    List<IssueCauseDto> findByExample(IssueCauseQueryDto queryDto);

    /**
     * 根据问题ID获取根节点原因列表
     *
     * @param issueId 问题ID
     * @return 原因信息
     */
    List<IssueCauseDto> findByIssueId(Long issueId);

    /**
     * 获取原因树
     *
     * @param issueId 问题ID
     * @return 原因树
     */
    Object createTree(Long issueId);
}