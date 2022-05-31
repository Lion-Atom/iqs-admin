package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.CalibrationOrg;
import me.zhengjie.domain.InstruCalibration;
import me.zhengjie.domain.Instrument;
import me.zhengjie.repository.CaliOrgRepository;
import me.zhengjie.repository.InstruCalibrationFileV2Repository;
import me.zhengjie.repository.InstruCalibrationRepository;
import me.zhengjie.repository.InstrumentRepository;
import me.zhengjie.service.InstruCalibrationServiceV2;
import me.zhengjie.service.dto.InstruCalibrationDto;
import me.zhengjie.service.dto.InstruCalibrationQueryCriteria;
import me.zhengjie.service.mapstruct.InstruCalibrationMapper;
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
public class InstruCalibrationServiceImplV2 implements InstruCalibrationServiceV2 {

    private final InstruCalibrationRepository caliRepository;
    private final InstruCalibrationFileV2Repository fileRepository;
    private final InstrumentRepository instruRepository;
    private final InstruCalibrationMapper calibrationMapper;
    private final CaliOrgRepository caliOrgRepository;

    @Override
    public List<InstruCalibrationDto> queryAll(InstruCalibrationQueryCriteria criteria) {
        List<InstruCalibrationDto> list = new ArrayList<>();
        List<InstruCalibration> calibrationList = caliRepository.findAll(((root, query, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder)));
        if (ValidationUtil.isNotEmpty(calibrationList)) {
            list = calibrationMapper.toDto(calibrationList);
            Set<Long> instruIds = new HashSet<>();
            Map<Long, String> instruNameMap = new HashMap<>();
            Map<Long, String> instruNumMap = new HashMap<>();
            Set<Long> caliOrgIds = new HashSet<>();
            Map<Long, String> caliOrgMap = new HashMap<>();
            list.forEach(cali -> {
                instruIds.add(cali.getInstruId());
                if (cali.getCaliOrgId() != null) {
                    caliOrgIds.add(cali.getCaliOrgId());
                }
            });
            // 获取仪器信息
            if (!instruIds.isEmpty()) {
                List<Instrument> equipmentList = instruRepository.findByIdIn(instruIds);
                if (ValidationUtil.isNotEmpty(equipmentList)) {
                    equipmentList.forEach(dto -> {
                        instruNameMap.put(dto.getId(), dto.getInstruName());
                        instruNumMap.put(dto.getId(), dto.getInnerId());
                    });
                }
                // 获取设备基础信息
                list.forEach(dto -> {
                    dto.setInstruName(instruNameMap.get(dto.getInstruId()));
                    dto.setInnerId(instruNumMap.get(dto.getInstruId()));
                });
            }
            // 获取校准机构名称
            if (!caliOrgIds.isEmpty()) {
                List<CalibrationOrg> orgList = caliOrgRepository.findByIdsIn(caliOrgIds);
                if (ValidationUtil.isNotEmpty(orgList)) {
                    orgList.forEach(dto -> {
                        caliOrgMap.put(dto.getId(), dto.getCaliOrgName());
                    });
                }
            }
            // 获取校准机构信息
            list.forEach(dto -> {
                dto.setCaliOrgName(caliOrgMap.get(dto.getCaliOrgId()));
            });
        }
        return list;
    }

