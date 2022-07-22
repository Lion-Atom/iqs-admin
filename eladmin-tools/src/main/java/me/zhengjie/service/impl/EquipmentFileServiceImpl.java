package me.zhengjie.service.impl;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.config.FileProperties;
import me.zhengjie.domain.EquipmentFile;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.EquipmentFileRepository;
import me.zhengjie.repository.EquipmentRepository;
import me.zhengjie.service.EquipmentFileService;
import me.zhengjie.service.dto.EquipFileQueryByExample;
import me.zhengjie.utils.FileUtil;
import me.zhengjie.utils.QueryHelp;
import me.zhengjie.utils.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * @author TongMin Jie
 * @version V1
 * @date 2022/7/13 16:07
 */
@Service
@RequiredArgsConstructor
public class EquipmentFileServiceImpl implements EquipmentFileService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentFileRepository fileRepository;
    private final FileProperties properties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadFile(Long equipId, String fileType, MultipartFile multipartFile) {
        FileUtil.checkSize(properties.getMaxSize(), multipartFile.getSize());
        String suffix = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
        assert suffix != null;
        String type = FileUtil.getFileType(suffix);
        String fileNameFormat = FileUtil.fileNameFormat(multipartFile, fileType, suffix);
        File file = FileUtil.upload(multipartFile, properties.getPath().getPath() + type + File.separator);
        if (ObjectUtil.isNull(file)) {
            throw new BadRequestException("上传失败");
        }
        try {

            EquipmentFile equipmentFile = new EquipmentFile(
                    equipId,
                    fileType,
                    fileNameFormat,
                    file.getName(),
                    suffix,
                    file.getPath(),
                    type,
                    FileUtil.getSize(multipartFile.getSize())
            );

            fileRepository.save(equipmentFile);

        } catch (Exception e) {
            FileUtil.del(file);
            throw e;
        }
    }

    @Override
    public List<EquipmentFile> queryByExample(EquipFileQueryByExample queryByExample) {
        return fileRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, queryByExample, criteriaBuilder));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        fileRepository.deleteAllByIdIn(ids);
    }
}
