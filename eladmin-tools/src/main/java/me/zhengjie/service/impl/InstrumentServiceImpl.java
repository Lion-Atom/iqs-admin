package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.CalibrationOrg;
import me.zhengjie.domain.InstruCali;
import me.zhengjie.domain.InstruCalibration;
import me.zhengjie.domain.Instrument;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.CaliOrgRepository;
import me.zhengjie.repository.InstruCalibrationRepository;
import me.zhengjie.repository.InstruFileRepository;
import me.zhengjie.repository.InstrumentRepository;
import me.zhengjie.service.InstrumentService;
import me.zhengjie.service.dto.InstrumentDto;
import me.zhengjie.service.dto.InstrumentQueryCriteria;
import me.zhengjie.service.mapstruct.InstrumentMapper;
import me.zhengjie.utils.FileUtil;
import me.zhengjie.utils.QueryHelp;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

/**
 * @author TongMin Jie
 * @version V1
 * @date 2022/5/27 13:59
 */
@Service
@RequiredArgsConstructor
public class InstrumentServiceImpl implements InstrumentService {

    private final InstrumentRepository instruRepository;
    private final InstruFileRepository instruFileRepository;
    private final InstrumentMapper instruMapper;
    private final CaliOrgRepository orgRepository;
    private final InstruCalibrationRepository caliRepository;

    @Override
    public List<InstrumentDto> queryAll(InstrumentQueryCriteria criteria) {
        List<InstrumentDto> list = new ArrayList<>();
        List<Instrument> instrumentList = instruRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
        if (ValidationUtil.isNotEmpty(instrumentList)) {
            list = instruMapper.toDto(instrumentList);
            Map<Long, String> orgMap = new HashMap<>();
            Set<Long> orgIds = new HashSet<>();
            initCaliOrgName(list, orgMap, orgIds);
        }
        return list;
    }

