package me.zhengjie.service.impl;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.config.FileProperties;
import me.zhengjie.domain.ChangeFile;
import me.zhengjie.domain.SupplierFile;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.ChangeFileRepository;
import me.zhengjie.service.ChangeFileService;
import me.zhengjie.service.dto.ChangeFileQueryDto;
import me.zhengjie.utils.FileUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2022/1/19 11:33
 */
@Service
@RequiredArgsConstructor
public class ChangeFileServiceImpl implements ChangeFileService {

    private final FileProperties properties;
    private final ChangeFileRepository fileRepository;


    @Override
    public List<ChangeFile> findByCond(ChangeFileQueryDto dto) {
        List<ChangeFile> list = new ArrayList<>();
        if (dto.getFactorId() == null) {
            list = fileRepository.findByChangeIdAndFileType(dto.getChangeId(), dto.getFileType());
        } else if (dto.getFactorId() != null) {
            list = fileRepository.findBySupplierIdAndFactorIdAndFileType(dto.getChangeId(), dto.getFactorId(), dto.getFileType());
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadFile(Long changeId, Long factorId, String fileType, MultipartFile multipartFile) {
        if (factorId == 0) {
            factorId = null;
        }
        FileUtil.checkSize(properties.getMaxSize(), multipartFile.getSize());
        String suffix = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
        String type = FileUtil.getFileType(suffix);
        File file = FileUtil.upload(multipartFile, properties.getPath().getPath() + type + File.separator);
        if (ObjectUtil.isNull(file)) {
            throw new BadRequestException("上传失败");
        }
        try {
            ChangeFile changeFile = new ChangeFile(
                    changeId,
                    factorId,
                    fileType,
                    multipartFile.getOriginalFilename(),
                    file.getName(),
                    suffix,
                    file.getPath(),
                    type,
                    FileUtil.getSize(multipartFile.getSize())
            );

            fileRepository.save(changeFile);

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
}
