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
package me.zhengjie.modules.system.service;

import me.zhengjie.modules.system.domain.Job;
import me.zhengjie.modules.system.service.dto.JobDto;
import me.zhengjie.modules.system.service.dto.JobQueryCriteria;
import org.springframework.data.domain.Pageable;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* @author Zheng Jie
* @date 2019-03-29
*/
public interface JobService {

    /**
     * 根据ID查询
     * @param id /
     * @return /
     */
    JobDto findById(Long id);

    /**
     * 创建
     * @param resources /
     * @return /
     */
    void create(Job resources);

    /**
     * 编辑
     * @param resources /
     */
    void update(Job resources);

    /**
     * 删除
     * @param ids /
     */
    void delete(Set<Long> ids);

    /**
     * 分页查询
     * @param criteria 条件
     * @param pageable 分页参数
     * @return /
     */
    Map<String,Object> queryAll(JobQueryCriteria criteria, Pageable pageable);

    /**
     * 查询全部数据
     * @param criteria /
     * @return /
     */
    List<JobDto> queryAll(JobQueryCriteria criteria);

    /**
     * 导出数据
     * @param queryAll 待导出的数据
     * @param response /
     * @throws IOException /
     */
    void download(List<JobDto> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 验证是否被用户关联
     * @param ids /
     */
    void verification(Set<Long> ids);
    
    /**
     * 根据PID查询
     * @param pid /
     * @return /
     */
    List<Job> findByPid(long pid);

    Set<JobDto> getDeleteDepts(List<Job> jobList, Set<JobDto> jobDtos);

    /**
     * 删除
     * @param jobDtos 待删除对象
     *
     */
    void deleteAll(Set<JobDto> jobDtos);

    /**
     * 查询所有数据
     * @param criteria 条件
     * @param isQuery /
     * @throws Exception /
     * @return /
     */
    List<JobDto> queryAllV2(JobQueryCriteria criteria, Boolean isQuery) throws Exception;

    /**
     * 根据ID获取同级与上级数据
     * @param jobDto /
     * @param jobs /
     * @return /
     */
    List<JobDto> getSuperior(JobDto jobDto, ArrayList<Job> jobs);

    /**
     * 构建树形数据
     * @param jobDtos /
     * @return /
     */
    Object buildTree(List<JobDto> jobDtos);
}