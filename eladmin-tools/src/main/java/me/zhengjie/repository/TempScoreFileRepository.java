package me.zhengjie.repository;

import me.zhengjie.domain.TempScoreFile;
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
public interface TempScoreFileRepository extends JpaRepository<TempScoreFile, Long>, JpaSpecificationExecutor<TempScoreFile> {


    /**
     * 根据审核计划下模板问题打分清单id删除附件
     *
     * @param scoreId 问题清单打分信息ID
     */
    @Modifying
    @Query(value = " delete from template_score_file where template_score_id = ?1 ", nativeQuery = true)
    void deleteByCerId(Long scoreId);

    /**
     * 根据审核计划id删除附件
     *
     * @param scoreIds 问题清单打分信息ID集合
     */
    @Modifying
    @Query(value = " delete from template_score_file where template_score_id in ?1 ", nativeQuery = true)
    void deleteByScoreIdIn(Set<Long> scoreIds);

    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * 根据审核计划id查询审核计划非模板的附件信息
     *
     * @param scoreId 打分ID
     * @return 审核计划模板信息
     */
    @Query(value = " select * from template_score_file where template_score_id = ?1 ", nativeQuery = true)
    List<TempScoreFile> findByScoreId(Long scoreId);

    @Query(value = "select plan_id from plan_template where " +
            " template_id  = (  " +
            " SELECT template_id FROM template_score where template_score_id = ( " +
            " select template_score_id from template_score_file where template_score_file_id = ?1 ) )", nativeQuery = true)
    Long findPlanIdById(Long id);
}
