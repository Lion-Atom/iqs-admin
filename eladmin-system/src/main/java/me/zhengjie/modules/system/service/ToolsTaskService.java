package me.zhengjie.modules.system.service;

import me.zhengjie.modules.system.domain.ToolsTask;
import me.zhengjie.modules.system.service.dto.TaskQueryCriteria;
import me.zhengjie.modules.system.service.dto.ToolsTaskDto;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/6/22 11:32
 */
public interface ToolsTaskService {

    /**
     * @param criteria 查询条件
     * @param pageable 分页器
     * @return 待审批任务列表
     */
    Map<String, Object> queryAll(TaskQueryCriteria criteria, Pageable pageable);

    /**
     * @return 任务数目
     */
    Integer queryTaskCount();

    /**
     * 更新任务信息
     *
     * @param task 任务信息
     */
    void update(ToolsTask task);

    /**
     * 审批任务
     *
     * @param task 任务
     */
    void submit(ToolsTask task);


    /**
     * @param tasks 任务列表
     */
    void batchSubmit(List<ToolsTask> tasks);
}
