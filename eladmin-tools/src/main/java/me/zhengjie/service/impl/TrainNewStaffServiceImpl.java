package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.FileDept;
import me.zhengjie.domain.TrainNewStaff;
import me.zhengjie.domain.TrainSchedule;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.exception.EntityIDExistException;
import me.zhengjie.repository.FileDeptRepository;
import me.zhengjie.repository.TrNewStaffFileRepository;
import me.zhengjie.repository.TrainNewStaffRepository;
import me.zhengjie.repository.TrainScheduleRepository;
import me.zhengjie.service.TrainNewStaffService;
import me.zhengjie.service.dto.TrainNewStaffDto;
import me.zhengjie.service.dto.TrainNewStaffQueryCriteria;
import me.zhengjie.service.mapstruct.TrainNewStaffMapper;
import me.zhengjie.utils.*;
import org.apache.poi.hssf.record.ObjRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TrainNewStaffServiceImpl implements TrainNewStaffService {

    private final TrainScheduleRepository trScheduleRepository;
    private final TrainNewStaffRepository staffRepository;
    private final TrNewStaffFileRepository fileRepository;
    private final FileDeptRepository deptRepository;
    private final TrainNewStaffMapper staffMapper;

    @Override
    public List<TrainNewStaffDto> queryAll(TrainNewStaffQueryCriteria criteria) {
        List<TrainNewStaffDto> list = new ArrayList<>();
        List<TrainNewStaff> staffs = staffRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
        if (ValidationUtil.isNotEmpty(staffs)) {
            Set<Long> deptIds = new HashSet<>();
            Map<Long, String> deptMap = new HashMap<>();
            Set<Long> scheduleIds = new HashSet<>();
            Map<Long, TrainSchedule> scheduleMap = new HashMap<>();
            list = staffMapper.toDto(staffs);
            list.forEach(staff -> {
                deptIds.add(staff.getDepartId());
                scheduleIds.add(staff.getTrScheduleId());
            });
            initStaffDepartName(list, deptIds, deptMap);
            initStaffScheduleInfo(list, scheduleIds, scheduleMap);
        }
        return list;
    }

    private void initStaffScheduleInfo(List<TrainNewStaffDto> list, Set<Long> scheduleIds, Map<Long, TrainSchedule> scheduleMap) {
        if (!scheduleIds.isEmpty()) {
            List<TrainSchedule> sList = trScheduleRepository.findByIdIn(scheduleIds);
            sList.forEach(schedule -> {
                scheduleMap.put(schedule.getId(), schedule);
            });
            if (ValidationUtil.isNotEmpty(sList)) {
                list.forEach(dto -> {
                    dto.setTrainTitle(scheduleMap.get(dto.getTrScheduleId()).getTrainTitle());
                    dto.setTrainTime(scheduleMap.get(dto.getTrScheduleId()).getTrainTime());
                    dto.setScheduleStatus(scheduleMap.get(dto.getTrScheduleId()).getScheduleStatus());
                    dto.setIsExam(scheduleMap.get(dto.getTrScheduleId()).getIsExam());
                });
            }
        }
    }

    private void initStaffDepartName(List<TrainNewStaffDto> list, Set<Long> deptIds, Map<Long, String> deptMap) {
        if (!deptIds.isEmpty()) {
            List<FileDept> deptList = deptRepository.findByIdIn(deptIds);
            deptList.forEach(dept -> {
                deptMap.put(dept.getId(), dept.getName());
            });
            if (ValidationUtil.isNotEmpty(deptList)) {
                list.forEach(dto -> {
                    dto.setDepartName(deptMap.get(dto.getDepartId()));
                });
            }
        }
    }

    @Override
    public void download(List<TrainNewStaffDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (TrainNewStaffDto dto : queryAll) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("员工姓名", dto.getStaffName());
            map.put("部门", dto.getDepartName());
            map.put("上级主管", dto.getSuperior());
            map.put("入职日期", ValidationUtil.transToDate(dto.getHireDate()));
            map.put("工号", dto.getJobNum());
            map.put("岗位", dto.getJobName());
            map.put("车间", dto.getWorkshop());
            map.put("班组", dto.getTeam());
            map.put("培训项目", dto.getTrainTitle());
            map.put("是否完成培训", dto.getIsFinished() ? "是" : "否");
            map.put("未完成原因", dto.getReason());
            map.put("创建日期", dto.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public Map<String, Object> queryAll(TrainNewStaffQueryCriteria criteria, Pageable pageable) {
        Map<String, Object> map = new HashMap<>();
        Page<TrainNewStaff> page = staffRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        List<TrainNewStaffDto> list = new ArrayList<>();
        long total = 0L;
        if (ValidationUtil.isNotEmpty(page.getContent())) {
            Boolean isAdmin = SecurityUtils.isAdmin();
            String username = SecurityUtils.getCurrentUsername();
            Set<Long> deptIds = new HashSet<>();
            Map<Long, String> deptMap = new HashMap<>();
            Set<Long> scheduleIds = new HashSet<>();
            Map<Long, TrainSchedule> scheduleMap = new HashMap<>();
            list = staffMapper.toDto(page.getContent());
            list.forEach(staff -> {
                if (staff.getCreateBy().equals(username) || staff.getStaffName().equals(username) || isAdmin) {
                    staff.setHasEditAuthorized(true);
                } else {
                    staff.setHasEditAuthorized(false);
                }
                deptIds.add(staff.getDepartId());
                scheduleIds.add(staff.getTrScheduleId());
            });
            initStaffDepartName(list, deptIds, deptMap);
            initStaffScheduleInfo(list, scheduleIds, scheduleMap);
            total = page.getTotalElements();
        }
        map.put("content", list);
        map.put("totalElements", total);
        return map;
    }

    @Override
    public TrainNewStaffDto findById(Long id) {
        TrainNewStaff staff = staffRepository.findById(id).orElseGet(TrainNewStaff::new);
        ValidationUtil.isNull(staff.getId(), "TrainNewStaff", "id", id);
        TrainNewStaffDto dto = staffMapper.toDto(staff);
        if (dto.getDepartId() != null) {
            FileDept dept = deptRepository.findById(dto.getDepartId()).orElseGet(FileDept::new);
            ValidationUtil.isNull(dept.getId(), "FileDept", "id", dto.getDepartId());
            dto.setDepartName(dept.getName());
        }
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(TrainNewStaff resource) {
        TrainNewStaff entity = staffRepository.findById(resource.getId()).orElseGet(TrainNewStaff::new);
        ValidationUtil.isNull(entity.getId(), "TrainNewStaff", "id", resource.getId());
        TrainNewStaff staff = staffRepository.findAllByDepartIdAndTrScheduleIdAndStaffName(resource.getDepartId(), resource.getTrScheduleId(), resource.getStaffName());
        if (staff != null && !staff.getId().equals(resource.getId())) {
            throw new EntityIDExistException(TrainNewStaff.class, "trScheduleId", resource.getTrScheduleId().toString());
        }
        if (entity.getIsFinished()) {
            resource.setReason(null);
        } else {
            fileRepository.deleteByTrNewStaffId(resource.getId());
        }
        staffRepository.save(resource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(TrainNewStaffDto resource) {
        TrainNewStaff staff = staffRepository.findAllByDepartIdAndTrScheduleIdAndStaffName(resource.getDepartId(), resource.getTrScheduleId(), resource.getStaffName());
        if (staff != null) {
            throw new EntityIDExistException(TrainNewStaff.class, "trScheduleId", resource.getTrScheduleId().toString());
        }
        if (resource.getIsFinished()) {
            resource.setReason(null);
        }
        TrainNewStaff trNewStaff = staffRepository.save(staffMapper.toEntity(resource));
        // 文件列表
        if (ValidationUtil.isNotEmpty(resource.getFileList())) {
            resource.getFileList().forEach(file -> {
                file.setTrNewStaffId(trNewStaff.getId());
            });
            fileRepository.saveAll(resource.getFileList());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        // 员工信息删改权限控制
        Boolean isAdmin = SecurityUtils.isAdmin();
        String username = SecurityUtils.getCurrentUsername();
        if (!isAdmin) {
            ids.forEach(id -> {
                TrainNewStaff staff = staffRepository.findById(id).orElseGet(TrainNewStaff::new);
                ValidationUtil.isNull(staff.getId(), "TrainNewStaff", "id", id);
                if (!staff.getCreateBy().equals(username) && !staff.getStaffName().equals(username)) {
                    // 非创建者亦非管理员则无权限修改和删除
                    throw new BadRequestException("No Access!抱歉，您暂无权更改此项！");
                }
            });
        }
        // 员工信息
        staffRepository.deleteAllByIdIn(ids);
        // 删除相关附件
        fileRepository.deleteByTrNewStaffIdIn(ids);
    }
}