    @Override
    public void download(List<InstrumentDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (InstrumentDto dto : queryAll) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("仪器名称", dto.getInstruName());
            map.put("出厂型号", dto.getInstruNum());
            map.put("采购日期", dto.getPurDate());
            map.put("内部ID", dto.getInnerId());
            map.put("测量范围", dto.getCaliScope());
            map.put("精度要求", dto.getPrecise());
            map.put("允许误差", dto.getErrorRange());
            map.put("使用区域", dto.getUseArea());
            map.put("使用人", dto.getUseBy());
            map.put("存放位置", dto.getPosition());
            map.put("保管人", dto.getKeeper());
            map.put("上一次校准日期", ValidationUtil.transToDate(dto.getLastCaliDate()));
            map.put("校准周期", dto.getCaliPeriod() + dto.getPeriodUnit());
            map.put("下一次校准日期", ValidationUtil.transToDate(dto.getNextCaliDate()));
            map.put("内部校准", dto.getInnerChecked() ? "是" : "否");
            if (!dto.getInnerChecked()) {
                map.put("外部校准", dto.getIsDoor() ? "上门校准，校准机构：" + dto.getCaliOrgName() : "送出校准，校准机构：" + dto.getCaliOrgName());
            } else {
                map.put("外部校准", "---");
            }
            map.put("创建日期", dto.getCreateTime());
            map.put("仪器状态", dto.getStatus());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public Map<String, Object> queryByPage(InstrumentQueryCriteria criteria, Pageable pageable) {
        Map<String, Object> map = new HashMap<>();
        Page<Instrument> page = instruRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        List<InstrumentDto> list = new ArrayList<>();
        long total = 0L;
        if (ValidationUtil.isNotEmpty(page.getContent())) {
            list = instruMapper.toDto(page.getContent());
            Map<Long, String> orgMap = new HashMap<>();
            Set<Long> orgIds = new HashSet<>();
            initCaliOrgName(list, orgMap, orgIds);
            total = page.getTotalElements();
        }
        map.put("content", list);
        map.put("totalElements", total);
        return map;
    }

    private void initCaliOrgName(List<InstrumentDto> list, Map<Long, String> orgMap, Set<Long> orgIds) {
        list.forEach(dto -> {
            if (dto.getCaliOrgId() != null) {
                orgIds.add(dto.getCaliOrgId());
            }
        });
        List<CalibrationOrg> orgs = orgRepository.findByIdsIn(orgIds);
        if (ValidationUtil.isNotEmpty(orgs)) {
            orgs.forEach(org -> {
                if (!orgMap.containsKey(org.getId())) {
                    orgMap.put(org.getId(), org.getCaliOrgName());
                }
            });
        }
        list.forEach(dto -> {
            if (dto.getCaliOrgId() != null) {
                dto.setCaliOrgName(orgMap.get(dto.getCaliOrgId()));
            }
        });
    }

    @Override
    public Instrument findById(Long id) {
        Instrument instru = instruRepository.findById(id).orElseGet(Instrument::new);
        ValidationUtil.isNull(InstruCali.class, "Instrument", "id", instru.getId());
        return instru;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(InstrumentDto resource) {
        // 内部ID是唯一性校验
        Instrument instru = instruRepository.findByInnerID(resource.getInnerId());
        if (instru != null) {
//            throw new EntityIDExistException(InstruCali.class, "innerId", resource.getInnerId());
            throw new BadRequestException("该内部ID已存在，请核实后填入！");
        }
        if (resource.getAssetNum() != null) {
            Instrument other = instruRepository.findByAssetNum(resource.getAssetNum());
            if (other != null) {
                throw new BadRequestException("该资产号已存在，请核实后填入！");
            }
        }

        if (resource.getStatus().equals(CommonConstants.INSTRUMENT_STATUS_DROP)) {
            // 报废处理
            resource.setIsRemind(false);
            resource.setRemindDays(null);
            resource.setNextCaliDate(null);
            resource.setCaliStatus(CommonConstants.INSTRU_CALI_STATUS_DROP);
            Instrument instrument = instruRepository.save(instruMapper.toEntity(resource));
            if (ValidationUtil.isNotEmpty(resource.getFileList())) {
                resource.getFileList().forEach(file -> {
                    file.setInstruId(instrument.getId());
                });
                instruFileRepository.saveAll(resource.getFileList());
            }
        } else {
            resource.setCaliStatus(CommonConstants.INSTRU_CALI_STATUS_WAIT);
            instruRepository.save(instruMapper.toEntity(resource));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Instrument resource) {
        // 内部ID是唯一性校验
        Instrument instru = instruRepository.findByInnerID(resource.getInnerId());
        if (instru != null && !resource.getId().equals(instru.getId())) {
            throw new BadRequestException("该内部ID已存在，请核实后填入！");
        }
        if (resource.getAssetNum() != null) {
            Instrument other = instruRepository.findByAssetNum(resource.getAssetNum());
            if (other != null && !resource.getId().equals(other.getId())) {
                throw new BadRequestException("该资产号已存在，请核实后填入！");
            }
        }
        InstruCalibration max = caliRepository.findMaxByInstruId(resource.getId());
        if (resource.getStatus().equals(CommonConstants.INSTRUMENT_STATUS_DROP)) {
            resource.setCaliStatus(CommonConstants.INSTRU_CALI_STATUS_DROP);
        } else if (max != null) {
            long unit = (long) (24 * 3600 * 1000);
            switch (resource.getPeriodUnit()) {
                case CommonConstants.PERIOD_UNIT_YEAR:
                    // 年度
                    unit = 360 * unit;
                    break;
                case CommonConstants.PERIOD_UNIT_QUARTER:
                    // 季度-3个月
                    unit = 90 * unit;
                    break;
                case CommonConstants.PERIOD_UNIT_MONTH:
                    // 月
                    unit = 30 * unit;
                    break;
                case CommonConstants.PERIOD_UNIT_WEEK:
                    // 周
                    unit = 7 * unit;
                    break;
            }
            resource.setLastCaliDate(max.getCaliDate());
            long dueDate = resource.getLastCaliDate().getTime() + unit * resource.getCaliPeriod();
            resource.setNextCaliDate(new Timestamp(dueDate));
            long current = System.currentTimeMillis();//当前时间毫秒数
            // 今日零点前一毫秒
            long zero = current - (current + TimeZone.getDefault().getRawOffset()) % (1000 * 3600 * 24);
            int diff = (int) Math.ceil((double) (dueDate - zero) / (24 * 60 * 60 * 1000));
            // 校准过期时间与当前时间对比，若是小于当前时间则认定为过期未保养
            if (diff > 0) {
                if (resource.getRemindDays() != null && diff <= resource.getRemindDays()) {
                    resource.setCaliStatus(CommonConstants.INSTRU_CALI_STATUS_SOON_OVERDUE);
                } else {
                    resource.setCaliStatus(CommonConstants.INSTRU_CALI_STATUS_FINISHED);
                }
            } else {
                resource.setCaliStatus(CommonConstants.INSTRU_CALI_STATUS_OVERDUE);
            }
        }
        instruRepository.save(resource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        instruRepository.deleteAllByIdIn(ids);
        // 删除相关附件
        instruRepository.deleteAllByInstruIdIn(ids);
        // todo 删除相关校准信息
    }
}
