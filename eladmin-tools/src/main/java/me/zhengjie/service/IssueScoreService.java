package me.zhengjie.service;

import me.zhengjie.domain.IssueScore;
import java.util.List;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/09/09 10:10
 */
public interface IssueScoreService {

    /**
     * 根据问题ID查询
     * @param issueId /
     * @return /
     */
    List<IssueScore> findByIssueId(Long issueId);

    /**
     * 编辑
     * @param resources /
     */
    void update(List<IssueScore> resources);

}
