package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.Supplier;
import me.zhengjie.domain.SupplierContact;
import me.zhengjie.domain.SupplierCusAudit;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.repository.SupplierCusAuditRepository;
import me.zhengjie.repository.SupplierRepository;
import me.zhengjie.service.SupplierCusAuditService;
import me.zhengjie.service.dto.SupplierCusAuditReplaceDto;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/12/1 14:07
 */
@Service
@RequiredArgsConstructor
public class SupplierCusAuditServiceImpl implements SupplierCusAuditService {

    private final SupplierRepository supplierRepository;
    private final SupplierCusAuditRepository cusAuditRepository;


    @Override
    public List<SupplierCusAudit> findBySupplierId(Long supplierId) {
        return cusAuditRepository.findBySupplierId(supplierId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SupplierCusAudit create(SupplierCusAudit resources) {
        SupplierCusAudit audit = cusAuditRepository.findByCustomerNameAndSupplierId(resources.getCustomerName(), resources.getSupplierId());
        // 同一个供应商下的重名校验
        if (audit != null) {
            throw new EntityExistException(SupplierContact.class, "customerName", resources.getCustomerName());
        }
        return cusAuditRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SupplierCusAudit resources) {
        SupplierCusAudit audit = cusAuditRepository.findByCustomerNameAndSupplierId(resources.getCustomerName(), resources.getSupplierId());
        // 同一个供应商下的重名校验
        if (audit != null && !audit.getId().equals(resources.getId())) {
            throw new EntityExistException(SupplierContact.class, "customerName", resources.getCustomerName());
        }
        cusAuditRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceBindSupplierId(SupplierCusAuditReplaceDto dto) {
        Supplier supplier = supplierRepository.findById(dto.getSupplierId()).orElseGet(Supplier::new);
        ValidationUtil.isNull(supplier.getId(), "Supplier", "id", dto.getSupplierId());
        List<SupplierCusAudit> cusAudits = cusAuditRepository.findBySupplierId(dto.getUId());
        if (ValidationUtil.isNotEmpty(cusAudits)) {
            cusAudits.forEach(audit -> {
                audit.setSupplierId(dto.getSupplierId());
            });
            cusAuditRepository.saveAll(cusAudits);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        cusAuditRepository.deleteAllByIdIn(ids);
    }
}
