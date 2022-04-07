package me.zhengjie.service;

import me.zhengjie.domain.IssueNum;
import me.zhengjie.domain.StepDefect;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/23 10:10
 */
public interface StepDefectService {

    /**
     * 根据问题ID查询
     * @param issueId /
     * @return /
     */
    List<StepDefect> findByIssueId(Long issueId);

    /**
     * 编辑
     * @param resources /
     */
    void update(List<StepDefect> resources);

}