    @Override
    public void download(List<InstruCalibrationDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (InstruCalibrationDto dto : queryAll) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("仪器名称", dto.getInstruName());
            map.put("内部编码", dto.getInnerId());
            map.put("校准日期", ValidationUtil.transToDate(dto.getCaliDate()));
            if (dto.getInnerChecked()) {
                // 内部校准
                map.put("校准方式", "内部校准");
            } else {
                if (dto.getIsDoor()) {
                    map.put("校准方式", "外部上门校准," + "校准机构：" + dto.getCaliOrgName());
                } else {
                    map.put("校准方式", "送出校准," + "校准机构：" + dto.getCaliOrgName());
                }
            }
            if (dto.getCaliResult().equals("不合格")) {
                map.put("校准结果", dto.getCaliResult() + ",原因：" + dto.getFailDesc());
            } else {
                map.put("校准结果", dto.getCaliResult());
            }
            map.put("创建时间", dto.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public Map<String, Object> queryAll(InstruCalibrationQueryCriteria criteria, Pageable pageable) {
        return PageUtil.toPage(caliRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable));
    }

    @Override
    public InstruCalibration findById(Long id) {
        return caliRepository.findById(id).orElseGet(InstruCalibration::new);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(InstruCalibrationDto resource) {
        Instrument instrument = instruRepository.findById(resource.getInstruId()).orElseGet(Instrument::new);
        ValidationUtil.isNull(instrument.getId(), "Instrument", "id", resource.getInstruId());
        caliRepository.save(calibrationMapper.toEntity(resource));
        InstruCalibration max = caliRepository.findMaxByInstruId(instrument.getId());
        setInstruCalibrationInfo(instrument, max);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(InstruCalibrationDto resource) {
        Instrument instrument = instruRepository.findById(resource.getInstruId()).orElseGet(Instrument::new);
        ValidationUtil.isNull(instrument.getId(), "Instrument", "id", resource.getInstruId());
        // 设备保养信息
        InstruCalibration calibration = caliRepository.save(calibrationMapper.toEntity(resource));
        // 文件列表
        if (ValidationUtil.isNotEmpty(resource.getFileList())) {
            resource.getFileList().forEach(file -> {
                file.setCaliId(calibration.getId());
            });
            fileRepository.saveAll(resource.getFileList());
        }
        // 关联反馈到设备保养相关基础信息-上次保养日期和保养到期日期
        InstruCalibration max = caliRepository.findMaxByInstruId(instrument.getId());
        setInstruCalibrationInfo(instrument, max);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        List<Long> list = new ArrayList<>(ids);
        InstruCalibration calibration = caliRepository.findById(list.get(0)).orElseGet(InstruCalibration::new);
        ValidationUtil.isNull(calibration.getId(), "InstruCalibration", "id", list.get(0));
        Instrument instrument = instruRepository.findById(calibration.getInstruId()).orElseGet(Instrument::new);
        ValidationUtil.isNull(instrument.getId(), "Instrument", "id", calibration.getInstruId());
        caliRepository.deleteAllByIdIn(ids);
        InstruCalibration max = caliRepository.findMaxByInstruId(instrument.getId());
        setInstruCalibrationInfo(instrument, max);
    }

    private void setInstruCalibrationInfo(Instrument instrument, InstruCalibration max) {
        if (max != null) {
            instrument.setLastCaliDate(max.getCaliDate());
            // todo 根据上次设置到期日期
            long unit = (long) (24 * 3600 * 1000);
            switch (instrument.getPeriodUnit()) {
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
            long dueDate = instrument.getLastCaliDate().getTime() + unit * instrument.getCaliPeriod();
            instrument.setNextCaliDate(new Timestamp(dueDate));
            // 前一天的23:59:59
            long current = System.currentTimeMillis();//当前时间毫秒数
            long zero = current - (current + TimeZone.getDefault().getRawOffset()) % (1000 * 3600 * 24);
            int diff = (int) Math.ceil((double) (dueDate - zero) / (24 * 60 * 60 * 1000));
            // 校准过期时间与当前时间对比，若是小于当前时间则认定为过期未保养
            if (diff > 0) {
                if (instrument.getRemindDays() != null && diff <= instrument.getRemindDays()) {
                    instrument.setCaliStatus(CommonConstants.INSTRU_CALI_STATUS_SOON_OVERDUE);
                } else {
                    instrument.setCaliStatus(CommonConstants.INSTRU_CALI_STATUS_FINISHED);
                }
            } else {
                instrument.setCaliStatus(CommonConstants.INSTRU_CALI_STATUS_OVERDUE);
            }
            instruRepository.save(instrument);
        } else {
            instrument.setLastCaliDate(null);
            instrument.setNextCaliDate(null);
            instrument.setCaliStatus(CommonConstants.INSTRU_CALI_STATUS_WAIT);
            instruRepository.save(instrument);
        }
    }

    @Override
    public List<InstruCalibration> findByInstruId(Long instruId) {
        Instrument instrument = instruRepository.findById(instruId).orElseGet(Instrument::new);
        ValidationUtil.isNull(instrument.getId(), "Instrument", "id", instruId);
        return caliRepository.findByInstruId(instruId);
    }
}
