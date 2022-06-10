package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.FileDept;
import me.zhengjie.domain.TrExamDepartFile;
import me.zhengjie.domain.TrainExamDepart;
import me.zhengjie.domain.TrainExamStaff;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.repository.FileDeptRepository;
import me.zhengjie.repository.TrExamDepartFileRepository;
import me.zhengjie.repository.TrainExamDepartRepository;
import me.zhengjie.repository.TrainExamStaffRepository;
import me.zhengjie.service.TrainExamDepartService;
import me.zhengjie.service.TrainExamStaffService;
import me.zhengjie.service.dto.TrExamStaffDto;
import me.zhengjie.service.dto.TrainExamDepartDto;
import me.zhengjie.service.dto.TrainExamDepartQueryCriteria;
import me.zhengjie.service.dto.TrainExamStaffQueryCriteria;
import me.zhengjie.service.mapstruct.TrExamDepartMapper;
import me.zhengjie.utils.QueryHelp;
import me.zhengjie.utils.SecurityUtils;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author TongMin Jie
 * @version V1
 * @date 2022/5/9 10:57
 */
@Service
@RequiredArgsConstructor
public class TrainExamDepartServiceImpl implements TrainExamDepartService {

    private final TrainExamDepartRepository examDepartRepository;
    private final TrExamDepartMapper examDepartMapper;
    private final FileDeptRepository deptRepository;
    private final TrExamDepartFileRepository fileRepository;
    private final TrainExamStaffRepository staffRepository;
    private final TrainExamStaffService examStaffService;

    @Override
    public List<TrainExamDepartDto> queryAll(TrainExamDepartQueryCriteria criteria) {
        List<TrainExamDepartDto> list = new ArrayList<>();
        List<TrainExamDepart> examDeparts = examDepartRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
        if (ValidationUtil.isNotEmpty(examDeparts)) {
            Set<Long> deptIds = new HashSet<>();
            Map<Long, String> deptMap = new HashMap<>();
            list = examDepartMapper.toDto(examDeparts);
            // todo 批量查询考试信息
            Map<Long, List<TrExamStaffDto>> examStaffMap = new HashMap<>();
            // todo 批量查询题库信息
            Map<Long, List<TrExamDepartFile>> fileMap = new HashMap<>();
            list.forEach(examDept -> {
                deptIds.add(examDept.getDepartId());
            });
            if (!deptIds.isEmpty()) {
                initExamDepartName(list, deptIds, deptMap);
                // 查询考试情况
                deptIds.forEach(deptId -> {
                    TrainExamStaffQueryCriteria examStaffQueryCriteria = new TrainExamStaffQueryCriteria();
                    examStaffQueryCriteria.setDepartId(deptId);
                    List<TrExamStaffDto> examStaffs = examStaffService.queryAll(examStaffQueryCriteria);
                    examStaffMap.put(deptId, examStaffs);
                });
                list.forEach(examDept -> {
                    examDept.setExamStaffList(examStaffMap.get(examDept.getDepartId()));
                });
            }
        }
        return list;
    }

    private void initExamDepartName(List<TrainExamDepartDto> list, Set<Long> deptIds, Map<Long, String> deptMap) {
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(TrainExamDepart resource) {
        // 校验是否具备权限
        checkEditAuthorized(resource);
        // 若更改为禁用则需要判断是否存在内容，若存在内容则不允许禁用
        if (!resource.getEnabled()) {
            List<TrainExamStaff> examStaffs = staffRepository.findAllByDepartId(resource.getDepartId());
            List<TrExamDepartFile> examDepartFiles = fileRepository.findByDepartId(resource.getDepartId());
            if (ValidationUtil.isNotEmpty(examDepartFiles) || ValidationUtil.isNotEmpty(examStaffs)) {
                throw new BadRequestException("No Valid!抱歉，该部门下存在有效信息，不可禁用！");
            }
        }
        examDepartRepository.save(resource);
    }

    private void checkEditAuthorized(TrainExamDepart resource) {
        Boolean isAdmin = SecurityUtils.isAdmin();
        String username = SecurityUtils.getCurrentUsername();
        if (!resource.getCreateBy().equals(username) && !isAdmin) {
            // 非创建者亦非管理员则无权限修改和删除
            throw new BadRequestException("No Access!抱歉，您暂无权更改此项！");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(TrainExamDepart resource) {
        TrainExamDepart examDepart = examDepartRepository.findByDepartId(resource.getDepartId());
        FileDept dept = deptRepository.findById(resource.getDepartId()).orElseGet(FileDept::new);
        if (examDepart != null) {
            throw new BadRequestException("【" + dept.getName() + "】已存在，请勿重新添加");
        }
        examDepartRepository.save(resource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        // todo 校验是否具备权限
//        checkEditAuthorized(resource);
        examDepartRepository.deleteAllByIdIn(ids);
    }
}
