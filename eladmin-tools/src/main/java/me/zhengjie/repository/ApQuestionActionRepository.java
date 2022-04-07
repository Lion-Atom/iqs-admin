package me.zhengjie.repository;

import me.zhengjie.domain.ApQuestionAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;


/**
 * @author Tong Minjie
 * @date 2020-11-04
 */
@Repository
public interface ApQuestionActionRepository extends JpaRepository<ApQuestionAction, Long>, JpaSpecificationExecutor<ApQuestionAction> {

    /**
     * 查询问题对应的改善对策
     *
     * @param quesId 问题ID
     * @return 问题对应的改善对策信息
     */
    @Query(value = "SELECT * FROM  question_action WHERE  report_question_id = ?1", nativeQuery = true)
    List<ApQuestionAction> findByQuesId(Long quesId);

    /**
     * 查询审核计划对应的改善对策
     *
     * @param planId 审核计划ID
     * @return 问题对应的改善对策信息
     */
    @Query(value = "SELECT * FROM  question_action WHERE plan_id = ?1", nativeQuery = true)
    List<ApQuestionAction> findByPlanId(Long planId);

    /**
     * 根据审核计划id删除改善对策信息
     *
     * @param ids 问题标识集合
     */
    @Modifying
    @Query(value = " delete  from question_action where report_question_id in ?1 ", nativeQuery = true)
    void deleteAllByQuesIdIn(Set<Long> ids);

    /**
     * 根据标题全匹配查询改善对策
     *
     * @param title 对策标题
     * @return 改善对策
     */
    @Query(value = "SELECT * FROM question_action WHERE title = ?1", nativeQuery = true)
    ApQuestionAction findByTitle(String title);

    /**
     * 查询问题对应的改善对策
     *
     * @param quesId 问题ID
     * @return 问题对应的改善对策信息
     */
    @Query(value = "SELECT * FROM  question_action WHERE  report_question_id = ?1 and question_action_id <> ?2", nativeQuery = true)
    List<ApQuestionAction> findByQuesIdButNotId(Long quesId, Long id);
}
