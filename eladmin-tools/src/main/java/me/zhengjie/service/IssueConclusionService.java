package me.zhengjie.service;

import me.zhengjie.domain.IssueConclusion;

import java.util.List;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/23 10:10
 */
public interface IssueConclusionService {

    /**
     * 根据问题ID查询
     *
     * @param issueId /
     * @return /
     */
    IssueConclusion findByIssueId(Long issueId);

    /**
     * 编辑
     *
     * @param resource /
     */
    void update(IssueConclusion resource);

}
