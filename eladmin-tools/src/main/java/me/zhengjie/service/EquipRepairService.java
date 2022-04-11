package me.zhengjie.service;

import me.zhengjie.domain.EquipRepair;
import me.zhengjie.service.dto.EquipRepairDto;
import me.zhengjie.service.dto.EquipRepairQueryCriteria;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface EquipRepairService {

    /**
     * 查询全部数据
     *
     * @param criteria /
     * @return /
     */
    List<EquipRepairDto> queryAll(EquipRepairQueryCriteria criteria);

    /**
     * 导出数据
     *
     * @param queryAll 待导出的数据
     * @param response /
     * @throws IOException /
     */
    void download(List<EquipRepairDto> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 分页查询
     *
     * @param criteria 条件
     * @param pageable 分页参数
     * @return /
     */
    Map<String, Object> queryAll(EquipRepairQueryCriteria criteria, Pageable pageable);

    /**
     * 根据ID查询仪器维修信息
     *
     * @param id /
     * @return /
     */
    EquipRepairDto findById(Long id);

    /**
     * 更新
     *
     * @param resource 仪器维修信息
     */
    void update(EquipRepairDto resource);

    /**
     * 新增仪器维修信息
     *
     * @param resource 仪器维修信息
     */
    void create(EquipRepairDto resource);

    /**
     * 删除
     *
     * @param ids /
     */
    void delete(Set<Long> ids);


    /**
     * @param equipmentId 设备ID
     * @return 设备维修信息
     */
    List<EquipRepairDto> findByEquipmentId(Long equipmentId);


    /**
     * 自动化生成设备维修单号
     *
     * @return 设备维修单号
     */
    String initRepairNum();
}
