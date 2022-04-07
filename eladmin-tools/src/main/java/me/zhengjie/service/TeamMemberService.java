package me.zhengjie.service;

import me.zhengjie.domain.TeamMember;

import java.util.Map;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/23 10:10
 */
public interface TeamMemberService {

    /**
     * 根据问题ID查询
     *
     * @param issueId /
     * @return /
     */
    Map<String, Object> findByIssueId(Long issueId);

    /**
     * 创建
     *
     * @param resources /
     */
    void create(TeamMember resources);

    /**
     * 编辑
     *
     * @param resources /
     */
    void update(TeamMember resources);

    /**
     * @param ids 小组成员标识集合
     */
    void delete(Set<Long> ids);

    /**
     * 更改8D权限判定
     *
     * @param issueId 问题标识
     */
    void checkEditAuthorized(Long issueId);

    /**
     * 判断D8管理层或组长权限
     *
     * @param issueId 问题标识
     */
    void checkSubmitAuthorized(Long issueId);

//    void checkConclusionAuthorized(Long issueId);
}
