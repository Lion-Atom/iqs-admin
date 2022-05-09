package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.FileDept;
import me.zhengjie.domain.TrainExamDepart;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.repository.FileDeptRepository;
import me.zhengjie.repository.TrainExamDepartRepository;
import me.zhengjie.service.TrainExamDepartService;
import me.zhengjie.service.dto.TrainExamDepartDto;
import me.zhengjie.service.dto.TrainExamDepartQueryCriteria;
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

    @Override
    public List<TrainExamDepartDto> queryAll(TrainExamDepartQueryCriteria criteria) {
        List<TrainExamDepartDto> list = new ArrayList<>();
        List<TrainExamDepart> examDeparts = examDepartRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
        if (ValidationUtil.isNotEmpty(examDeparts)) {
            Set<Long> deptIds = new HashSet<>();
            Map<Long, String> deptMap = new HashMap<>();
            list = examDepartMapper.toDto(examDeparts);
            list.forEach(staff -> {
                deptIds.add(staff.getDepartId());
            });
            initExamDepartName(list, deptIds, deptMap);
        }
        return list;
    }

    private void initExamDepartName(List<TrainExamDepartDto> list, Set<Long> deptIds, Map<Long, String> deptMap) {
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
    @Transactional(rollbackFor = Exception.class)
    public void update(TrainExamDepart resource) {
        // todo 校验是否具备权限
        checkEditAuthorized(resource);
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