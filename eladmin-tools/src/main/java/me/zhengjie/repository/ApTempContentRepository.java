package me.zhengjie.repository;

import me.zhengjie.domain.ApTemplateContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/9/7 10:28
 */
public interface ApTempContentRepository extends JpaRepository<ApTemplateContent, Long>, JpaSpecificationExecutor<ApTemplateContent> {

    /**
     * 根据模板id删除模板问题清单分数分布
     *
     * @param templateId 模板ID
     */
    @Modifying
    @Query(value = " delete from template_content where template_id = ?1 ", nativeQuery = true)
    void deleteByTemplateId(Long templateId);
}
