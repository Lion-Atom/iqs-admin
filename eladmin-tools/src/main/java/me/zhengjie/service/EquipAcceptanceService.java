package me.zhengjie.service;

import me.zhengjie.domain.EquipAcceptance;
import me.zhengjie.domain.Equipment;
import me.zhengjie.service.dto.EquipAcceptanceDto;
import me.zhengjie.service.dto.EquipAcceptanceQueryCriteria;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface EquipAcceptanceService {

    /**
     * 查询全部数据
     *
     * @param criteria /
     * @return /
     */
    List<EquipAcceptanceDto> queryAll(EquipAcceptanceQueryCriteria criteria);

    /**
     * 导出数据
     *
     * @param queryAll 待导出的数据
     * @param response /
     * @throws IOException /
     */
    void download(List<EquipAcceptanceDto> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 分页查询
     *
     * @param criteria 条件
     * @param pageable 分页参数
     * @return /
     */
    Map<String, Object> queryAll(EquipAcceptanceQueryCriteria criteria, Pageable pageable);

    /**
     * 根据ID查询仪器校准信息
     *
     * @param id /
     * @return /
     */
    EquipAcceptanceDto findById(Long id);

    /**
     * 更新
     *
     * @param resource 仪器校准信息
     */
    void update(EquipAcceptance resource);

    /**
     * 新增仪器校准信息
     *
     * @param resource 仪器校准信息
     */
    void create(EquipAcceptance resource);

    /**
     * 删除
     *
     * @param ids /
     */
    void delete(Set<Long> ids);


    /**
     * @param equipmentId 设备ID
     * @return 设备验收信息
     */
    EquipAcceptance findByEquipmentId(Long equipmentId);
}
