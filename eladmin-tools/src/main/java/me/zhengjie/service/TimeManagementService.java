package me.zhengjie.service;

import me.zhengjie.domain.TimeManagement;
import me.zhengjie.service.dto.TimeManagementDto;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/23 10:10
 */
public interface TimeManagementService {

    /**
     * 根据问题ID查询
     * @param issueId /
     * @return /
     */
    TimeManagementDto findByIssueId(Long issueId);

    /**
     * 创建
     * @param resources /
     */
    void create(TimeManagement resources);

    /**
     * 编辑
     * @param resources /
     */
    void update(TimeManagementDto resources);
}
