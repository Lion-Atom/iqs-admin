package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.CalibrationFile;
import me.zhengjie.domain.InstruCali;
import me.zhengjie.domain.ToolsLog;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.exception.EntityIDExistException;
import me.zhengjie.repository.InstruCaliFileRepository;
import me.zhengjie.repository.InstruCaliRepository;
import me.zhengjie.repository.ToolsLogRepository;
import me.zhengjie.service.InstruCaliService;
import me.zhengjie.service.dto.InstruCaliDto;
import me.zhengjie.service.dto.InstruCaliQueryCriteria;
import me.zhengjie.service.mapstruct.InstruCaliMapper;
import me.zhengjie.utils.*;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author tmj
 * @version 1.0
 * @date 2022/3/14 13:52
 */
@Service
@RequiredArgsConstructor
public class InstruCaliServiceImpl implements InstruCaliService {

    private final InstruCaliMapper caliMapper;
    private final InstruCaliRepository caliRepository;
    private final InstruCaliFileRepository fileRepository;
    private final ToolsLogRepository toolsLogRepository;

    @Override
    public List<InstruCali> queryAll(InstruCaliQueryCriteria criteria) {
        return caliRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
    }

    @Override
    public void download(List<InstruCali> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (InstruCali dto : queryAll) {
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
            map.put("上一次校准日期", ValidationUtil.transToDate(dto.getLastCaliDate()));
            map.put("校准周期", dto.getCaliPeriod()+dto.getPeriodUnit());
            map.put("下一次校准日期", ValidationUtil.transToDate(dto.getNextCaliDate()));
            map.put("内部校准", dto.getInnerChecked() ? "是" : "否");
            if (!dto.getInnerChecked()) {
                map.put("外部校准", dto.getIsDoor() ? "上门校准" : "送出校准");
            } else {
                map.put("外部校准", "--");
            }
            map.put("创建日期", dto.getCreateTime());
            map.put("是否报废", dto.getIsDroped() ? "是" : "否");
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public Map<String, Object> queryAll(InstruCaliQueryCriteria criteria, Pageable pageable) {
        Page<InstruCali> page = caliRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        Map<String, Object> map = new HashMap<>();
        List<InstruCaliDto> list = new ArrayList<>();
        long total = 0L;
        // 加入最新上次校准报告信息
        if (ValidationUtil.isNotEmpty(page.getContent())) {
            list = caliMapper.toDto(page.getContent());
            Set<Long> caliIds = new HashSet<>();
            Map<Long, List<CalibrationFile>> fileMap = new HashMap<>();
            list.forEach(cali -> {
                caliIds.add(cali.getId());
            });
            List<CalibrationFile> files = fileRepository.findByCaliIdInAndIsLatest(caliIds, true);
            if (ValidationUtil.isNotEmpty(files)) {
                files.forEach(file -> {
                    if (fileMap.containsKey(file.getCalibrationId())) {
                        fileMap.get(file.getCalibrationId()).add(file);
                    } else {
                        List<CalibrationFile> fileList = new ArrayList<>();
                        fileList.add(file);
                        fileMap.put(file.getCalibrationId(), fileList);
                    }
                });
            }
            if (!fileMap.isEmpty()) {
                list.forEach(dto -> {
                    // 防止【上次校准报告】暂未上传，为空
                    if (ValidationUtil.isNotEmpty(fileMap.get(dto.getId()))) {
                        dto.setFileList(fileMap.get(dto.getId()));
                    }
                });
            }
            total = page.getTotalElements();
        }
        map.put("content", list);
        map.put("totalElements", total);
        return map;
    }

    @Override
    public InstruCaliDto findById(Long id) {
        InstruCali cali = caliRepository.findById(id).orElseGet(InstruCali::new);
        ValidationUtil.isNull(InstruCali.class, "InstruCali", "id", id);
        InstruCaliDto dto = caliMapper.toDto(cali);
        List<CalibrationFile> files = fileRepository.findByCaliId(id);
        dto.setFileList(files);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(InstruCali resource) {
        InstruCali old = caliRepository.findById(resource.getId()).orElseGet(InstruCali::new);
        ValidationUtil.isNull(InstruCali.class, "InstruCali", "id", resource.getId());
        // 检测变化--暂注释
        String str = compareObj(old, resource);
        if (ValidationUtil.isBlank(str)) {
//            throw new BadRequestException("No Change Found!未检测到变化！无须重复提交！");
        }
        InstruCali calibration = caliRepository.findByInnerID(resource.getInnerId());
        // 内部ID是唯一性校验
        if (calibration != null && !calibration.getId().equals(old.getId())) {
//            throw new EntityIDExistException(InstruCali.class, "innerId", resource.getInnerId());
            throw new BadRequestException("该内部ID已存在，请核实后填入！");
        }
        if (resource.getStatus().equals(CommonConstants.INSTRU_CALI_STATUS_UPLOAD)) {
            fileRepository.updateToOld(resource.getId());
        }
        // 资产号唯一性校验
        if (resource.getAssetNum() != null) {
            InstruCali cali = caliRepository.findByAssetNum(resource.getAssetNum());
            if (cali != null && !cali.getId().equals(old.getId())) {
                throw new BadRequestException("该资产号已存在，请核实后填入！");
            }
        }
        InstruCali cali = caliRepository.save(resource);
        // todo 监控校准报告变化
        // 监控上次校准报告
        // 以往校准报告信息
        // todo 生成日志记录
        if (ValidationUtil.isBlank(str)) {
//            throw new BadRequestException("No Change Found!未检测到变化！无须重复提交！");
            str="未检测到值变化";
        }
        ToolsLog log = new ToolsLog();
        log.setBindingId(cali.getId());
        log.setLogType(CommonConstants.LOG_TYPE_INSTRUMENT_CALIBRATION);
        log.setUsername(getUserName());
        log.setDescription("修改仪器校准信息");
        log.setDescriptionDetail(str);
        toolsLogRepository.save(log);
    }

    private String compareObj(Object oldBean, Object newBean) {
        // 字段转义
        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put("instruName", "仪器名称");
        fieldMap.put("instruNum", "出厂型号");
        fieldMap.put("assetNum", "资产号");
        fieldMap.put("purDate", "出厂日期");
        fieldMap.put("innerId", "内部ID");
        fieldMap.put("caliPeriod", "校准周期");
        fieldMap.put("periodUnit", "时间单位");
        fieldMap.put("lastCaliDate", "上次校准日期");
        fieldMap.put("nextCaliDate", "下次校准日期");
        fieldMap.put("innerChecked", "内部校准");
        fieldMap.put("isDoor", "外部校准选择");
        fieldMap.put("caliScope", "测量范围");
        fieldMap.put("precise", "精度要求");
        fieldMap.put("errorRange", "误差范围");
        fieldMap.put("useArea", "使用区域");
        fieldMap.put("useBy", "使用人");
        fieldMap.put("isDroped", "是否报废");
        fieldMap.put("dropRemark", "废弃说明");
        fieldMap.put("isRemind", "下次校准提醒");
        fieldMap.put("remindDays", "提前提醒天数");
        fieldMap.put("status", "仪校状态");
        StringBuilder str = new StringBuilder();
        //if (oldBean instanceof SysConfServer && newBean instanceof SysConfServer) {
        InstruCali pojo1 = (InstruCali) oldBean;
        InstruCali pojo2 = (InstruCali) newBean;
        try {
            Class clazz = pojo1.getClass();
            Field[] fields = pojo1.getClass().getDeclaredFields();
            int i = 1;
            for (Field field : fields) {
                if ("serialVersionUID".equals(field.getName())) {
                    continue;
                }
                if ("status".equals(field.getName())) {
                    continue;
                }
                PropertyDescriptor pd = new PropertyDescriptor(field.getName(), clazz);
                Method getMethod = pd.getReadMethod();
                Object o1 = getMethod.invoke(pojo1);
                Object o2 = getMethod.invoke(pojo2);
                if (o1 == null && o2 == null) {
                    continue;
                }
                if (!ValidationUtil.isEquals(o1, o2)) {
                    if (i != 1) {
                        str.append(";\n");
                    }
                    // 是否内部校准、是否废弃、是否需要下次校准提醒转义
                    if (field.getName().equals("innerChecked") || field.getName().equals("isDroped")
                            || field.getName().equals("isRemind")) {
                        o1 = (o1 != null && o1.toString().equals("true")) ? "是" : "否";
                        o2 = o2.toString().equals("true") ? "是" : "否";
                    }
                    // 外部校准选择转义
                    if (field.getName().equals("isDoor")) {
                        o1 = (o1 != null && o1.toString().equals("true")) ? "上门校准" : "送出校准";
                        o2 = o2.toString().equals("true") ? "上门校准" : "送出校准";
                    }
                    // 上次校准日期喝下次校准日期转义
                    if (field.getName().equals("lastCaliDate") || field.getName().equals("nextCaliDate") || field.getName().equals("purDate")) {
                        // 截取年月日：yyyy-MM-dd(0,10)
                        o1 = o1 != null ? o1.toString().substring(0, 10) : null;
                        o2 = o2.toString().substring(0, 10);
                    }
                    str.append(i).append("、字段名称【").append(fieldMap.get(field.getName())).append("】,旧值:").append(o1).append(",新值:").append(o2);
                    i++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // }
        return str.toString();
    }

    private String getUserName() {
        try {
            return SecurityUtils.getCurrentUsername();
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(InstruCaliDto resource) {
        // 内部ID是唯一性校验
        InstruCali calibration = caliRepository.findByInnerID(resource.getInnerId());
        if (calibration != null) {
//            throw new EntityIDExistException(InstruCali.class, "innerId", resource.getInnerId());
            throw new BadRequestException("该内部ID已存在，请核实后填入！");
        }
        if (resource.getAssetNum() != null) {
            InstruCali cali = caliRepository.findByAssetNum(resource.getAssetNum());
            if (cali != null) {
                throw new BadRequestException("该资产号已存在，请核实后填入！");
            }
        }
        resource.setStatus(CommonConstants.INSTRU_CALI_STATUS_FINISHED);
        InstruCali cali = caliRepository.save(caliMapper.toEntity(resource));
        if (resource.getUid() != null) {
            // 文件绑定转移到正式校准机构ID上
            List<CalibrationFile> files = fileRepository.findByCaliId(resource.getUid());
            if (ValidationUtil.isNotEmpty(files)) {
                files.forEach(file -> {
                    file.setCalibrationId(cali.getId());
                });
                fileRepository.saveAll(files);
            }
        }
        ToolsLog log = new ToolsLog();
        log.setBindingId(cali.getId());
        log.setLogType(CommonConstants.LOG_TYPE_INSTRUMENT_CALIBRATION);
        log.setUsername(getUserName());
        log.setDescription("新增仪器校准信息");
        log.setDescriptionDetail("新增" + resource.getInstruName() + "仪器校准信息");
        toolsLogRepository.save(log);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        caliRepository.deleteAllByIdIn(ids);
        fileRepository.deleteByCaliIdIn(ids);
    }
}
