package me.zhengjie.repository;

import me.zhengjie.domain.ApQuestionFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;


/**
 * @author Tong Minjie
 * @date 2020-09-14
 */
@Repository
public interface ApQuestionFileRepository extends JpaRepository<ApQuestionFile, Long>, JpaSpecificationExecutor<ApQuestionFile> {


    /**
     * 根据审核计划id删除附件
     *
     * @param quesId 审核计划ID
     */
    @Modifying
    @Query(value = " delete from plan_question_file where report_question_id = ?1 and question_action_id is null", nativeQuery = true)
    void deleteByQuesIdAndActIdIsNull(Long quesId);

    /**
     * 根据审核计划id和模板id删除附件
     *
     * @param actId  行动ID
     * @param quesId 审核计划ID
     */
    @Modifying
    @Query(value = " delete from plan_question_file where report_question_id = ?1 and question_action_id = ?2", nativeQuery = true)
    void deleteByQuesIdAndActId(Long quesId, Long actId);


    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * 根据审核计划id查询审核计划非模板的附件信息
     *
     * @param quesId 审核计划ID
     * @return 审核计划模板信息
     */
    @Query(value = " select * from plan_question_file where report_question_id = ?1 and question_action_id is null ", nativeQuery = true)
    List<ApQuestionFile> findByQuesIdAndActIdIsNull(Long quesId);

    /**
     * 根据审核计划id查询模板下附件信息
     *
     * @param actId  行动ID
     * @param quesId 审核计划ID
     * @return 审核计划模板信息
     */
    @Query(value = " select * from plan_question_file where report_question_id = ?1 and question_action_id = ?2 ", nativeQuery = true)
    List<ApQuestionFile> findByQuesIdAndActId(Long quesId, Long actId);

    /**
     * 根据审核计划id删除附件信息
     *
     * @param quesIds 审核计划IDs
     */
    @Modifying
    @Query(value = " delete  from plan_question_file where report_question_id in ?1 ", nativeQuery = true)
    void deleteByQuestionIn(Set<Long> quesIds);

    /**
     * 反查审核计划ID
     *
     * @param id 附件ID
     * @return /
     */
    @Query(value = "select plan_id from plan_report where " +
            " plan_report_id = ( " +
            " SELECT plan_report_id FROM `plan_report_question` where report_question_id = " +
            " ( select report_question_id from plan_question_file where plan_question_file_id = ?1 )" +
            " )", nativeQuery = true)
    Long findPlanIdByQuesFileId(Long id);

    /**
     * 根据审核报告问题id删除附件信息
     *
     * @param ids 问题标识集合
     */
    @Modifying
    @Query(value = " delete  from plan_question_file where report_question_id in ?1 ", nativeQuery = true)
    void deleteAllByQuesIdIn(Set<Long> ids);

    /**
     * 根据审核报告问题对应改善对策id删除附件信息
     *
     * @param actId 改善对策id
     */
    @Modifying
    @Query(value = " delete  from plan_question_file where question_action_id = ?1 ", nativeQuery = true)
    void deleteAllByActIdIn(Long actId);
}
