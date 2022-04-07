package me.zhengjie.service;

import me.zhengjie.domain.TemplateScore;
import me.zhengjie.service.dto.TemplateScoreDto;
import me.zhengjie.service.dto.TemplateScoreQueryDto;

import java.util.List;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/09/09 10:10
 */
public interface TemplateScoreService {

    /**
     * 查询模板问题清单
     *
     * @param dto 查询条件
     * @return 模板分数分布
     */
    List<TemplateScoreDto> getByTemplateIdAndTypes(TemplateScoreQueryDto dto);

    /**
     * 批量更新数据
     *
     * @param resources 模板问题列表
     */
    void update(List<TemplateScore> resources);
}
