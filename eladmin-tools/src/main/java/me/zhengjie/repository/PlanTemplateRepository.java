package me.zhengjie.repository;

import me.zhengjie.domain.PlanTemplate;
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
public interface PlanTemplateRepository extends JpaRepository<PlanTemplate, Long>, JpaSpecificationExecutor<PlanTemplate> {

    /**
     * 根据审核计划id删除模板
     *
     * @param planId 审核计划ID
     */
    @Modifying
    @Query(value = " delete  from plan_template where plan_id = ?1 ", nativeQuery = true)
    void deleteByPlanId(Long planId);


    /**
     * 根据审核计划id删除模板
     *
     * @param planIds 审核计划IDs
     */
    @Modifying
    @Query(value = " delete  from plan_template where plan_id in ?1 ", nativeQuery = true)
    void deleteByPlanIdIn(Set<Long> planIds);

    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * 根据审核计划id查询审核计划模板信息
     *
     * @param planId 审核计划ID
     * @return 审核计划模板信息
     */
    @Query(value = " select * from plan_template where plan_id = ?1 ", nativeQuery = true)
    PlanTemplate findByPlanId(Long planId);

    /**
     * 根据审核计划id查询审核计划模板信息
     *
     * @param templateType 模板类型
     * @param enabled 启用状态
     * @return 审核计划模板信息
     */
    @Query(value = " select * from plan_template where template_type = ?1 and enabled = ?2 ", nativeQuery = true)
    List<PlanTemplate> findByTempTypeAndDisEnabled(String templateType, Boolean enabled);
}
