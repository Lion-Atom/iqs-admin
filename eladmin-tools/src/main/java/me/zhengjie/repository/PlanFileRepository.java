package me.zhengjie.repository;

import me.zhengjie.domain.AuditPlanFile;
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
public interface PlanFileRepository extends JpaRepository<AuditPlanFile, Long>, JpaSpecificationExecutor<AuditPlanFile> {


    /**
     * 根据审核计划id删除附件
     *
     * @param planId 审核计划ID
     */
    @Modifying
    @Query(value = " delete from plan_file where plan_id = ?1 and template_id is null", nativeQuery = true)
    void deleteByPlanIdAndTemplateIdIsNull(Long planId);

    /**
     * 根据审核计划id和模板id删除附件
     *
     * @param templateId 模板ID
     * @param planId     审核计划ID
     */
    @Modifying
    @Query(value = " delete from plan_file where plan_id = ?1 and template_id = ?2", nativeQuery = true)
    void deleteByPlanIdAndTemplateId(Long planId, Long templateId);


    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * 根据审核计划id查询审核计划非模板的附件信息
     *
     * @param planId 审核计划ID
     * @return 审核计划模板信息
     */
    @Query(value = " select * from plan_file where plan_id = ?1 and template_id is null ", nativeQuery = true)
    List<AuditPlanFile> findByPlanIdAndTemplateIdIsNull(Long planId);

    /**
     * 根据审核计划id查询模板下附件信息
     *
     * @param templateId 模板ID
     * @param planId     审核计划ID
     * @return 审核计划模板信息
     */
    @Query(value = " select * from plan_file where plan_id = ?1 and template_id = ?2 ", nativeQuery = true)
    List<AuditPlanFile> findByPlanIdAndTemplateId(Long planId, Long templateId);

    /**
     * 根据审核计划id删除附件信息
     *
     * @param planIds 审核计划IDs
     */
    @Modifying
    @Query(value = " delete  from plan_file where plan_id in ?1 ", nativeQuery = true)
    void deleteByPlanIdIn(Set<Long> planIds);
}
