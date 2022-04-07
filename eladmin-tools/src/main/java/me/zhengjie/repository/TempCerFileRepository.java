package me.zhengjie.repository;

import me.zhengjie.domain.ApQuestionFile;
import me.zhengjie.domain.TempCerFile;
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
public interface TempCerFileRepository extends JpaRepository<TempCerFile, Long>, JpaSpecificationExecutor<TempCerFile> {


    /**
     * 根据审核计划id删除附件
     *
     * @param cerId 认证信息ID
     */
    @Modifying
    @Query(value = " delete from template_certificate_file where template_certificate_id = ?1 ", nativeQuery = true)
    void deleteByCerId(Long cerId);

    /**
     * 根据审核计划id删除附件
     *
     * @param cerIds 认证信息ID集合
     */
    @Modifying
    @Query(value = " delete from template_certificate_file where template_certificate_id in ?1 ", nativeQuery = true)
    void deleteByCerIdIn(Set<Long> cerIds);

    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * 根据审核计划id查询审核计划非模板的附件信息
     *
     * @param cerId 认证信息ID
     * @return 审核计划模板信息
     */
    @Query(value = " select * from template_certificate_file where template_certificate_id = ?1 ", nativeQuery = true)
    List<TempCerFile> findByCerId(Long cerId);

    @Query(value = "select plan_id from plan_template where " +
            " template_id  = (  " +
            " SELECT template_id FROM template_certificate where template_certificate_id = ( " +
            " select template_certificate_id from template_certificate_file where template_certificate_file_id = ?1 ) )", nativeQuery = true)
    Long findPlanIdById(Long id);
}
