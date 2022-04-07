package me.zhengjie.service;

import me.zhengjie.domain.ConAction;
import me.zhengjie.service.dto.ConActionDto;

import java.util.List;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/30 13:33
 */
public interface ConActionService {

    /**
     * 查询围堵措施
     *
     * @param issueId 问题标识
     * @return 围堵措施列表
     */
    List<ConActionDto> findByIssueId(Long issueId);

    /**
     * 更改围堵措施信息
     *
     * @param resources 围堵措施信息
     */
    void update(ConAction resources);

    /**
     * 更改围堵措施信息，删除措施信息
     *
     * @param resources 删除措施信息
     */
    void clear(ConAction resources);
}
