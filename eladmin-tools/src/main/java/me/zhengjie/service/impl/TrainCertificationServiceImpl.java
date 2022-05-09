package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.FileDept;
import me.zhengjie.domain.TrainCertification;
import me.zhengjie.domain.TrainCertification;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.repository.*;
import me.zhengjie.service.TrainCertificationService;
import me.zhengjie.service.TrainCertificationService;
import me.zhengjie.service.dto.TrainCertificationDto;
import me.zhengjie.service.dto.TrainCertificationQueryCriteria;
import me.zhengjie.service.dto.TrainCertificationDto;
import me.zhengjie.service.dto.TrainCertificationQueryCriteria;
import me.zhengjie.service.mapstruct.TrainCertificationMapper;
import me.zhengjie.service.mapstruct.TrainCertificationMapper;
import me.zhengjie.utils.FileUtil;
import me.zhengjie.utils.PageUtil;
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
public class TrainCertificationServiceImpl implements TrainCertificationService {

    private final TrainCertificationRepository certificationRepository;
    private final TrCertificationFileRepository fileRepository;
    private final FileDeptRepository deptRepository;
    private final TrainCertificationMapper certificationMapper;

    @Override
    public List<TrainCertificationDto> queryAll(TrainCertificationQueryCriteria criteria) {
        List<TrainCertificationDto> list = new ArrayList<>();
        List<TrainCertification> certifications = certificationRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
        if (ValidationUtil.isNotEmpty(certifications)) {
            Set<Long> deptIds = new HashSet<>();
            Map<Long, String> deptMap = new HashMap<>();
            list = certificationMapper.toDto(certifications);
            list.forEach(staff -> {
                deptIds.add(staff.getDepartId());
            });
            initCerDepartName(list, deptIds, deptMap);
        }
        return list;
    }

    private void initCerDepartName(List<TrainCertificationDto> list, Set<Long> deptIds, Map<Long, String> deptMap) {
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
    public void download(List<TrainCertificationDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (TrainCertificationDto dto : queryAll) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("员工姓名", dto.getStaffName());
            map.put("部门", dto.getDepartName());
            map.put("上级主管", dto.getSuperior());
            map.put("入职日期", ValidationUtil.transToDate(dto.getHireDate()));
            map.put("工号", dto.getJobNum());
            map.put("岗位", dto.getJobName());
            map.put("证明种类", dto.getCertificationType());
            map.put("工种种类", dto.getJobType());
            map.put("培训内容", dto.getTrainContent());
            map.put("创建日期", dto.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public Map<String, Object> queryAll(TrainCertificationQueryCriteria criteria, Pageable pageable) {
        Page<TrainCertification> page = certificationRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        Map<String, Object> map = new HashMap<>();
        List<TrainCertificationDto> cerList = new ArrayList<>();
        long total = 0L;
        if (ValidationUtil.isNotEmpty(page.getContent())) {
            Set<Long> deptIds = new HashSet<>();
            Map<Long, String> deptMap = new HashMap<>();
            cerList = certificationMapper.toDto(page.getContent());
            cerList.forEach(staff -> {
                deptIds.add(staff.getDepartId());
            });
            initCerDepartName(cerList, deptIds, deptMap);
            total = page.getTotalElements();
        }
        map.put("content", cerList);
        map.put("totalElements", total);
        return map;
    }

    @Override
    public TrainCertificationDto findById(Long id) {
        TrainCertification staff = certificationRepository.findById(id).orElseGet(TrainCertification::new);
        ValidationUtil.isNull(staff.getId(), "TrainCertification", "id", id);
        TrainCertificationDto dto = certificationMapper.toDto(staff);
        if (dto.getDepartId() != null) {
            FileDept dept = deptRepository.findById(dto.getDepartId()).orElseGet(FileDept::new);
            ValidationUtil.isNull(dept.getId(), "FileDept", "id", dto.getDepartId());
            dto.setDepartName(dept.getName());
        }
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(TrainCertification resource) {
        TrainCertification entity = certificationRepository.findById(resource.getId()).orElseGet(TrainCertification::new);
        ValidationUtil.isNull(entity.getId(), "TrainCertification", "id", resource.getId());
        TrainCertification staff = certificationRepository.findAllByStaffName(resource.getStaffName());
        if (staff != null && !staff.getId().equals(resource.getId())) {
            throw new EntityExistException(TrainCertification.class, "staffName", resource.getStaffName());
        }
        if (entity.getIsRemind() == null || !entity.getIsRemind()) {
            resource.setRemindDays(null);
        }
        certificationRepository.save(resource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(TrainCertificationDto resource) {
        TrainCertification trainCertification = certificationRepository.findAllByStaffName(resource.getStaffName());
        if (trainCertification != null) {
            throw new EntityExistException(TrainCertification.class, "staffName", resource.getStaffName());
        }
        if (resource.getIsRemind() == null || !resource.getIsRemind()) {
            resource.setRemindDays(null);
        }
        TrainCertification certification = certificationRepository.save(certificationMapper.toEntity(resource));
        // 文件列表
        if (ValidationUtil.isNotEmpty(resource.getFileList())) {
            resource.getFileList().forEach(file -> {
                file.setTrCertificationId(certification.getId());
            });
            fileRepository.saveAll(resource.getFileList());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        certificationRepository.deleteAllByIdIn(ids);
        // 删除相关附件
        fileRepository.deleteByTrNewStaffIdIn(ids);
    }
}