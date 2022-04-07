package me.zhengjie.service;

import me.zhengjie.domain.ChangeFactor;

import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2022/1/18 10:51
 */
public interface ChangeFactorService {

    /**
     * 查询变更信息对应的影响因素
     *
     * @param changeId 变更信息ID
     * @return 变更因素集合
     */
    List<ChangeFactor> findByChangeId(Long changeId);

    /**
     * 新增
     *
     * @param resources 变更因素信息
     * @return 变更因素最新信息
     */
    ChangeFactor create(ChangeFactor resources);

    /**
     * 更新
     *
     * @param resources 变更因素信息
     */
    void update(ChangeFactor resources);

    /**
     * 删除
     *
     * @param ids 选择删除的ID集合
     */
    void delete(Set<Long> ids);
}
