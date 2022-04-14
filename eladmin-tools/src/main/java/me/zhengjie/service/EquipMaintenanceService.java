package me.zhengjie.service;

import me.zhengjie.domain.EquipMaintenance;
import me.zhengjie.service.dto.EquipMaintainQueryCriteria;
import me.zhengjie.service.dto.EquipMaintenanceDto;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface EquipMaintenanceService {
    /**
     * 查询全部数据
     *
     * @param criteria /
     * @return /
     */
    List<EquipMaintenanceDto> queryAll(EquipMaintainQueryCriteria criteria);

    /**
     * 导出数据
     *
     * @param queryAll 待导出的数据
     * @param response /
     * @throws IOException /
     */
    void download(List<EquipMaintenanceDto> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 分页查询
     *
     * @param criteria 条件
     * @param pageable 分页参数
     * @return /
     */
    Map<String, Object> queryAll(EquipMaintainQueryCriteria criteria, Pageable pageable);

    /**
     * 根据ID查询仪器校准信息
     *
     * @param id /
     * @return /
     */
    EquipMaintenance findById(Long id);

    /**
     * 更新
     *
     * @param resource 设备保养信息
     */
    void update(EquipMaintenanceDto resource);

    /**
     * 新增设备保养信息
     *
     * @param resource 设备保养信息
     */
    void create(EquipMaintenanceDto resource);

    /**
     * 删除
     *
     * @param ids /
     */
    void delete(Set<Long> ids);


    /**
     * @param equipmentId 设备ID
     * @return 设备保养信息
     */
    List<EquipMaintenance> findByEquipmentId(Long equipmentId);
}
