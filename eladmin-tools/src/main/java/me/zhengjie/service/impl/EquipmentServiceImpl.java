package me.zhengjie.service.impl;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.Equipment;
import me.zhengjie.domain.FileDept;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.EquipmentRepository;
import me.zhengjie.repository.FileDeptRepository;
import me.zhengjie.service.EquipmentService;
import me.zhengjie.service.dto.EquipmentDto;
import me.zhengjie.service.dto.EquipmentQueryByExample;
import me.zhengjie.service.dto.EquipmentQueryCriteria;
import me.zhengjie.service.mapstruct.EquipmentMapper;
import me.zhengjie.utils.FileUtil;
import me.zhengjie.utils.QueryHelp;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor

public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentMapper equipmentMapper;
    private final FileDeptRepository deptRepository;
    private final EquipmentRepository equipmentRepository;

    @Override
    public List<EquipmentDto> queryAll(EquipmentQueryCriteria criteria) {
        List<EquipmentDto> list = new ArrayList<>();
        List<Equipment> equipmentList = equipmentRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
        if(ValidationUtil.isNotEmpty(equipmentList)) {
            Set<Long> deptIds = new HashSet<>();
            Map<Long,String> deptMap = new HashMap<>();
            list = equipmentMapper.toDto(equipmentList);
            list.forEach(equip->{
                deptIds.add(equip.getUseDepart());
            });
            initUseDepartName(list, deptIds, deptMap);
        }
        return list;
    }

    private void initUseDepartName(List<EquipmentDto> list, Set<Long> deptIds, Map<Long, String> deptMap) {
        if (!deptIds.isEmpty()) {
            List<FileDept> deptList = deptRepository.findByIdIn(deptIds);
            deptList.forEach(dept -> {
                deptMap.put(dept.getId(), dept.getName());
            });
            if (ValidationUtil.isNotEmpty(deptList)) {
                list.forEach(dto -> {
                    dto.setUseDepartName(deptMap.get(dto.getUseDepart()));
                });
            }
        }
    }

    @Override
    public void download(List<EquipmentDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (EquipmentDto dto : queryAll) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("设备名称", dto.getEquipName());
            map.put("设备编号", dto.getEquipNum());
            map.put("设备型号", dto.getEquipModel());
            map.put("资产号", dto.getAssetNum());
            map.put("设备厂家", dto.getEquipProvider());
            map.put("使用部门", dto.getUseDepartName());
            map.put("设备位置", dto.getUseArea());
            map.put("设备级别", dto.getEquipLevel());
            map.put("设备状态", dto.getStatus());
            map.put("保养级别", dto.getMaintainLevel());
            map.put("上次保养时间", dto.getLastMaintainDate());
            map.put("保养周期", dto.getMaintainPeriod());
            map.put("保养到期日期", dto.getMaintainDueDate());
            map.put("创建日期", dto.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public Map<String, Object> queryAll(EquipmentQueryCriteria criteria, Pageable pageable) {
        Page<Equipment> page = equipmentRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        Map<String, Object> map = new HashMap<>();
        List<EquipmentDto> list = new ArrayList<>();
        long total = 0L;
        if (ValidationUtil.isNotEmpty(page.getContent())) {
            Set<Long> deptIds = new HashSet<>();
            Map<Long,String> deptMap = new HashMap<>();
            list = equipmentMapper.toDto(page.getContent());
            list.forEach(dto -> {
                deptIds.add(dto.getUseDepart());
                // 添加其他子项目--验收部门+人员
                if (dto.getAcceptBy() != null) {
                    dto.setAcceptByList(dto.getAcceptBy().split(","));
                }
            });
            initUseDepartName(list, deptIds, deptMap);
            total = page.getTotalElements();
        }
        map.put("content", list);
        map.put("totalElements", total);
        return map;
    }

    @Override
    public EquipmentDto findById(Long id) {
        Equipment equipment = equipmentRepository.findById(id).orElseGet(Equipment::new);
        ValidationUtil.isNull(equipment.getId(), "Equipment", "id", id);
        EquipmentDto dto = equipmentMapper.toDto(equipment);
        if (dto.getAcceptBy() != null) {
            dto.setAcceptByList(dto.getAcceptBy().split(","));
        }
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Equipment resource) {
        Equipment old = equipmentRepository.findById(resource.getId()).orElseGet(Equipment::new);
        ValidationUtil.isNull(old.getId(), "Equipment", "id", resource.getId());
        // todo 校验是否发生了变化
        /*String str = compareObj(old, resource);
        if (ValidationUtil.isBlank(str)) {
            throw new BadRequestException("No Change Found!未检测到变化！无须重复提交！");
        }*/
        equipmentRepository.save(resource);
    }

    private String compareObj(Object oldBean, Object newBean) {
        StringBuilder str = new StringBuilder();
        // todo 填充变更项判断
        return str.toString();
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Equipment resource) {
        // todo 设备判重
        resource.setStatus("待验收");
        equipmentRepository.save(resource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        equipmentRepository.deleteAllByIdIn(ids);
    }

    @Override
    public List<Equipment> queryByExample(EquipmentQueryByExample queryDto) {
        return equipmentRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, queryDto, criteriaBuilder));
    }
}
