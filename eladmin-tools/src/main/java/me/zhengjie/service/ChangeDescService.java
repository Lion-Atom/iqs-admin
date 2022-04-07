package me.zhengjie.service;

import me.zhengjie.domain.ChangeDesc;

import java.util.List;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/23 10:10
 */
public interface ChangeDescService {

    /**
     * 根据问题ID查询
     *
     * @param issueId /
     * @return /
     */
    List<ChangeDesc> findByIssueId(Long issueId);

    /**
     * 编辑
     *
     * @param resources /
     */
    void update(List<ChangeDesc> resources);

}
