package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.FileDept;
import me.zhengjie.domain.TrainExamDepart;
import me.zhengjie.domain.TrainMaterialDepart;
import me.zhengjie.domain.TrainMaterialFile;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.FileDeptRepository;
import me.zhengjie.repository.TrainExamDepartRepository;

import me.zhengjie.repository.TrainMaterialDepartRepository;
import me.zhengjie.repository.TrainMaterialFileRepository;
import me.zhengjie.service.TrainMaterialDepartService;
import me.zhengjie.service.TrainMaterialFileService;
import me.zhengjie.service.dto.*;
import me.zhengjie.service.mapstruct.TrMaterialDepartMapper;
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
public class TrainMaterialDepartServiceImpl implements TrainMaterialDepartService {

    private final TrainMaterialDepartRepository materialDepartRepository;
    private final TrainMaterialFileRepository materialFileRepository;
    private final TrMaterialDepartMapper materialDepartMapper;
    private final FileDeptRepository deptRepository;
    private final TrainMaterialFileService materialFileService;

    @Override
    public List<TrainMaterialDepartDto> queryAll(TrainExamDepartQueryCriteria criteria) {
        List<TrainMaterialDepartDto> list = new ArrayList<>();
        List<TrainMaterialDepart> materialDeparts = materialDepartRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
        if (ValidationUtil.isNotEmpty(materialDeparts)) {
            Set<Long> deptIds = new HashSet<>();
            Map<Long, String> deptMap = new HashMap<>();
            Map<Long, List<TrainMaterialFileDto>> fileMap = new HashMap<>();
            list = materialDepartMapper.toDto(materialDeparts);
            list.forEach(materialDepart -> {
                deptIds.add(materialDepart.getDepartId());
            });
            if (!deptIds.isEmpty()) {
                initExamDepartName(list, deptIds, deptMap);
                deptIds.forEach(deptId -> {
                    TrainMaterialFileQueryCriteria materialFileQueryCriteria = new TrainMaterialFileQueryCriteria();
                    materialFileQueryCriteria.setDepartId(deptId);
                    List<TrainMaterialFileDto> trainMaterialFileDtoList = materialFileService.queryAll(materialFileQueryCriteria);
                    fileMap.put(deptId, trainMaterialFileDtoList);
                });
                list.forEach(materialDepart -> {
                    materialDepart.setMaterialFileList(fileMap.get(materialDepart.getDepartId()));
                });
            }

        }
        return list;
    }

    private void initExamDepartName(List<TrainMaterialDepartDto> list, Set<Long> deptIds, Map<Long, String> deptMap) {

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
    public void update(TrainMaterialDepart resource) {
        // todo 校验是否具备权限
        checkEditAuthorized(resource);
        // 若更改为禁用则需要判断是否存在内容，若存在内容则不允许禁用
        if (!resource.getEnabled()) {
            List<TrainMaterialFile> materialFiles = materialFileRepository.findByDepartId(resource.getDepartId());
            if (ValidationUtil.isNotEmpty(materialFiles)) {
                throw new BadRequestException("No Valid!抱歉，该部门下存在有效材料信息，不可禁用！");
            }
        }
        materialDepartRepository.save(resource);
    }

    private void checkEditAuthorized(TrainMaterialDepart resource) {
        Boolean isAdmin = SecurityUtils.isAdmin();
        String username = SecurityUtils.getCurrentUsername();
        if (!resource.getCreateBy().equals(username) && !isAdmin) {
            // 非创建者亦非管理员则无权限修改和删除
            throw new BadRequestException("No Access!抱歉，您暂无权更改此项！");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(TrainMaterialDepart resource) {
        TrainMaterialDepart examDepart = materialDepartRepository.findByDepartId(resource.getDepartId());
        FileDept dept = deptRepository.findById(resource.getDepartId()).orElseGet(FileDept::new);
        if (examDepart != null) {
            throw new BadRequestException("【" + dept.getName() + "】已存在，请勿重新添加");
        }
        materialDepartRepository.save(resource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        // todo 校验是否具备权限
//        checkEditAuthorized(resource);
        materialDepartRepository.deleteAllByIdIn(ids);
    }
}
