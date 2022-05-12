package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.FileDept;
import me.zhengjie.domain.TrainNewStaff;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.repository.FileDeptRepository;
import me.zhengjie.repository.TrNewStaffFileRepository;
import me.zhengjie.repository.TrainNewStaffRepository;
import me.zhengjie.service.TrainNewStaffService;
import me.zhengjie.service.dto.TrainNewStaffDto;
import me.zhengjie.service.dto.TrainNewStaffQueryCriteria;
import me.zhengjie.service.mapstruct.TrainNewStaffMapper;
import me.zhengjie.utils.FileUtil;
import me.zhengjie.utils.PageUtil;
import me.zhengjie.utils.QueryHelp;
import me.zhengjie.utils.ValidationUtil;
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
            list = staffMapper.toDto(staffs);
            list.forEach(staff -> {
                deptIds.add(staff.getDepartId());
            });
            initStaffDepartName(list, deptIds, deptMap);
        }
        return list;
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
            map.put("培训内容", dto.getTrainContent());
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
            Set<Long> deptIds = new HashSet<>();
            Map<Long, String> deptMap = new HashMap<>();
            list = staffMapper.toDto(page.getContent());
            list.forEach(staff -> {
                deptIds.add(staff.getDepartId());
            });
            initStaffDepartName(list, deptIds, deptMap);
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
        TrainNewStaff staff = staffRepository.findAllByStaffName(resource.getStaffName());
        if (staff != null && !staff.getId().equals(resource.getId())) {
            throw new EntityExistException(TrainNewStaff.class, "staffName", resource.getStaffName());
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
        TrainNewStaff staff = staffRepository.findAllByStaffName(resource.getStaffName());
        if (staff != null) {
            throw new EntityExistException(TrainNewStaff.class, "staffName", resource.getStaffName());
        }
       /* List<TrainNewStaff> staffList = staffRepository.findAllByDepartIdAndStaffName(resource.getDepartId(), resource.getStaffName());
        if (ValidationUtil.isNotEmpty(staffList)) {
            throw new EntityExistException(TrainNewStaff.class, "staffName", resource.getStaffName());
        }*/
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
        staffRepository.deleteAllByIdIn(ids);
        // 删除相关附件
        fileRepository.deleteByTrNewStaffIdIn(ids);
    }
}
