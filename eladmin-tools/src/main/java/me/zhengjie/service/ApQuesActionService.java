package me.zhengjie.service;

import me.zhengjie.domain.ApQuestionAction;

import java.util.List;
import java.util.Map;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/11/5 9:32
 */
public interface ApQuesActionService {

    List<ApQuestionAction> findByPlanIdAndQuesId(Long planId, Long quesId);

    Map<String,Object> update(ApQuestionAction resources);

    void create(ApQuestionAction resources);

    Map<String,Object> delete(ApQuestionAction resources);
}
