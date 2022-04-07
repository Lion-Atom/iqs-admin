package me.zhengjie.service;

import me.zhengjie.domain.ChangeDesc;
import me.zhengjie.domain.Why;
import me.zhengjie.service.dto.CauseWhysDto;

import java.util.List;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/23 10:10
 */
public interface WhyService {

    /**
     * 根据原因ID查询
     *
     * @param causeId /
     * @return /
     */
    List<Why> findByCauseId(Long causeId);

    /**
     * 新增
     *
     * @param resources /
     */
    void create(List<Why> resources);

    /**
     * 编辑
     *
     * @param resources /
     */
    void update(List<Why> resources);

    /**
     * 根据原因ID查询原因及其对应的5whys数据
     *
     * @param issueId 问题ID
     * @return 原因及其对应的5whys数据
     */
    List<CauseWhysDto> findByIssueId(Long issueId);
}
