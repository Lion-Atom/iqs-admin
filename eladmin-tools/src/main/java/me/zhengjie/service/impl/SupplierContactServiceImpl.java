package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.base.CommonDTO;
import me.zhengjie.config.FileProperties;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.Supplier;
import me.zhengjie.domain.SupplierContact;
import me.zhengjie.domain.TimeManagement;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.repository.SupplierContactRepository;
import me.zhengjie.repository.SupplierRepository;
import me.zhengjie.service.SupplierContactService;
import me.zhengjie.service.SupplierService;
import me.zhengjie.service.dto.SupplierContactDto;
import me.zhengjie.service.dto.SupplierContactQueryCriteria;
import me.zhengjie.service.dto.SupplierDto;
import me.zhengjie.service.dto.SupplierQueryCriteria;
import me.zhengjie.service.mapstruct.SupplierContactMapper;
import me.zhengjie.service.mapstruct.SupplierMapper;
import me.zhengjie.utils.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/11/22 11:52
 */
@Service
@RequiredArgsConstructor
public class SupplierContactServiceImpl implements SupplierContactService {

    private final SupplierRepository supplierRepository;
    private final SupplierContactRepository contactRepository;
    private final SupplierContactMapper contactMapper;
    private final FileProperties properties;

    @Override
    public Map<String, Object> queryAll(SupplierContactQueryCriteria criteria, Pageable pageable) {
        Page<SupplierContact> page = contactRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        Map<String, Object> map = new HashMap<>();
        List<SupplierContactDto> list = new ArrayList<>();
        long total = 0L;
        if (ValidationUtil.isNotEmpty(page.getContent())) {
            list = contactMapper.toDto(page.getContent());
            list.forEach(contact -> {
                contact.setSupplierName(getSupplierNameById(contact.getSupplierId()));
            });
            total = page.getTotalElements();
        }
        map.put("content", list);
        map.put("totalElements", total);
        return map;
    }

    @Override
    public List<SupplierContact> findBySupplierId(Long supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId).orElseGet(Supplier::new);
        ValidationUtil.isNull(supplier.getId(), "Supplier", "id", supplierId);
        return contactRepository.findBySupplierId(supplierId);
    }

    @Override
    public SupplierContactDto findById(Long contactId) {
        SupplierContact contact = contactRepository.findById(contactId).orElseGet(SupplierContact::new);
        ValidationUtil.isNull(contact.getId(), "SupplierContact", "id", contactId);
        SupplierContactDto dto = contactMapper.toDto(contact);
        if (dto != null) {
            if(!ValidationUtil.isBlank(dto.getHobby())){
                dto.setHobbyTags(dto.getHobby().split(","));
            }
        }
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SupplierContact create(SupplierContact resources) {
        Long supplierId = resources.getSupplierId();
        Supplier supplier = supplierRepository.findById(supplierId).orElseGet(Supplier::new);
        ValidationUtil.isNull(supplier.getId(), "Supplier", "id", supplierId);
        SupplierContact contact = contactRepository.findByNameAndSupplierId(resources.getName(),supplierId);
        // 同一个供应商下的重名校验
        if (contact != null) {
            throw new EntityExistException(SupplierContact.class, "name", resources.getName());
        }
        return contactRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SupplierContact resources) {
        Long supplierId = resources.getSupplierId();
        Supplier supplier = supplierRepository.findById(supplierId).orElseGet(Supplier::new);
        ValidationUtil.isNull(supplier.getId(), "Supplier", "id", supplierId);
        SupplierContact contact = contactRepository.findByNameAndSupplierId(resources.getName(),supplierId);
        // 同一个供应商下的重名校验
        if (contact != null && !contact.getId().equals(resources.getId())) {
            throw new EntityExistException(SupplierContact.class, "name", resources.getName());
        }
        contactRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        contactRepository.deleteAllByIdIn(ids);
    }

    @Override
    public List<SupplierContact> queryAll(SupplierContactQueryCriteria criteria) {
        return contactRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
    }

    @Override
    public void download(List<SupplierContact> contacts, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (SupplierContact contact : contacts) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("联系人名称", contact.getName());
            map.put("供应商名称", getSupplierNameById(contact.getSupplierId()));
            map.put("重要程度", contact.getImportantLevel());
            map.put("称谓", contact.getTitle());
            map.put("性别", contact.getGender());
            map.put("单位", contact.getUnit());
            map.put("部门", contact.getDepartment());
            map.put("职位", contact.getPost());
            map.put("在职状态", contact.getJobStatus());
            map.put("移动电话", contact.getPhone());
            map.put("邮箱地址", contact.getEmail());
            map.put("住址", contact.getAddress());
            map.put("创建日期", contact.getCreateTime());
            map.put("最后更新日期", contact.getUpdateTime());
            map.put("最后更新人", contact.getUpdateBy());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    private String getSupplierNameById(Long supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId).orElseGet(Supplier::new);
        ValidationUtil.isNull(supplier.getId(), "Supplier", "id", supplierId);
        return supplier.getName();
    }

    @Override
    public void verification(Set<Long> ids) {

    }

    @Override
    public Map<String, String> updateAvatar(Long contactId, MultipartFile multipartFile) {
        SupplierContact contact = contactRepository.findById(contactId).orElseGet(SupplierContact::new);
        ValidationUtil.isNull(contact.getId(), "SupplierContact", "id", contactId);
        String oldPath = contact.getAvatarPath();
        File file = FileUtil.upload(multipartFile, properties.getPath().getAvatar());
        contact.setAvatarPath(Objects.requireNonNull(file).getPath());
        contact.setAvatarName(file.getName());
        contactRepository.save(contact);
        if (StringUtils.isNotBlank(oldPath)) {
            FileUtil.del(oldPath);
        }
        return new HashMap<String, String>(1) {{
            put("avatar", file.getName());
        }};
    }
}
