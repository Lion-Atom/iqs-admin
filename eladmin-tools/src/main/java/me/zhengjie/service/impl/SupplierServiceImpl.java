package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.Supplier;
import me.zhengjie.domain.SupplierContact;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.repository.*;
import me.zhengjie.service.SupplierService;
import me.zhengjie.service.dto.SupplierContactDto;
import me.zhengjie.service.dto.SupplierDto;
import me.zhengjie.service.dto.SupplierQueryCriteria;
import me.zhengjie.service.mapstruct.SupplierContactMapper;
import me.zhengjie.service.mapstruct.SupplierMapper;
import me.zhengjie.utils.*;
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
 * @date 2021/11/22 11:52
 */
@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;
    private final SupplierContactRepository contactRepository;
    private final SupplierContactMapper contactMapper;
    private final SupplierFileRepository fileRepository;
    private final SupplierCusAuditRepository cusAuditRepository;
    private final SupplierQMCertificationRepository certificationRepository;
    private final SupplierQMMethodRepository methodRepository;

    @Override
    public SupplierDto findById(Long supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId).orElseGet(Supplier::new);
        ValidationUtil.isNull(supplier.getId(), "Auditor", "id", supplierId);
        // 或需要处理批准人信息、审核人员的部门等信息等--目前不需要
        SupplierDto dto = supplierMapper.toDto(supplier);
        if (dto != null) {
            if (dto.getLogistics() != null) {
                dto.setLogisticsTags(dto.getLogistics().split(","));
            }
            if (dto.getProduction() != null) {
                dto.setProdTags(dto.getProduction().split(","));
            }
            // 获取其下联系人列表
            List<SupplierContact> list = contactRepository.findBySupplierId(supplierId);
            dto.setContacts(list);
        }
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Supplier create(Supplier resources) {
        //重名校验
        Supplier supplier = supplierRepository.findByName(resources.getName());
        if (supplier != null) {
            throw new EntityExistException(Supplier.class, "name", resources.getName());
        }
        // 自动生成供应商编号
        Integer count = supplierRepository.findTodayCountByCreateTime();
        resources.setSupplierCode(StringUtils.getTCode(resources.getName(), count));
        return supplierRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Supplier resources) {
        //重名校验
        Supplier supplier = supplierRepository.findByName(resources.getName());
        if (supplier != null && !supplier.getId().equals(resources.getId())) {
            throw new EntityExistException(Supplier.class, "name", resources.getName());
        }
        supplierRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        supplierRepository.deleteAllByIdIn(ids);
        // 删除联系人信息
        contactRepository.deleteBySupplierIdIn(ids);
        // 删除附件信息
        fileRepository.deleteBySupplierIdIn(ids);
        // 删除客户审核认证信息
        certificationRepository.deleteBySupplierIdIn(ids);
        // 删除客户审核信息
        cusAuditRepository.deleteBySupplierIdIn(ids);
        // 删除审核管理方法
        methodRepository.deleteBySupplierIdIn(ids);
    }

    @Override
    public Map<String, Object> queryAll(SupplierQueryCriteria criteria, Pageable pageable) {
        Page<Supplier> page = supplierRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        return PageUtil.toPage(page);
    }

    @Override
    public List<SupplierDto> queryAll(SupplierQueryCriteria criteria) {
        List<Supplier> suppliers = supplierRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
        List<SupplierDto> list = new ArrayList<>();
        if (ValidationUtil.isNotEmpty(suppliers)) {
            // todo 联系人信息、审核信息等
            list = supplierMapper.toDto(suppliers);
            // todo 获取最近一次年度审核得分： assessScore
        }
        return list;
    }

    @Override
    public void download(List<SupplierDto> supplierDtos, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (SupplierDto dto : supplierDtos) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("供应商名称", dto.getName());
            map.put("供应商编号", dto.getSupplierCode());
            map.put("产品", dto.getProduction());
            map.put("供应商类别", dto.getType());
            map.put("供应商级别", dto.getLevel());
            map.put("通讯地址", dto.getAddress());
            map.put("成立时间", dto.getFoundationDate());
            map.put("运送方式", dto.getDeliveryMethod());
            map.put("物流公司", dto.getLogistics());
            // todo 审核信息
            map.put("创建日期", dto.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public void verification(Set<Long> ids) {

    }
}
