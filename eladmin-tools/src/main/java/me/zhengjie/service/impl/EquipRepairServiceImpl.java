package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.EquipRepair;
import me.zhengjie.domain.Equipment;
import me.zhengjie.domain.RepairFile;
import me.zhengjie.domain.RepairPart;
import me.zhengjie.repository.EquipRepairRepository;
import me.zhengjie.repository.EquipmentRepository;
import me.zhengjie.repository.RepairFileRepository;
import me.zhengjie.repository.RepairPartRepository;
import me.zhengjie.service.EquipRepairService;
import me.zhengjie.service.dto.EquipRepairDto;
import me.zhengjie.service.dto.EquipRepairQueryCriteria;
import me.zhengjie.service.mapstruct.EquipRepairMapper;
import me.zhengjie.service.mapstruct.RepairPartMapper;
import me.zhengjie.utils.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EquipRepairServiceImpl implements EquipRepairService {

    private final EquipRepairRepository repairRepository;
    private final EquipmentRepository equipmentRepository;
    private final RepairFileRepository fileRepository;
    private final RepairPartRepository partRepository;
    private final RepairPartMapper partMapper;
    private final EquipRepairMapper repairMapper;

    @Override
    public List<EquipRepairDto> queryAll(EquipRepairQueryCriteria criteria) {
        List<EquipRepairDto> list = new ArrayList<>();
        List<EquipRepair> repairList = repairRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
        if (ValidationUtil.isNotEmpty(repairList)) {
            Set<Long> equipIds = new HashSet<>();
            Map<Long, String> equipMap = new HashMap<>();
            list = repairMapper.toDto(repairList);
            initAddEquipInfo(list, equipIds, equipMap);
        }
        return list;
    }

    @Override
    public void download(List<EquipRepairDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (EquipRepairDto dto : queryAll) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("维修单号", dto.getRepairNum());
            map.put("设备名称", dto.getEquipName());
            map.put("停机时间", dto.getShutTime());
            map.put("停机人员", dto.getShutBy());
            map.put("故障判定", dto.getIsFault() ? "故障" : "非故障");
            map.put("停机原因", dto.getJudgeReason());
            map.put("维修负责人", dto.getRepairBy());
            map.put("开始维修时间", dto.getRepairTime());
            map.put("结束维修时间", dto.getResolveTime());
            map.put("是否完成", dto.getIsFinished() ? "已完成" : "未完成");
            map.put("确认人", dto.getConfirmBy());
            map.put("确认时间", dto.getConfirmTime());
            map.put("创建日期", dto.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public Map<String, Object> queryAll(EquipRepairQueryCriteria criteria, Pageable pageable) {
        Page<EquipRepair> page = repairRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        Map<String, Object> map = new HashMap<>();
        List<EquipRepairDto> repairList = new ArrayList<>();
        long total = 0L;
        if (ValidationUtil.isNotEmpty(page.getContent())) {
            Set<Long> equipIds = new HashSet<>();
            Map<Long, String> equipMap = new HashMap<>();
            repairList = repairMapper.toDto(page.getContent());
            initAddEquipInfo(repairList, equipIds, equipMap);
            total = page.getTotalElements();
        }
        map.put("content", repairList);
        map.put("totalElements", total);
        return map;
    }

    private void initAddEquipInfo(List<EquipRepairDto> repairList, Set<Long> equipIds, Map<Long, String> equipMap) {
        repairList.forEach(repair -> {
            equipIds.add(repair.getEquipmentId());
        });
        // 设备名称格式化
        if (!equipIds.isEmpty()) {
            List<Equipment> equipmentList = equipmentRepository.findByIdIn(equipIds);
            if (ValidationUtil.isNotEmpty(equipmentList)) {
                equipmentList.forEach(dto -> {
                    equipMap.put(dto.getId(), dto.getEquipName());
                });
            }
        }
        // 获取设备维修确认单信息
        repairList.forEach(dto -> {
            dto.setEquipName(equipMap.get(dto.getEquipmentId()));
            List<RepairFile> fileList = fileRepository.findByRepairId(dto.getId());
            dto.setFileList(fileList);
            List<RepairPart> partList = partRepository.findByRepairId(dto.getId());
            dto.setPartList(partMapper.toDto(partList));
        });
    }

    @Override
    public EquipRepairDto findById(Long id) {
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(EquipRepairDto resource) {
        // 维修设备重复
        Equipment equipment = equipmentRepository.findById(resource.getEquipmentId()).orElseGet(Equipment::new);
        ValidationUtil.isNull(equipment.getId(), "Equipment", "id", resource.getEquipmentId());
        /*List<EquipRepair> repairs = repairRepository.findByEquipId(resource.getEquipmentId());
        if (repair.getId() != null && !repair.getId().equals(resource.getId())) {
            throw new BadRequestException("该设备已存在维修信息，请勿重复添加！");
        }*/
        if (!resource.getIsFinished()) {
            resource.setConfirmBy(null);
            resource.setConfirmTime(null);
            // todo 删除已有维修确认单文件信息
        }
        EquipRepair repair =  repairRepository.save(repairMapper.toEntity(resource));
        // 备件列表
        partRepository.deleteByRepairId(repair.getId());
        if (ValidationUtil.isNotEmpty(resource.getPartList())) {
            resource.getPartList().forEach(part -> {
                part.setId(null);
                part.setRepairId(repair.getId());
            });
            partRepository.saveAll(partMapper.toEntity(resource.getPartList()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(EquipRepairDto resource) {
        // 维修设备重复
        Equipment equipment = equipmentRepository.findById(resource.getEquipmentId()).orElseGet(Equipment::new);
        ValidationUtil.isNull(equipment.getId(), "Equipment", "id", resource.getEquipmentId());
        /*
        // 自动生成设备维修单号
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmssS");
        resource.setRepairNum(StringUtils.getPinyin(equipment.getEquipName()) + "-" + format.format(date));*/
        EquipRepair repair = repairRepository.save(repairMapper.toEntity(resource));
        // 文件列表
        if (ValidationUtil.isNotEmpty(resource.getFileList())) {
            resource.getFileList().forEach(file -> {
                file.setRepairId(repair.getId());
            });
            fileRepository.saveAll(resource.getFileList());
        }
        // 备件列表
        if (ValidationUtil.isNotEmpty(resource.getPartList())) {
            resource.getPartList().forEach(part -> {
                part.setRepairId(repair.getId());
            });
            partRepository.saveAll(partMapper.toEntity(resource.getPartList()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        repairRepository.deleteAllByIdIn(ids);
    }

    @Override
    public List<EquipRepairDto> findByEquipmentId(Long equipId) {
        List<EquipRepairDto> repairList = new ArrayList<>();
        List<EquipRepair> repairs = repairRepository.findByEquipId(equipId);
        if (ValidationUtil.isNotEmpty(repairs)) {
            Set<Long> equipIds = new HashSet<>();
            Map<Long, String> equipMap = new HashMap<>();
            repairList = repairMapper.toDto(repairs);
            initAddEquipInfo(repairList, equipIds, equipMap);
        }
        return repairList;
    }

    @Override
    public String initRepairNum() {
        List<EquipRepair> repairs = repairRepository.findByCreatedInToday();
        int num = repairs.size() + 1;
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        if (num > 9) {
            return "W" + format.format(date) + "00" + num;
        } else {
            return "W" + format.format(date) + "000" + num;
        }
    }
}
