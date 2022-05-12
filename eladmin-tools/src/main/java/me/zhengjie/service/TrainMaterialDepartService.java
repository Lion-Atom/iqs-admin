package me.zhengjie.service;

import me.zhengjie.domain.TrainMaterialDepart;
import me.zhengjie.service.dto.TrainExamDepartQueryCriteria;
import me.zhengjie.service.dto.TrainMaterialDepartDto;

import java.util.List;
import java.util.Set;

/**
 * @author TongMin Jie
 * @version V1
 * @date 2022/5/9 10:35
 */
public interface TrainMaterialDepartService {

    /**
     * 查询全部培训考试关联部门信息数据
     *
     * @return /
     */
    List<TrainMaterialDepartDto> queryAll(TrainExamDepartQueryCriteria criteria);


    /**
     * 更新
     *
     * @param resource 培训考试关联部门信息
     */
    void update(TrainMaterialDepart resource);

    /**
     * 新增考试关联部门信息
     *
     * @param resource 培训考试关联部门信息
     */
    void create(TrainMaterialDepart resource);

    /**
     * 删除
     *
     * @param ids /
     */
    void delete(Set<Long> ids);
}
