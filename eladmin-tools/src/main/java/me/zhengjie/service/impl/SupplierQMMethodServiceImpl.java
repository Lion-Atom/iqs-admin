package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.Supplier;
import me.zhengjie.domain.SupplierQMCertification;
import me.zhengjie.domain.SupplierQMMethod;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.repository.SupplierQMMethodRepository;
import me.zhengjie.repository.SupplierRepository;
import me.zhengjie.service.SupplierQMMethodService;
import me.zhengjie.service.dto.SupplierQMMethodReplaceDto;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/12/2 11:10
 */
@Service
@RequiredArgsConstructor
public class SupplierQMMethodServiceImpl implements SupplierQMMethodService {

    private final SupplierRepository supplierRepository;
    private final SupplierQMMethodRepository methodRepository;


    @Override
    public List<SupplierQMMethod> findBySupplierId(Long supplierId) {
//        Supplier supplier = supplierRepository.findById(supplierId).orElseGet(Supplier::new);
//        ValidationUtil.isNull(supplier.getId(), "Supplier", "id", supplierId);
        return methodRepository.findBySupplierId(supplierId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SupplierQMMethod create(SupplierQMMethod resource) {
        Long supplierId = resource.getSupplierId();
        String methodName = resource.getMethodName();
/*        Supplier supplier = supplierRepository.findById(supplierId).orElseGet(Supplier::new);
        ValidationUtil.isNull(supplier.getId(), "Supplier", "id", supplierId);*/
        // 重名校验
        SupplierQMMethod method = methodRepository.findByMethodNameAndSupplierId(supplierId, methodName);
        if (method != null) {
            throw new EntityExistException(SupplierQMMethod.class, "methodName", methodName);
        }
        return methodRepository.save(resource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<SupplierQMMethod> initMethod(Long uId) {
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SupplierQMMethod resource) {
        Long supplierId = resource.getSupplierId();
        String methodName = resource.getMethodName();
/*        Supplier supplier = supplierRepository.findById(supplierId).orElseGet(Supplier::new);
        ValidationUtil.isNull(supplier.getId(), "Supplier", "id", supplierId);*/
        // 重名校验
        SupplierQMMethod method = methodRepository.findByMethodNameAndSupplierId(supplierId, methodName);
        if (method != null && !method.getId().equals(resource.getId())) {
            throw new EntityExistException(SupplierQMMethod.class, "methodName", methodName);
        }
        methodRepository.save(resource);
    }

    @Override
    public void replaceMethod(SupplierQMMethodReplaceDto dto) {
//        Supplier supplier = supplierRepository.findById(dto.getSupplierId()).orElseGet(Supplier::new);
//        ValidationUtil.isNull(supplier.getId(), "Supplier", "id", dto.getSupplierId());
        List<SupplierQMMethod> methods = methodRepository.findBySupplierId(dto.getUId());
        if (ValidationUtil.isNotEmpty(methods)) {
            methods.forEach(cer -> {
                cer.setSupplierId(dto.getSupplierId());
            });
            methodRepository.saveAll(methods);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        methodRepository.deleteByIdIn(ids);
    }
}
