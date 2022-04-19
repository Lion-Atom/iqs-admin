package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.EquipAcceptance;
import me.zhengjie.domain.Equipment;
import me.zhengjie.domain.FileDept;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.EquipAcceptanceRepository;
import me.zhengjie.repository.EquipmentRepository;
import me.zhengjie.repository.FileDeptRepository;
import me.zhengjie.service.EquipAcceptanceService;
import me.zhengjie.service.dto.EquipAcceptanceDto;
import me.zhengjie.service.dto.EquipAcceptanceQueryCriteria;
import me.zhengjie.service.mapstruct.EquipAcceptanceMapper;
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
public class EquipAcceptanceServiceImpl implements EquipAcceptanceService {

    private final EquipAcceptanceRepository acceptanceRepository;
    private final EquipmentRepository equipmentRepository;
    private final FileDeptRepository deptRepository;
    private final EquipAcceptanceMapper acceptanceMapper;

    @Override
    public List<EquipAcceptanceDto> queryAll(EquipAcceptanceQueryCriteria criteria) {
        List<EquipAcceptanceDto> list = new ArrayList<>();
        List<EquipAcceptance> acceptList = acceptanceRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
        if (ValidationUtil.isNotEmpty(acceptList)) {
            Set<Long> deptIds = new HashSet<>();
            Set<Long> equipIds = new HashSet<>();
            Map<Long, Equipment> equipMap = new HashMap<>();
            Map<Long, String> deptMap = new HashMap<>();
            list = acceptanceMapper.toDto(acceptList);
            initAddtional(list, deptIds, equipIds, equipMap, deptMap);
        }
        return list;
    }

    @Override
    public void download(List<EquipAcceptanceDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (EquipAcceptanceDto dto : queryAll) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("设备名称", dto.getEquipName());
            map.put("验收参与部门人员", dto.getAcceptParticipant());
            map.put("验收部门", dto.getAcceptDepartName());
            map.put("验收人", dto.getAcceptBy());
            map.put("提交人", dto.getSubmitBy());
            map.put("提交时间", dto.getSubmitTime());
            map.put("批准部门", dto.getAcceptDepartName());
            map.put("批准人", dto.getAcceptBy());
            map.put("批准日期", dto.getSubmitTime());
            map.put("创建日期", dto.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public Map<String, Object> queryAll(EquipAcceptanceQueryCriteria criteria, Pageable pageable) {
        Page<EquipAcceptance> page = acceptanceRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        Map<String, Object> map = new HashMap<>();
        List<EquipAcceptanceDto> list = new ArrayList<>();
        long total = 0L;
        if (ValidationUtil.isNotEmpty(page.getContent())) {
            Set<Long> deptIds = new HashSet<>();
            Set<Long> equipIds = new HashSet<>();
            Map<Long, Equipment> equipMap = new HashMap<>();
            Map<Long, String> deptMap = new HashMap<>();
            list = acceptanceMapper.toDto(page.getContent());
            initAddtional(list, deptIds, equipIds, equipMap, deptMap);
            total = page.getTotalElements();
        }
        map.put("content", list);
        map.put("totalElements", total);
        return map;
    }

    private void initAddtional(List<EquipAcceptanceDto> list, Set<Long> deptIds, Set<Long> equipIds, Map<Long, Equipment> equipMap, Map<Long, String> deptMap) {
        list.forEach(accept -> {
            deptIds.add(accept.getAcceptDepart());
            deptIds.add(accept.getApproveDepart());
            equipIds.add(accept.getEquipmentId());
        });
        // 设备名称格式化
        if (!equipIds.isEmpty()) {
            List<Equipment> equipmentList = equipmentRepository.findByIdIn(equipIds);
            if (ValidationUtil.isNotEmpty(equipmentList)) {
                equipmentList.forEach(dto -> {
                    equipMap.put(dto.getId(), dto);
                });
            }

        }
        // 部门名称格式化
        if (!deptIds.isEmpty()) {
            List<FileDept> deptList = deptRepository.findByIdIn(deptIds);
            deptList.forEach(dept -> {
                deptMap.put(dept.getId(), dept.getName());
            });
        }
        list.forEach(dto -> {
            dto.setAcceptDepartName(deptMap.get(dto.getAcceptDepart()));
            if (dto.getApproveDepart() != null) {
                dto.setApproveDepartName(deptMap.get(dto.getApproveDepart()));
            }
            // 设备信息MAP-值判空
            if (equipMap.get(dto.getEquipmentId()) != null) {
                dto.setEquipName(equipMap.get(dto.getEquipmentId()).getEquipName());
                dto.setEquipNum(equipMap.get(dto.getEquipmentId()).getEquipNum());
            }
            // 获取验收参与者
            if (dto.getAcceptParticipant() != null) {
                dto.setParticipantTags(dto.getAcceptParticipant().split(","));
            }
        });
    }

    @Override
    public EquipAcceptanceDto findById(Long id) {
        // 验收信息
        EquipAcceptance acceptance = acceptanceRepository.findById(id).orElseGet(EquipAcceptance::new);
        ValidationUtil.isNull(acceptance.getId(), "EquipAcceptance", "id", id);
        EquipAcceptanceDto dto = acceptanceMapper.toDto(acceptance);
        // todo 验收部门、验收人信息扩展
        // 获取验收参与者
        if (dto.getAcceptParticipant() != null) {
            dto.setParticipantTags(dto.getAcceptParticipant().split(","));
        }
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(EquipAcceptance resource) {
        // 校验设备重复
        Equipment equipment = equipmentRepository.findById(resource.getEquipmentId()).orElseGet(Equipment::new);
        ValidationUtil.isNull(equipment.getId(), "Equipment", "id", resource.getEquipmentId());
        EquipAcceptance acceptance = acceptanceRepository.findByEquipId(resource.getEquipmentId());
        if (acceptance.getId() != null && !acceptance.getId().equals(resource.getId())) {
            throw new BadRequestException("该设备已存在验收信息，请勿重复添加！");
        }
        acceptanceRepository.save(resource);
        if (!resource.getAcceptStatus().equals(equipment.getStatus())) {
            equipment.setStatus(resource.getAcceptStatus());
        }
        equipmentRepository.save(equipment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(EquipAcceptance resource) {
        // 校验设备重复
        Equipment equipment = equipmentRepository.findById(resource.getEquipmentId()).orElseGet(Equipment::new);
        ValidationUtil.isNull(equipment.getId(), "Equipment", "id", resource.getEquipmentId());
        EquipAcceptance acceptance = acceptanceRepository.findByEquipId(resource.getEquipmentId());
        if (acceptance != null) {
            throw new BadRequestException("该设备已存在验收信息，请勿重复添加！");
        }
        acceptanceRepository.save(resource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        acceptanceRepository.deleteAllByIdIn(ids);
        // 关联删除设备项目
        equipmentRepository.rollbackEquipStatus(ids);
    }

    @Override
    public EquipAcceptance findByEquipmentId(Long equipmentId) {
        return acceptanceRepository.findByEquipId(equipmentId);
    }
}
