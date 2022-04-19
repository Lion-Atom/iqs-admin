package me.zhengjie.service;

import me.zhengjie.domain.EquipAcceptanceDetail;

import java.util.List;

/**
 * @author tmj
 * @version 1.0
 * @date 2022/04/19 10:10
 */
public interface EquipAcceptanceDetailService {

    /**
     * 根据设备验收ID查询
     *
     * @param acceptanceId /
     * @return /
     */
    List<EquipAcceptanceDetail> findByAcceptanceId(Long acceptanceId);

    /**
     * 编辑
     *
     * @param resources /
     */
    void update(List<EquipAcceptanceDetail> resources);

}
