package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.Supplier;
import me.zhengjie.domain.SupplierQMCertification;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.repository.SupplierQMCertificationRepository;
import me.zhengjie.repository.SupplierRepository;
import me.zhengjie.service.SupplierQMCertificationService;
import me.zhengjie.service.dto.SupplierQMCerReplaceDto;
import me.zhengjie.service.dto.SupplierQMCertificationDto;
import me.zhengjie.service.mapstruct.SupplierQMCerMapper;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/11/30 10:31
 */
@Service
@RequiredArgsConstructor
public class SupplierQMCertificationServiceImpl implements SupplierQMCertificationService {

    private final SupplierRepository supplierRepository;
    private final SupplierQMCertificationRepository certificationRepository;
    private final SupplierQMCerMapper cerMapper;

    @Override
    public List<SupplierQMCertificationDto> findBySupplierId(Long supplierId) {
        List<SupplierQMCertificationDto> list = new ArrayList<>();
//        ValidationUtil.isNull(supplier.getId(), "Supplier", "id", supplierId);
        List<SupplierQMCertification> certifications = certificationRepository.findBySupplierId(supplierId);
        if (ValidationUtil.isNotEmpty(certifications)) {
            list = cerMapper.toDto(certifications);
            // 获取当前有效期样式:1.一个月-黄色 2.过期-红色 3.大于一个月，正常显示
            list.forEach(this::initStyleTypeByExpireDate);
        }
        return list;
    }

    private void initStyleTypeByExpireDate(SupplierQMCertificationDto dto) {
        if (dto.getExpireDate() != null) {
            // 粗略计算，误差一天内
            Timestamp validLine = dto.getExpireDate();
            Date now = new Date();
            if (validLine.getTime() > now.getTime()) {
                long diff = validLine.getTime() - now.getTime();
                int closeDuration = (int) (diff / (24 * 60 * 60 * 1000)) == 0 ? 1 : (int) (diff / (24 * 60 * 60 * 1000));
                if (closeDuration > CommonConstants.AUDIT_DAYS_MONTH) {
                    dto.setStyleType("primary");
                } else {
                    dto.setStyleType("warn");
                }
            } else {
                dto.setStyleType("alert");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SupplierQMCertification create(SupplierQMCertification resource) {
        Long supplierId = resource.getSupplierId();
        String itemName = resource.getItemName();
/*        Supplier supplier = supplierRepository.findById(supplierId).orElseGet(Supplier::new);
        ValidationUtil.isNull(supplier.getId(), "Supplier", "id", supplierId);*/
        // 重名校验
        SupplierQMCertification certification = certificationRepository.findByItemNameAndSupplierId(supplierId, itemName);
        if (certification != null) {
            throw new EntityExistException(SupplierQMCertification.class, "itemName", itemName);
        }
        return certificationRepository.save(resource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SupplierQMCertification resource) {
        Long supplierId = resource.getSupplierId();
        String itemName = resource.getItemName();
/*        Supplier supplier = supplierRepository.findById(supplierId).orElseGet(Supplier::new);
        ValidationUtil.isNull(supplier.getId(), "Supplier", "id", supplierId);*/
        // 重名校验
        SupplierQMCertification certification = certificationRepository.findByItemNameAndSupplierId(supplierId, itemName);
        if (certification != null && !certification.getId().equals(resource.getId())) {
            throw new EntityExistException(SupplierQMCertification.class, "itemName", itemName);
        }
        certificationRepository.save(resource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        certificationRepository.deleteByIdIn(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<SupplierQMCertification> initCertification(Long uId) {
        List<SupplierQMCertification> certifications = new ArrayList<>();
        SupplierQMCertification cer1 = new SupplierQMCertification();
        cer1.setSupplierId(uId);
        cer1.setItemName("ISO9001");
        certifications.add(cer1);
        SupplierQMCertification cer2 = new SupplierQMCertification();
        cer2.setItemName("IATF16949");
        cer2.setSupplierId(uId);
        certifications.add(cer2);
        SupplierQMCertification cer3 = new SupplierQMCertification();
        cer3.setItemName("ISO14001");
        cer3.setSupplierId(uId);
        certifications.add(cer3);
        SupplierQMCertification cer4 = new SupplierQMCertification();
        cer4.setItemName("ISO45001");
        cer4.setSupplierId(uId);
        certifications.add(cer4);
        SupplierQMCertification cer5 = new SupplierQMCertification();
        cer5.setItemName("VDA6.3/VDA6.5");
        cer5.setSupplierId(uId);
        certifications.add(cer5);
        return certificationRepository.saveAll(certifications);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceCer(SupplierQMCerReplaceDto dto) {
//        Supplier supplier = supplierRepository.findById(dto.getSupplierId()).orElseGet(Supplier::new);
//        ValidationUtil.isNull(supplier.getId(), "Supplier", "id", dto.getSupplierId());
        List<SupplierQMCertification> certifications = certificationRepository.findBySupplierId(dto.getUId());
        if (ValidationUtil.isNotEmpty(certifications)) {
            certifications.forEach(cer -> {
                cer.setSupplierId(dto.getSupplierId());
            });
            certificationRepository.saveAll(certifications);
        }
    }
}
