package me.zhengjie.service;

import me.zhengjie.domain.Equipment;
import me.zhengjie.service.dto.EquipmentDto;
import me.zhengjie.service.dto.EquipmentQueryByExample;
import me.zhengjie.service.dto.EquipmentQueryCriteria;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2022-03-30
 */
public interface EquipmentService {

    /**
     * 查询全部数据
     *
     * @param criteria /
     * @return /
     */
    List<EquipmentDto> queryAll(EquipmentQueryCriteria criteria);

    /**
     * 导出数据
     *
     * @param queryAll 待导出的数据
     * @param response /
     * @throws IOException /
     */
    void download(List<EquipmentDto> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 分页查询
     *
     * @param criteria 条件
     * @param pageable 分页参数
     * @return /
     */
    Map<String, Object> queryAll(EquipmentQueryCriteria criteria, Pageable pageable);

    /**
     * 根据ID查询仪器校准信息
     *
     * @param id /
     * @return /
     */
    EquipmentDto findById(Long id);

    /**
     * 更新
     *
     * @param resource 仪器校准信息
     */
    void update(Equipment resource);

    /**
     * 新增仪器校准信息
     *
     * @param resource 仪器校准信息
     */
    void create(Equipment resource);

    /**
     * 删除
     *
     * @param ids /
     */
    void delete(Set<Long> ids);


    /**
     * 条件查询设备信息集合
     * @param queryByExample 查询入参
     * @return 设备信息列表
     */
    List<Equipment> queryByExample(EquipmentQueryByExample queryByExample);
}
