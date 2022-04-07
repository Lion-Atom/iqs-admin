package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.Change;
import me.zhengjie.domain.ChangeApprove;
import me.zhengjie.domain.ChangeFactor;
import me.zhengjie.domain.ChangeManagement;
import me.zhengjie.repository.*;
import me.zhengjie.service.ChangeService;
import me.zhengjie.service.dto.ChangeQueryCriteria;
import me.zhengjie.service.dto.ChangeDto;
import me.zhengjie.service.mapstruct.ChangeMapper;
import me.zhengjie.utils.FileUtil;
import me.zhengjie.utils.QueryHelp;
import me.zhengjie.utils.StringUtils;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author tmj
 * @version 1.0
 * @date 2022/1/17 11:26
 */
@Service
@RequiredArgsConstructor
public class ChangeServiceImpl implements ChangeService {

    private final ChangeMapper changeMapper;
    private final ChangeRepository changeRepository;
    private final ChangeFactorRepository factorRepository;
    private final ChangeManagementRepository managementRepository;
    private final ChangeApproveRepository approveRepository;
    private final ChangeFileRepository fileRepository;

    @Override
    public List<ChangeDto> queryAll(ChangeQueryCriteria criteria) {
        List<Change> changes = changeRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
        // todo 添加后续拓展项目
        return changeMapper.toDto(changes);
    }

    @Override
    public Map<String, Object> queryAll(ChangeQueryCriteria criteria, Pageable pageable) {
        Page<Change> page = changeRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        Map<String, Object> map = new HashMap<>();
        List<ChangeDto> list = new ArrayList<>();
        long total = 0L;
        if (ValidationUtil.isNotEmpty(page.getContent())) {
            list = changeMapper.toDto(page.getContent());
            list.forEach(dto -> {
                // todo 添加其他子项目
            });
            total = page.getTotalElements();
        }
        map.put("content", list);
        map.put("totalElements", total);
        return map;
    }

