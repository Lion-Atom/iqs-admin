package me.zhengjie.service;

import me.zhengjie.domain.IssueNum;

import java.util.Map;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/23 10:10
 */
public interface IssueNumService {

    /**
     * 根据问题ID查询
     * @param issueId /
     * @return /
     */
    Map<String,Object> findByIssueId(Long issueId);

    /**
     * 创建
     * @param resources /
     */
    void create(IssueNum resources);

    /**
     * 编辑
     * @param resources /
     */
    void update(IssueNum resources);

    /**
     * @param ids 小组成员标识集合
     */
    void delete(Set<Long> ids);
}
