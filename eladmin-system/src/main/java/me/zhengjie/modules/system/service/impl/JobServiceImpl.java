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
package me.zhengjie.modules.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.modules.system.domain.Job;
import me.zhengjie.modules.system.repository.UserRepository;
import me.zhengjie.modules.system.service.dto.JobQueryCriteria;
import me.zhengjie.utils.*;
import me.zhengjie.modules.system.repository.JobRepository;
import me.zhengjie.modules.system.service.JobService;
import me.zhengjie.modules.system.service.dto.JobDto;
import me.zhengjie.modules.system.service.mapstruct.JobMapper;
import me.zhengjie.utils.enums.DataScopeEnum;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Zheng Jie
 * @date 2019-03-29
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "job")
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final JobMapper jobMapper;
    private final RedisUtils redisUtils;
    private final UserRepository userRepository;

    @Override
    public Map<String, Object> queryAll(JobQueryCriteria criteria, Pageable pageable) {
        Page<Job> page = jobRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        return PageUtil.toPage(page.map(jobMapper::toDto).getContent(), page.getTotalElements());
    }

    @Override
    public List<JobDto> queryAll(JobQueryCriteria criteria) {
        List<Job> list = jobRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
        return jobMapper.toDto(list);
    }

    @Override
    @Cacheable(key = "'id:' + #p0")
    public JobDto findById(Long id) {
        Job job = jobRepository.findById(id).orElseGet(Job::new);
        ValidationUtil.isNull(job.getId(), "ToolsJob", "id", id);
        return jobMapper.toDto(job);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Job resources) {
        Job job = jobRepository.findByName(resources.getName());
        if (job != null) {
            throw new EntityExistException(Job.class, "name", resources.getName());
        }
        jobRepository.save(resources);
        // 计算子节点数目
        resources.setSubCount(0);
        // 变更父节点下子节点数据
        updateSubCnt(resources.getPid());
    }

    private void updateSubCnt(Long jobId) {
        if (jobId != null) {
            int count = jobRepository.countByPid(jobId);
            jobRepository.updateSubCntById(count, jobId);
        }
    }

    @Override
    @CacheEvict(key = "'id:' + #p0.id")
    @Transactional(rollbackFor = Exception.class)
    public void update(Job resources) {
        // 旧的部门
        Long oldPid = findById(resources.getId()).getPid();
        Long newPid = resources.getPid();
        if (resources.getPid() != null && resources.getId().equals(resources.getPid())) {
            throw new BadRequestException("上级不能为自己");
        }
        Job job = jobRepository.findById(resources.getId()).orElseGet(Job::new);
        Job old = jobRepository.findByName(resources.getName());
        if (old != null && !old.getId().equals(resources.getId())) {
            throw new EntityExistException(Job.class, "name", resources.getName());
        }
        ValidationUtil.isNull(job.getId(), "ToolsJob", "id", resources.getId());
        resources.setId(job.getId());
        jobRepository.save(resources);
        // 更新父节点中子节点数目
        updateSubCnt(oldPid);
        updateSubCnt(newPid);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        jobRepository.deleteAllByIdIn(ids);
        // 删除缓存
        redisUtils.delByKeys(CacheKey.JOB_ID, ids);
    }

    @Override
    public void download(List<JobDto> jobDtos, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (JobDto jobDTO : jobDtos) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("岗位名称", jobDTO.getName());
            map.put("岗位状态", jobDTO.getEnabled() ? "启用" : "停用");
            map.put("创建日期", jobDTO.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public void verification(Set<Long> ids) {
        if (userRepository.countByJobs(ids) > 0) {
            throw new BadRequestException("所选的岗位中存在用户关联，请解除关联再试！");
        }
    }

    @Override
    public List<Job> findByPid(long pid) {
        return jobRepository.findByPid(pid);
    }

    @Override
    public Set<JobDto> getDeleteDepts(List<Job> jobList, Set<JobDto> jobDtos) {
        for (Job job : jobList) {
            jobDtos.add(jobMapper.toDto(job));
            List<Job> depts = jobRepository.findByPid(job.getId());
            if(depts!=null && depts.size()!=0){
                getDeleteDepts(depts, jobDtos);
            }
        }
        return jobDtos;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAll(Set<JobDto> jobDtos) {
        for (JobDto jobDTO : jobDtos) {
            // 清理缓存
            delCaches(jobDTO.getId());
            jobRepository.deleteById(jobDTO.getId());
            updateSubCnt(jobDTO.getPid());
        }
    }

    @Override
    public List<JobDto> queryAllV2(JobQueryCriteria criteria, Boolean isQuery) throws Exception {
        Sort sort = Sort.by(Sort.Direction.ASC, "jobSort");
        String dataScopeType = SecurityUtils.getDataScopeType();
        if (isQuery) {
            if(dataScopeType.equals(DataScopeEnum.ALL.getValue())){
                criteria.setPidIsNull(true);
            }
            List<Field> fields = QueryHelp.getAllFields(criteria.getClass(), new ArrayList<>());
            List<String> fieldNames = new ArrayList<String>(){{ add("pidIsNull");add("enabled");}};
            for (Field field : fields) {
                //设置对象的访问权限，保证对private的属性的访问
                field.setAccessible(true);
                Object val = field.get(criteria);
                if(fieldNames.contains(field.getName())){
                    continue;
                }
                if (ObjectUtil.isNotNull(val)) {
                    criteria.setPidIsNull(null);
                    break;
                }
            }
        }
        List<JobDto> list = jobMapper.toDto(jobRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),sort));
        // 如果为空，就代表为自定义权限或者本级权限，就需要去重，不理解可以注释掉，看查询结果
        if(StringUtils.isBlank(dataScopeType)){
            return deduplication(list);
        }
        return list;
    }

    @Override
    public List<JobDto> getSuperior(JobDto jobDto, ArrayList<Job> jobs) {
        if(jobDto.getPid() == null){
            jobs.addAll(jobRepository.findByPidIsNull());
            return jobMapper.toDto(jobs);
        }
        jobs.addAll(jobRepository.findByPid(jobDto.getPid()));
        return getSuperior(findById(jobDto.getPid()), jobs);
    }

    @Override
    public Object buildTree(List<JobDto> jobDtos) {
        Set<JobDto> trees = new LinkedHashSet<>();
        Set<JobDto> depts= new LinkedHashSet<>();
        List<String> deptNames = jobDtos.stream().map(JobDto::getName).collect(Collectors.toList());
        boolean isChild;
        for (JobDto jobDTO : jobDtos) {
            isChild = false;
            if (jobDTO.getPid() == null) {
                trees.add(jobDTO);
            }
            for (JobDto it : jobDtos) {
                if (it.getPid() != null && jobDTO.getId().equals(it.getPid())) {
                    isChild = true;
                    if (jobDTO.getChildren() == null) {
                        jobDTO.setChildren(new ArrayList<>());
                    }
                    jobDTO.getChildren().add(it);
                }
            }
            if(isChild) {
                depts.add(jobDTO);
            } else if(jobDTO.getPid() != null &&  !deptNames.contains(findById(jobDTO.getPid()).getName())) {
                depts.add(jobDTO);
            }
        }

        if (CollectionUtil.isEmpty(trees)) {
            trees = depts;
        }
        Map<String,Object> map = new HashMap<>(2);
        map.put("totalElements",jobDtos.size());
        map.put("content",CollectionUtil.isEmpty(trees)? jobDtos :trees);
        return map;
    }

    private List<JobDto> deduplication(List<JobDto> list) {
        List<JobDto> jobDtos = new ArrayList<>();
        for (JobDto jobDto : list) {
            boolean flag = true;
            for (JobDto dto : list) {
                if (dto.getId().equals(jobDto.getPid())) {
                    flag = false;
                    break;
                }
            }
            if (flag){
                jobDtos.add(jobDto);
            }
        }
        return jobDtos;
    }

    private void delCaches(Long id) {
        redisUtils.del(CacheKey.JOB_ID + id);
    }
}