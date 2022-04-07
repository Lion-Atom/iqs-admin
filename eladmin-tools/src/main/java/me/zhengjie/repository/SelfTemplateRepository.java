package me.zhengjie.repository;

import me.zhengjie.domain.SelfTemplate;
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
public interface SelfTemplateRepository extends JpaRepository<SelfTemplate, Long>, JpaSpecificationExecutor<SelfTemplate> {

    /**
     * 根据模板id删除自定义模板内容
     *
     * @param templateId 模板ID
     */
    @Modifying
    @Query(value = " delete  from self_template where template_id = ?1 ", nativeQuery = true)
    void deleteByTemplateId(Long templateId);


    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * 根据审核计划id查询审核计划模板信息
     *
     * @param templateId 模板ID
     * @return 审核计划模板信息
     */
    @Query(value = " select * from self_template where template_id = ?1  ", nativeQuery = true)
    List<SelfTemplate> findByTemplateId(Long templateId);

    @Query(value = " select * from self_template where self_template_id = ?1 and template_id = ?2 ", nativeQuery = true)
    SelfTemplate findByTemplateIdAndId(Long id, Long templateId);

    /**
     * @param pid 父ID
     * @return 自定义模板信息
     */
    @Query(value = " select * from self_template where pid = ?1 ", nativeQuery = true)
    List<SelfTemplate> findByPid(Long pid);

    @Query(value = " select template_id from self_template where self_template_id = ?1 ", nativeQuery = true)
    Long findTemplateIdById(Long id);

    @Query(value = " select * from self_template where template_id = ?1  and pid is null", nativeQuery = true)
    List<SelfTemplate> findByTemplateIdAndPidIsNull(Long templateId);

    /**
     * @param pid 父ID
     * @return 自定义模板信息
     */
    @Query(value = " select self_template_id from self_template where pid = ?1 ", nativeQuery = true)
    List<Long> findChildIdByPid(Long pid);

    /**
     * 查询同名根节点项目集
     *
     * @param templateTd 模板标识
     * @param itemName   项目名称
     * @return 同名项目
     */
    @Query(value = " select * from self_template where template_id = ?1 " +
            " and item_name = ?2 " +
            " and pid is null ", nativeQuery = true)
    SelfTemplate findByItemNameAndPidIsNull(Long templateTd, String itemName);

    /**
     * 查询同名子节点项目集
     *
     * @param templateTd 模板标识
     * @param itemName   项目名称
     * @param pid        父标识
     * @return 同名项目
     */
    @Query(value = " select * from self_template where template_id = ?1 " +
            " and item_name = ?2 " +
            " and pid = ?3 ", nativeQuery = true)
    SelfTemplate findByItemNameAndPid(Long templateTd, String itemName, Long pid);

    @Query(value = " select * from self_template where template_id = ?1 " +
            " and pid in ?2 ", nativeQuery = true)
    List<SelfTemplate> findByPidIn(Long templateId, Set<Long> pidList);
}
