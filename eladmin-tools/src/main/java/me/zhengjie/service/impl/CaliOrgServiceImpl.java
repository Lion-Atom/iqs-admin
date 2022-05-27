package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.CaliOrgFile;
import me.zhengjie.domain.CalibrationOrg;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.repository.CaliOrgFileRepository;
import me.zhengjie.repository.CaliOrgRepository;
import me.zhengjie.service.CaliOrgService;
import me.zhengjie.service.dto.CaliOrgQueryByExample;
import me.zhengjie.service.dto.CaliOrgQueryCriteria;
import me.zhengjie.service.dto.CalibrationOrgDto;
import me.zhengjie.service.mapstruct.CaliOrgMapper;
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

/**
 * @author tmj
 * @version 1.0
 * @date 2022/3/11 11:44
 */
@Service
@RequiredArgsConstructor
public class CaliOrgServiceImpl implements CaliOrgService {

    private final CaliOrgRepository caliOrgRepository;
    private final CaliOrgMapper caliOrgMapper;
    private final CaliOrgFileRepository fileRepository;

    @Override
    public List<CalibrationOrg> queryAll(CaliOrgQueryCriteria criteria) {
        return caliOrgRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
    }

    @Override
    public Map<String, Object> queryAll(CaliOrgQueryCriteria criteria, Pageable pageable) {
        Page<CalibrationOrg> page = caliOrgRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        return PageUtil.toPage(page);
    }

    @Override
    public void download(List<CalibrationOrg> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (CalibrationOrg dto : queryAll) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("校验机构", dto.getCaliOrgName());
            map.put("传真", dto.getFax());
            map.put("移动电话", dto.getPhone());
            map.put("电子邮箱", dto.getEmail());
            map.put("通讯地址", dto.getAddress());
            map.put("业务范畴", dto.getCaliScope());
            map.put("创建日期", dto.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public CalibrationOrgDto findById(Long caliOrgId) {
        CalibrationOrg org = caliOrgRepository.findById(caliOrgId).orElseGet(CalibrationOrg::new);
        ValidationUtil.isNull(org.getId(), "CalibrationOrg", "id", caliOrgId);
        return caliOrgMapper.toDto(org);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(CalibrationOrgDto resources) {

        CalibrationOrg caliOrg = caliOrgRepository.findByName(resources.getCaliOrgName());
        if (caliOrg != null) {
            throw new EntityExistException(CalibrationOrg.class, "name", resources.getCaliOrgName());
        }
        CalibrationOrg org = caliOrgRepository.save(caliOrgMapper.toEntity(resources));
        if (resources.getUid() != null) {
            // 文件绑定转移到正式校准机构ID上
            List<CaliOrgFile> files = fileRepository.findByCaliOrgId(resources.getUid());
            if (ValidationUtil.isNotEmpty(files)) {
                files.forEach(file -> {
                    file.setCaliOrgId(org.getId());
                });
                fileRepository.saveAll(files);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(CalibrationOrg resources) {
        CalibrationOrg caliOrg = caliOrgRepository.findByName(resources.getCaliOrgName());
        if (caliOrg != null && !caliOrg.getId().equals(resources.getId())) {
            throw new EntityExistException(CalibrationOrg.class, "name", resources.getCaliOrgName());
        }
        caliOrgRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        // 删除校准机构信息
        caliOrgRepository.deleteAllByIdIn(ids);
        // 删除校准机构相关文件
        fileRepository.deleteByCaliOrgIdIn(ids);
    }

    @Override
    public List<CalibrationOrg> queryByExample(CaliOrgQueryByExample queryByExample) {
        return caliOrgRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, queryByExample, criteriaBuilder));
    }
}