    @Override
    public void download(List<ChangeDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (ChangeDto dto : queryAll) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("变更编码", dto.getChangeNum());
            map.put("变更原因", dto.getReason());
            map.put("变更来源", dto.getSource());
            map.put("发起人", dto.getInitiator());
            map.put("发起部门", dto.getDepartment());
            map.put("发起时间", dto.getInitTime());
            map.put("涉及地域", dto.getArea());
            map.put("涉及部门", dto.getDepart());
            map.put("涉及产品", dto.getProduction());
            map.put("费用评估", dto.getCost());
            map.put("是否客户要求", dto.getIsCustomer() ? "是" : "否");
            map.put("审批部门", dto.getApproveDepart());
            map.put("审批人", dto.getApproveBy());
            map.put("变更进度", dto.getStatus());
            if (dto.getIsAccepted() != null) {
                map.put("是否同意变更", dto.getIsAccepted() ? "是" : "否");
            } else {
                map.put("是否同意变更", null);
            }
            map.put("创建日期", dto.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public ChangeDto findById(Long changeId) {
        Change change = changeRepository.findById(changeId).orElseGet(Change::new);
        ValidationUtil.isNull(change.getId(), "Change", "id", changeId);
        ChangeDto dto = changeMapper.toDto(change);
        dtoAdditional(dto);
        List<Integer> unlockStepList = new ArrayList<>();
        unlockStepList.add(1);
        if (dto.getIsAccepted() && dto.getFinishedStep() == 4) {
            unlockStepList.add(2);
            // 后续管理 add(3)
            ChangeManagement management = managementRepository.findByChangeId(changeId);
            if (management != null) {
                dto.setManagement(management);
                if (management.getFinishedStep() == 2) {
                    // 解锁3.变更通过/关闭
                    unlockStepList.add(3);
                }
            }
            // 通过/关闭
            ChangeApprove approve = approveRepository.findByChangeId(changeId);
            if (approve != null) {
                dto.setApprove(approve);
            }
        }
        dto.setUnlockSteps(unlockStepList);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChangeDto create(Change resources) {
        // 如果是最后一步，判断是否需要生成当前的变更编码
        resources.setStatus("起草中");
        Change change = changeRepository.save(resources);
        ChangeDto dto = changeMapper.toDto(change);
        // 数据转化
        dtoAdditional(dto);
        // 初始化五大影响因素数据：人机料法环
        initFactors(dto);
        return dto;
    }

    private void initFactors(ChangeDto dto) {
        // 初始化五大影响因素数据：人机料法环
        List<ChangeFactor> factors = new ArrayList<>();
        // 人员
        ChangeFactor man = new ChangeFactor();
        man.setChangeId(dto.getId());
        man.setName("人员");
        man.setType(CommonConstants.CHANGE_FACTOR_TYPE_MAJOR);
        factors.add(man);
        // 机器
        ChangeFactor machine = new ChangeFactor();
        machine.setChangeId(dto.getId());
        machine.setName("机器");
        machine.setType(CommonConstants.CHANGE_FACTOR_TYPE_MAJOR);
        factors.add(machine);
        // 物料
        ChangeFactor material = new ChangeFactor();
        material.setChangeId(dto.getId());
        material.setName("物料");
        material.setType(CommonConstants.CHANGE_FACTOR_TYPE_MAJOR);
        factors.add(material);
        // 方法
        ChangeFactor method = new ChangeFactor();
        method.setChangeId(dto.getId());
        method.setName("方法");
        method.setType(CommonConstants.CHANGE_FACTOR_TYPE_MAJOR);
        factors.add(method);
        // 环境
        ChangeFactor environment = new ChangeFactor();
        environment.setChangeId(dto.getId());
        environment.setName("环境");
        environment.setType(CommonConstants.CHANGE_FACTOR_TYPE_MAJOR);
        factors.add(environment);
        factorRepository.saveAll(factors);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> update(Change resources) {
        Set<Long> changeIds = new HashSet<>();
        Map<String, Object> map = new HashMap<>();
        List<Integer> list = new ArrayList<>();
        list.add(1);
        Change change = changeRepository.findById(resources.getId()).orElseGet(Change::new);
        ValidationUtil.isNull(change.getId(), "Change", "id", resources.getId());
        changeIds.add(change.getId());
        if (resources.getFinishedStep() == 4) {
            // 生成变更编码
            if (resources.getIsAccepted()) {
                if (resources.getChangeNum() == null) {
                    // 自动生成供应商编号
                    Integer count = changeRepository.findTodayCountByCreateTime();
                    resources.setChangeNum(StringUtils.getTCode("变更", count));
                }
                // 解锁2.变更管理
                list.add(2);
                resources.setStatus("待管理");
                // 原则上，进入管理流程后没有再修改draft权限和途径，此处为便于后续的拓展和修改，暂不注释
                ChangeManagement management = managementRepository.findByChangeId(resources.getId());
                if (management != null) {
                    resources.setStatus("管理中");
                    if (management.getFinishedStep() == 2) {
                        // 解锁3.变更通过/关闭
                        resources.setStatus("待批准");
                        list.add(3);
                    }
                    ChangeApprove approve = approveRepository.findByChangeId(resources.getId());
                    if (approve != null) {
                        resources.setStatus("批准中");
                        if (approve.getFinishedStep() == 2) {
                            resources.setStatus("已批准");
                        }
                    }
                }
            } else {
                resources.setStatus("被拒绝");
                // 删除已有的管理和批准/关闭的数据-manage和附件信息,备注：实际应用中无须此操作，因为进入管理流程后是无法修改起草信息的
                resources.setChangeNum(null);
                managementRepository.deleteByChangeIdIn(changeIds);
                approveRepository.deleteByChangeIdIn(changeIds);
            }
        }
        changeRepository.save(resources);
        map.put("steps", list);
        map.put("num", resources.getChangeNum());
        return map;
    }

    private void dtoAdditional(ChangeDto dto) {
        if (dto != null) {
            // 获取涉及地域
            if (dto.getArea() != null) {
                dto.setAreaTags(dto.getArea().split(","));
            }
            // 获取涉及部门
            if (dto.getDepart() != null) {
                dto.setDepartTags(dto.getDepart().split(","));
            }
            // 获取涉及项目
            if (dto.getProject() != null) {
                dto.setProjectTags(dto.getProject().split(","));
            }
            // 获取涉及产品
            if (dto.getProduction() != null) {
                dto.setProdTags(dto.getProduction().split(","));
            }
            // 获取变更范围
            if (dto.getScope() != null) {
                dto.setScopeTags(Arrays.asList(dto.getScope().split(",")));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        // 删除变更信息
        changeRepository.deleteAllByIdIn(ids);
        // 删除影响因素
        factorRepository.deleteByChangeIdIn(ids);
        // 删除变更管理信息
        managementRepository.deleteByChangeIdIn(ids);
        // 删除变更批准/关闭信息
        approveRepository.deleteByChangeIdIn(ids);
        // 删除子项目以及其下所有附件信息
        fileRepository.deleteByChangeIdIn(ids);
    }
}
