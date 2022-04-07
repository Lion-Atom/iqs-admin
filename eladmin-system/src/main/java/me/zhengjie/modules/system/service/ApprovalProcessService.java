package me.zhengjie.modules.system.service;

import me.zhengjie.modules.system.domain.ApprovalProcess;
import me.zhengjie.modules.system.service.dto.ApprovalProcessQueryCriteria;
import me.zhengjie.modules.system.service.dto.TaskQueryCriteria;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/6/22 11:32
 */
public interface ApprovalProcessService {

    /**
     * @param criteria 查询条件
     * @param pageable 分页器
     * @return 审批清单
     */
    Map<String, Object> queryAll(ApprovalProcessQueryCriteria criteria, Pageable pageable);

    /**
     * 发起人
     *
     * @param process 审批流程信息
     */
    void update(ApprovalProcess process);
}
