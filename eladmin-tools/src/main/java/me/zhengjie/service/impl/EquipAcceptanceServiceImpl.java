package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.EquipAcceptance;
import me.zhengjie.domain.EquipAcceptanceDetail;
import me.zhengjie.domain.Equipment;
import me.zhengjie.domain.FileDept;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.EquipAcceptanceDetailRepository;
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
    private final EquipAcceptanceDetailRepository detailRepository;
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
            map.put("验收提交人", dto.getSubmitBy());
            map.put("验收提交时间", dto.getSubmitTime());
            map.put("批准部门", dto.getAcceptDepartName());
            map.put("批准人", dto.getAcceptBy());
            map.put("批准日期", dto.getApproveTime());
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
        if (!resource.getAcceptStatus().equals(equipment.getAcceptStatus())) {
            equipment.setAcceptStatus(resource.getAcceptStatus());
        }
        List<EquipAcceptanceDetail> details = detailRepository.findByAcceptanceId(resource.getId());
        if (ValidationUtil.isEmpty(details)) {
            // 初始化设备验收明细信息
            initDetail(resource.getId());
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
        EquipAcceptance newAcceptance = acceptanceRepository.save(resource);
        // 初始化设备验收明细信息
        initDetail(newAcceptance.getId());
    }

    /**
     * 初始化设备验收明细信息
     *
     * @param acceptanceId 设备验收ID
     */
    private void initDetail(Long acceptanceId) {
        // 外观
        List<EquipAcceptanceDetail> appearanceList = new ArrayList<>();
        EquipAcceptanceDetail wg1 = new EquipAcceptanceDetail();
        wg1.setAcceptanceId(acceptanceId);
        wg1.setDetailCategory(CommonConstants.EQUIP_ACCEPTANCE_APPEARANCE);
        wg1.setDetailTitle("1. 设备外包装有无破损");
        wg1.setDetailSort(1);
        appearanceList.add(wg1);
        EquipAcceptanceDetail wg2 = new EquipAcceptanceDetail();
        wg2.setAcceptanceId(acceptanceId);
        wg2.setDetailCategory(CommonConstants.EQUIP_ACCEPTANCE_APPEARANCE);
        wg2.setDetailTitle("2. 开箱后设备本身情况有无生锈破损等其他情况");
        wg2.setDetailSort(2);
        appearanceList.add(wg2);
        detailRepository.saveAll(appearanceList);
        // todo 软件资料
        List<EquipAcceptanceDetail> rj1List = new ArrayList<>();
        EquipAcceptanceDetail rj1 = new EquipAcceptanceDetail();
        rj1.setAcceptanceId(acceptanceId);
        rj1.setDetailCategory(CommonConstants.EQUIP_ACCEPTANCE_SOFTWARE_INFO);
        rj1.setDetailTitle("1. 使用手册/维护手册/说明书");
        rj1.setDetailSort(3);
        rj1.setSubCount(10);
        EquipAcceptanceDetail rjzlFirst = detailRepository.save(rj1);
        EquipAcceptanceDetail rj1_1 = new EquipAcceptanceDetail();
        rj1_1.setAcceptanceId(acceptanceId);
        rj1_1.setPid(rjzlFirst.getId());
        rj1_1.setDetailCategory(CommonConstants.EQUIP_ACCEPTANCE_SOFTWARE_INFO);
        rj1_1.setDetailTitle("其中：装箱清单");
        rj1_1.setDetailSort(4);
        rj1List.add(rj1_1);
        EquipAcceptanceDetail rj1_2 = new EquipAcceptanceDetail();
        rj1_2.setAcceptanceId(acceptanceId);
        rj1_2.setPid(rjzlFirst.getId());
        rj1_2.setDetailCategory(CommonConstants.EQUIP_ACCEPTANCE_SOFTWARE_INFO);
        rj1_2.setDetailTitle("系统原理图");
        rj1_2.setDetailSort(5);
        rj1List.add(rj1_2);
        EquipAcceptanceDetail rj1_3 = new EquipAcceptanceDetail();
        rj1_3.setAcceptanceId(acceptanceId);
        rj1_3.setPid(rjzlFirst.getId());
        rj1_3.setDetailCategory(CommonConstants.EQUIP_ACCEPTANCE_SOFTWARE_INFO);
        rj1_3.setDetailTitle("控制系统原理图");
        rj1_3.setDetailSort(6);
        rj1List.add(rj1_3);
        EquipAcceptanceDetail rj1_4 = new EquipAcceptanceDetail();
        rj1_4.setAcceptanceId(acceptanceId);
        rj1_4.setPid(rjzlFirst.getId());
        rj1_4.setDetailCategory(CommonConstants.EQUIP_ACCEPTANCE_SOFTWARE_INFO);
        rj1_4.setDetailTitle("电器原理图");
        rj1_4.setDetailSort(7);
        rj1List.add(rj1_4);
        EquipAcceptanceDetail rj1_5 = new EquipAcceptanceDetail();
        rj1_5.setAcceptanceId(acceptanceId);
        rj1_5.setPid(rjzlFirst.getId());
        rj1_5.setDetailCategory(CommonConstants.EQUIP_ACCEPTANCE_SOFTWARE_INFO);
        rj1_5.setDetailTitle("液压系统原理图");
        rj1_5.setDetailSort(8);
        rj1List.add(rj1_5);
        EquipAcceptanceDetail rj1_6 = new EquipAcceptanceDetail();
        rj1_6.setAcceptanceId(acceptanceId);
        rj1_6.setPid(rjzlFirst.getId());
        rj1_6.setDetailCategory(CommonConstants.EQUIP_ACCEPTANCE_SOFTWARE_INFO);
        rj1_6.setDetailTitle("气动系统原理图");
        rj1_6.setDetailSort(9);
        rj1List.add(rj1_6);
        EquipAcceptanceDetail rj1_7 = new EquipAcceptanceDetail();
        rj1_7.setAcceptanceId(acceptanceId);
        rj1_7.setPid(rjzlFirst.getId());
        rj1_7.setDetailCategory(CommonConstants.EQUIP_ACCEPTANCE_SOFTWARE_INFO);
        rj1_7.setDetailTitle("PLC程序原理图、逻辑图");
        rj1_7.setDetailSort(10);
        rj1List.add(rj1_7);
        EquipAcceptanceDetail rj1_8 = new EquipAcceptanceDetail();
        rj1_8.setAcceptanceId(acceptanceId);
        rj1_8.setPid(rjzlFirst.getId());
        rj1_8.setDetailCategory(CommonConstants.EQUIP_ACCEPTANCE_SOFTWARE_INFO);
        rj1_8.setDetailTitle("设备外形尺寸图");
        rj1_8.setDetailSort(11);
        rj1List.add(rj1_8);
        EquipAcceptanceDetail rj1_9 = new EquipAcceptanceDetail();
        rj1_9.setAcceptanceId(acceptanceId);
        rj1_9.setPid(rjzlFirst.getId());
        rj1_9.setDetailCategory(CommonConstants.EQUIP_ACCEPTANCE_SOFTWARE_INFO);
        rj1_9.setDetailTitle("工装夹具接口尺寸图");
        rj1_9.setDetailSort(12);
        rj1List.add(rj1_9);
        EquipAcceptanceDetail rj1_10 = new EquipAcceptanceDetail();
        rj1_10.setAcceptanceId(acceptanceId);
        rj1_10.setPid(rjzlFirst.getId());
        rj1_10.setDetailCategory(CommonConstants.EQUIP_ACCEPTANCE_SOFTWARE_INFO);
        rj1_10.setDetailTitle("地基要求");
        rj1_10.setDetailSort(13);
        rj1List.add(rj1_10);
        detailRepository.saveAll(rj1List);
        List<EquipAcceptanceDetail> rjOtherList = new ArrayList<>();
        EquipAcceptanceDetail rj2 = new EquipAcceptanceDetail();
        rj2.setAcceptanceId(acceptanceId);
        rj2.setDetailCategory(CommonConstants.EQUIP_ACCEPTANCE_SOFTWARE_INFO);
        rj2.setDetailTitle("2. 计量/标定证书");
        rj2.setDetailSort(14);
        rjOtherList.add(rj2);
        EquipAcceptanceDetail rj3 = new EquipAcceptanceDetail();
        rj3.setAcceptanceId(acceptanceId);
        rj3.setDetailCategory(CommonConstants.EQUIP_ACCEPTANCE_SOFTWARE_INFO);
        rj3.setDetailTitle("3. 设备维修维护记录文件");
        rj3.setDetailSort(15);
        rjOtherList.add(rj3);
        EquipAcceptanceDetail rj4 = new EquipAcceptanceDetail();
        rj4.setAcceptanceId(acceptanceId);
        rj4.setDetailCategory(CommonConstants.EQUIP_ACCEPTANCE_SOFTWARE_INFO);
        rj4.setDetailTitle("4. 零部件更换记录文件");
        rj4.setDetailSort(16);
        rjOtherList.add(rj4);
        EquipAcceptanceDetail rj5 = new EquipAcceptanceDetail();
        rj5.setAcceptanceId(acceptanceId);
        rj5.setDetailCategory(CommonConstants.EQUIP_ACCEPTANCE_SOFTWARE_INFO);
        rj5.setDetailTitle("(其他技术文档)");
        rj5.setDetailSort(17);
        rjOtherList.add(rj5);
        detailRepository.saveAll(rjOtherList);
        // todo 运转测试
        List<EquipAcceptanceDetail> otList = new ArrayList<>();
        EquipAcceptanceDetail ot1 = new EquipAcceptanceDetail();
        ot1.setAcceptanceId(acceptanceId);
        ot1.setDetailCategory(CommonConstants.EQUIP_ACCEPTANCE_OPERATIONAL_TEST);
        ot1.setDetailTitle("CMK");
        ot1.setDetailSort(18);
        otList.add(ot1);
        EquipAcceptanceDetail ot2 = new EquipAcceptanceDetail();
        ot2.setAcceptanceId(acceptanceId);
        ot2.setDetailCategory(CommonConstants.EQUIP_ACCEPTANCE_OPERATIONAL_TEST);
        ot2.setDetailTitle("MSA");
        ot2.setDetailSort(19);
        otList.add(ot2);
        EquipAcceptanceDetail ot3 = new EquipAcceptanceDetail();
        ot3.setAcceptanceId(acceptanceId);
        ot3.setDetailCategory(CommonConstants.EQUIP_ACCEPTANCE_OPERATIONAL_TEST);
        ot3.setDetailTitle("UPH");
        ot3.setDetailSort(20);
        otList.add(ot3);
        EquipAcceptanceDetail ot4 = new EquipAcceptanceDetail();
        ot4.setAcceptanceId(acceptanceId);
        ot4.setDetailCategory(CommonConstants.EQUIP_ACCEPTANCE_OPERATIONAL_TEST);
        ot4.setDetailTitle("小批量试产合格率");
        ot4.setDetailSort(21);
        otList.add(ot4);
        EquipAcceptanceDetail ot5 = new EquipAcceptanceDetail();
        ot5.setAcceptanceId(acceptanceId);
        ot5.setDetailCategory(CommonConstants.EQUIP_ACCEPTANCE_OPERATIONAL_TEST);
        ot5.setDetailTitle("中批量试产合格率");
        ot5.setDetailSort(22);
        otList.add(ot5);
        EquipAcceptanceDetail ot6 = new EquipAcceptanceDetail();
        ot6.setAcceptanceId(acceptanceId);
        ot6.setDetailCategory(CommonConstants.EQUIP_ACCEPTANCE_OPERATIONAL_TEST);
        ot6.setDetailTitle("加工精度");
        ot6.setDetailSort(23);
        otList.add(ot6);
        EquipAcceptanceDetail ot7 = new EquipAcceptanceDetail();
        ot7.setAcceptanceId(acceptanceId);
        ot7.setDetailCategory(CommonConstants.EQUIP_ACCEPTANCE_OPERATIONAL_TEST);
        ot7.setDetailTitle("其它");
        ot7.setDetailSort(24);
        otList.add(ot7);
        detailRepository.saveAll(otList);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        acceptanceRepository.deleteAllByIdIn(ids);
        // 关联删除设备项目
        detailRepository.deleteByAcceptanceIdIn(ids);
        equipmentRepository.rollbackEquipStatus(ids);
    }

    @Override
    public EquipAcceptance findByEquipmentId(Long equipmentId) {
        return acceptanceRepository.findByEquipId(equipmentId);
    }
}
