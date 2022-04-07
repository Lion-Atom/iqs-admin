package me.zhengjie.service;

import me.zhengjie.domain.ChangeManagement;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2022/1/18 10:51
 */
public interface ChangeManagementService {

    /**
     * 查询变更信息对应的影响因素
     *
     * @param changeId 变更信息ID
     * @return 变更因素集合
     */
    ChangeManagement findByChangeId(Long changeId);

    /**
     * 新增
     *
     * @param resources 变更因素信息
     * @return 变更因素最新信息
     */
    ChangeManagement create(ChangeManagement resources);

    /**
     * 更新
     *
     * @param resources 变更因素信息
     */
    Map<String, Object> update(ChangeManagement resources);

    /**
     * 删除
     *
     * @param ids 选择删除的ID集合
     */
    void delete(Set<Long> ids);
}
