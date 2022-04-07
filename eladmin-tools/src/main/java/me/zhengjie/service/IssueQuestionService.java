package me.zhengjie.service;

import me.zhengjie.domain.IssueQuestion;

import java.util.List;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/23 10:10
 */
public interface IssueQuestionService {

    /**
     * 根据问题ID和选择类型查询
     *
     * @param issueId /
     * @return /
     */
    List<IssueQuestion> findByIssueIdAndType(Long issueId,String type);

    /**
     * 根据问题ID查询
     *
     * @param issueId /
     * @return /
     */
    List<IssueQuestion> findByIssueId(Long issueId);

    /**
     * 编辑
     *
     * @param resources /
     */
    void update(List<IssueQuestion> resources);

}
