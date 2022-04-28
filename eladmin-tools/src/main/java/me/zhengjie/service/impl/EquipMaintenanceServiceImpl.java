package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.EquipMaintenance;
import me.zhengjie.domain.Equipment;
import me.zhengjie.repository.EquipMaintenanceRepository;
import me.zhengjie.repository.EquipmentRepository;
import me.zhengjie.repository.MaintainFileRepository;
import me.zhengjie.service.EquipMaintenanceService;
import me.zhengjie.service.dto.EquipMaintainQueryCriteria;
import me.zhengjie.service.dto.EquipMaintenanceDto;
import me.zhengjie.service.mapstruct.EquipMaintenanceMapper;
import me.zhengjie.utils.FileUtil;
import me.zhengjie.utils.PageUtil;
import me.zhengjie.utils.QueryHelp;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EquipMaintenanceServiceImpl implements EquipMaintenanceService {

    private final EquipMaintenanceRepository maintenanceRepository;
    private final MaintainFileRepository fileRepository;
    private final EquipmentRepository equipmentRepository;
    private final EquipMaintenanceMapper maintenanceMapper;

    @Override
    public List<EquipMaintenanceDto> queryAll(EquipMaintainQueryCriteria criteria) {
        List<EquipMaintenanceDto> list = new ArrayList<>();
        List<EquipMaintenance> maintenanceList = maintenanceRepository.findAll(((root, query, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder)));
        if (ValidationUtil.isNotEmpty(maintenanceList)) {
            list = maintenanceMapper.toDto(maintenanceList);
            Set<Long> equipIds = new HashSet<>();
            Map<Long, String> equipNameMap = new HashMap<>();
            Map<Long, String> equipNumMap = new HashMap<>();
            list.forEach(maintenance -> {
                equipIds.add(maintenance.getEquipmentId());
            });
            if (!equipIds.isEmpty()) {
                List<Equipment> equipmentList = equipmentRepository.findByIdIn(equipIds);
                if (ValidationUtil.isNotEmpty(equipmentList)) {
                    equipmentList.forEach(dto -> {
                        equipNameMap.put(dto.getId(), dto.getEquipName());
                        equipNumMap.put(dto.getId(), dto.getEquipNum());
                    });
                }
                // 获取设备基础信息
                list.forEach(dto -> {
                    dto.setEquipName(equipNameMap.get(dto.getEquipmentId()));
                    dto.setEquipNum(equipNumMap.get(dto.getEquipmentId()));
                });
                // todo 获取设备保养单信息
            }
        }
        return list;
    }

    @Override
    public void download(List<EquipMaintenanceDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (EquipMaintenanceDto dto : queryAll) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("设备编号", dto.getEquipNum());
            map.put("设备名称", dto.getEquipName());
            map.put("保养日期", ValidationUtil.transToDate(dto.getMaintainDate()));
            map.put("保养人员", dto.getMaintainBy());
            map.put("保养时长", dto.getMaintainDuration());
            map.put("确认人", dto.getConfirmBy());
            map.put("创建时间", dto.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public Map<String, Object> queryAll(EquipMaintainQueryCriteria criteria, Pageable pageable) {
        return PageUtil.toPage(maintenanceRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable));
    }

    @Override
    public EquipMaintenance findById(Long id) {
        return maintenanceRepository.findById(id).orElseGet(EquipMaintenance::new);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(EquipMaintenanceDto resource) {
        Equipment equipment = equipmentRepository.findById(resource.getEquipmentId()).orElseGet(Equipment::new);
        ValidationUtil.isNull(equipment.getId(), "Equipment", "id", resource.getEquipmentId());
        maintenanceRepository.save(maintenanceMapper.toEntity(resource));
        EquipMaintenance max = maintenanceRepository.findMaxByEquipId(equipment.getId());
        setEquipMaintenanceInfo(equipment, max);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(EquipMaintenanceDto resource) {
        Equipment equipment = equipmentRepository.findById(resource.getEquipmentId()).orElseGet(Equipment::new);
        ValidationUtil.isNull(equipment.getId(), "Equipment", "id", resource.getEquipmentId());
        // 设备保养信息
        EquipMaintenance maintenance = maintenanceRepository.save(maintenanceMapper.toEntity(resource));
        // 文件列表
        if (ValidationUtil.isNotEmpty(resource.getFileList())) {
            resource.getFileList().forEach(file -> {
                file.setMaintenanceId(maintenance.getId());
            });
            fileRepository.saveAll(resource.getFileList());
        }
        // 关联反馈到设备保养相关基础信息-上次保养日期和保养到期日期
        EquipMaintenance max = maintenanceRepository.findMaxByEquipId(equipment.getId());
        setEquipMaintenanceInfo(equipment, max);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        List<Long> list = new ArrayList<>(ids);
        EquipMaintenance maintenance = maintenanceRepository.findById(list.get(0)).orElseGet(EquipMaintenance::new);
        ValidationUtil.isNull(maintenance.getId(), "EquipMaintenance", "id", list.get(0));
        Equipment equipment = equipmentRepository.findById(maintenance.getEquipmentId()).orElseGet(Equipment::new);
        ValidationUtil.isNull(equipment.getId(), "Equipment", "id", maintenance.getEquipmentId());
        maintenanceRepository.deleteAllByIdIn(ids);
        EquipMaintenance max = maintenanceRepository.findMaxByEquipId(maintenance.getEquipmentId());
        setEquipMaintenanceInfo(equipment, max);
    }

    private void setEquipMaintenanceInfo(Equipment equipment, EquipMaintenance max) {
        if (max != null) {
            equipment.setLastMaintainDate(max.getMaintainDate());
            // todo 根据上次设置到期日期
            long unit = (long) (24 * 3600 * 1000);
            if (equipment.getMaintainPeriodUnit().equals(CommonConstants.PERIOD_UNIT_YEAR)) {
                // 年度
                unit = 360 * unit;
            } else if (equipment.getMaintainPeriodUnit().equals(CommonConstants.PERIOD_UNIT_QUARTER)) {
                // 季度-3个月
                unit = 90 * unit;
            } else if (equipment.getMaintainPeriodUnit().equals(CommonConstants.PERIOD_UNIT_MONTH)) {
                // 月
                unit = 30 * unit;
            } else if (equipment.getMaintainPeriodUnit().equals(CommonConstants.PERIOD_UNIT_WEEK)) {
                // 周
                unit = 7 * unit;
            }
            long dueDate = equipment.getLastMaintainDate().getTime() + unit * equipment.getMaintainPeriod();
            equipment.setMaintainDueDate(new Timestamp(dueDate));
            equipmentRepository.save(equipment);
        } else {
            equipment.setLastMaintainDate(null);
            equipment.setMaintainDueDate(null);
            equipmentRepository.save(equipment);
        }
    }

    @Override
    public List<EquipMaintenance> findByEquipmentId(Long equipmentId) {
        Equipment equipment = equipmentRepository.findById(equipmentId).orElseGet(Equipment::new);
        ValidationUtil.isNull(equipment.getId(), "Equipment", "id", equipmentId);
        return maintenanceRepository.findByEquipId(equipmentId);
    }
}
