package me.zhengjie.service;

import me.zhengjie.domain.IssueAnalysis;
import me.zhengjie.service.dto.IssueAnalysisDto;

import java.util.List;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/23 10:10
 */
public interface IssueAnalysisService {

    /**
     * 根据问题ID查询
     *
     * @param issueId /
     * @return /
     */
    List<IssueAnalysisDto> findByIssueId(Long issueId);

    /**
     * 编辑
     *
     * @param resource /
     */
    void update(IssueAnalysis resource);

}
