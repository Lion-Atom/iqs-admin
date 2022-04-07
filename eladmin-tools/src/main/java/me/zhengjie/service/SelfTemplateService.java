package me.zhengjie.service;

import me.zhengjie.domain.SelfTemplate;
import me.zhengjie.service.dto.SelfTemplateDto;
import me.zhengjie.service.dto.SelfTemplateQueryDto;

import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/11/25 9:32
 */
public interface SelfTemplateService {

    /**
     * 查询模板下自定义表信息
     *
     * @param templateId 模板ID
     * @return 自定义模板信息
     */
    List<SelfTemplateDto> findByTemplateId(Long templateId);

    /**
     * @param resources 自定义表信息
     */
    void create(SelfTemplateDto resources);

    /**
     * 批量更新模板信息
     *
     * @param resources /
     */
    void batchUpdate(List<SelfTemplate> resources);


    /**
     * 查询模板模块
     *
     * @param queryDto 查询条件
     * @return 自定义模板模块内容
     */
    List<SelfTemplate> findByExample(SelfTemplateQueryDto queryDto);


    /**
     * @param ids 自定义模板模块ids
     */
    void delete(Set<Long> ids);


    /**
     * 更新模板模块
     *
     * @param resource /
     */
    void update(SelfTemplate resource);


    /**
     * 根据模板ID查询自定义模板树数据
     *
     * @param templateId 模板ID
     * @return 自定义模板数据树
     */
    Object getTreeByTemplateId(Long templateId);


    /**
     * @param id 自定义模板数据ID
     * @return /
     */
    SelfTemplateDto getById(Long id);
}
