package me.zhengjie.service;

import me.zhengjie.domain.IssueSpecial;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/23 10:10
 */
public interface IssueSpecialService {

    /**
     * 根据问题ID查询
     *
     * @param issueId /
     * @return /
     */
    IssueSpecial findByIssueIdAndType(Long issueId, String type);

    /**
     * 新增
     *
     * @param resource /
     */
    void create(IssueSpecial resource);

    /**
     * 编辑
     *
     * @param resource /
     */
    void update(IssueSpecial resource);


    /**
     * 删除特殊事件
     *
     * @param issueId 问题标识
     */
    void delByIssueId(Long issueId);
}
