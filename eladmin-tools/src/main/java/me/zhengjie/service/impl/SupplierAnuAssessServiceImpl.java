package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.Supplier;
import me.zhengjie.domain.SupplierAnnualAssessment;
import me.zhengjie.domain.SupplierContact;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.repository.SupplierAnuAssessRepository;
import me.zhengjie.repository.SupplierRepository;
import me.zhengjie.service.SupplierAnuAssessService;
import me.zhengjie.service.dto.SupplierCusAuditReplaceDto;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/12/1 14:07
 */
@Service
@RequiredArgsConstructor
public class SupplierAnuAssessServiceImpl implements SupplierAnuAssessService {

    private final SupplierRepository supplierRepository;
    private final SupplierAnuAssessRepository assessRepository;


    @Override
    public List<SupplierAnnualAssessment> findBySupplierId(Long supplierId) {
        return assessRepository.findBySupplierId(supplierId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SupplierAnnualAssessment create(SupplierAnnualAssessment resources) {
        //获取4位年份数字
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
        //获取时间戳判断时间
        String time = dateFormat.format(resources.getAssessDate());
        resources.setAssessName(time+"年度评估");
        SupplierAnnualAssessment assess = assessRepository.findByAssessNameAndSupplierId(resources.getAssessName(), resources.getSupplierId());
        // 同一个供应商下的重名校验
        if (assess != null) {
            throw new EntityExistException(SupplierContact.class, "assessName", resources.getAssessName());
        }
        return assessRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SupplierAnnualAssessment resources) {
        //获取4位年份数字
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
        //获取时间戳判断时间
        String time = dateFormat.format(resources.getAssessDate());
        resources.setAssessName(time+"年度评估");
        SupplierAnnualAssessment assess = assessRepository.findByAssessNameAndSupplierId(resources.getAssessName(), resources.getSupplierId());
        // 同一个供应商下的重名校验
        if (assess != null && !assess.getId().equals(resources.getId())) {
            throw new EntityExistException(SupplierContact.class, "assessName", resources.getAssessName());
        }
        assessRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceBindSupplierId(SupplierCusAuditReplaceDto dto) {
        Supplier supplier = supplierRepository.findById(dto.getSupplierId()).orElseGet(Supplier::new);
        ValidationUtil.isNull(supplier.getId(), "Supplier", "id", dto.getSupplierId());
        List<SupplierAnnualAssessment> cusAudits = assessRepository.findBySupplierId(dto.getUId());
        if (ValidationUtil.isNotEmpty(cusAudits)) {
            cusAudits.forEach(audit -> {
                audit.setSupplierId(dto.getSupplierId());
            });
            assessRepository.saveAll(cusAudits);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        assessRepository.deleteAllByIdIn(ids);
    }
}
