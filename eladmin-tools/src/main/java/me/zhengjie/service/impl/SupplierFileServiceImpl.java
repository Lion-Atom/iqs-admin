package me.zhengjie.service.impl;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.config.FileProperties;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.SupplierFile;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.SupplierFileRepository;
import me.zhengjie.service.SupplierFileService;
import me.zhengjie.service.dto.SupplierFileQueryDto;
import me.zhengjie.service.dto.SupplierFileReplaceDto;
import me.zhengjie.utils.FileUtil;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/11/29 10:19
 */
@Service
@RequiredArgsConstructor
public class SupplierFileServiceImpl implements SupplierFileService {

    private final FileProperties properties;
    private final SupplierFileRepository fileRepository;


    @Override
    public List<SupplierFile> findByCond(SupplierFileQueryDto dto) {
        List<SupplierFile> list = new ArrayList<>();
        if (dto.getSupplierContactId() == null) {
            list = fileRepository.findBySupplierIdAndFileType(dto.getSupplierId(), dto.getFileType());
        } else if (dto.getSupplierContactId() != null) {
            list = fileRepository.findBySupplierIdAndSupplierContactIdAndFileType(dto.getSupplierId(), dto.getSupplierContactId(), dto.getFileType());
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadFile(Long supplierId, Long contactId, String fileType, MultipartFile multipartFile) {
        if (contactId == 0) {
            contactId = null;
        }
        FileUtil.checkSize(properties.getMaxSize(), multipartFile.getSize());
        String suffix = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
        String type = FileUtil.getFileType(suffix);
        File file = FileUtil.upload(multipartFile, properties.getPath().getPath() + type + File.separator);
        if (ObjectUtil.isNull(file)) {
            throw new BadRequestException("上传失败");
        }
        try {
            SupplierFile supplierFile = new SupplierFile(
                    supplierId,
                    contactId,
                    fileType,
                    multipartFile.getOriginalFilename(),
                    file.getName(),
                    suffix,
                    file.getPath(),
                    type,
                    FileUtil.getSize(multipartFile.getSize())
            );

            fileRepository.save(supplierFile);

        } catch (Exception e) {
            FileUtil.del(file);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        fileRepository.deleteAllByIdIn(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFile(SupplierFileReplaceDto dto) {
        List<SupplierFile> files = new ArrayList<>();
        if (dto.getSupplierContactId() == null) {
            // 供应商附件信息绑定变更复位
            files = fileRepository.findBySupplierIdAndFileTypeIn(dto.getUId(), dto.getFileTypes());
            files.forEach(file -> {
                file.setSupplierId(dto.getSupplierId());
            });
            fileRepository.saveAll(files);
        } else if (dto.getSupplierContactId() != null && Arrays.asList(dto.getFileTypes()).contains("联系人")) {
            // 供应商联系人的附件信息绑定变更复位
            // 供应商附件信息绑定变更复位
            files = fileRepository.findBySupplierIdAndSupplierContactIdAndFileTypeIn(dto.getUId(), dto.getUId(), dto.getFileTypes());
            files.forEach(file -> {
                file.setSupplierId(dto.getSupplierId());
                file.setSupplierContactId(dto.getSupplierContactId());
            });
            fileRepository.saveAll(files);
        } else if (dto.getSupplierContactId() == 0 && CommonConstants.SUPPLIER_SPECIAL_FILE_TYPE_LIST.containsAll(Arrays.asList(dto.getFileTypes()))) {
            files = fileRepository.findBySupplierIdAndSupplierContactIdIsNotNullAndFileType(dto.getUId(), dto.getFileTypes());
            files.forEach(file -> {
                file.setSupplierId(dto.getSupplierId());
            });
            fileRepository.saveAll(files);
        }
    }
}
