package me.zhengjie.service.impl;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.config.FileProperties;
import me.zhengjie.domain.InstruCaliFileV2;
import me.zhengjie.domain.MaintainFile;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.InstruCalibrationFileV2Repository;
import me.zhengjie.repository.MaintainFileRepository;
import me.zhengjie.service.InstruCaliFileServiceV2;
import me.zhengjie.service.InstruCalibrationServiceV2;
import me.zhengjie.service.MaintainFileService;
import me.zhengjie.utils.FileUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class InstruCaliFileServiceV2Impl implements InstruCaliFileServiceV2 {

    private final InstruCalibrationFileV2Repository fileRepository;
    private final FileProperties properties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadFile(Long caliId, MultipartFile multipartFile) {
        FileUtil.checkSize(properties.getMaxSize(), multipartFile.getSize());
        String suffix = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
        assert suffix != null;
        String type = FileUtil.getFileType(suffix);

        File file = FileUtil.upload(multipartFile, properties.getPath().getPath() + type + File.separator);
        if (ObjectUtil.isNull(file)) {
            throw new BadRequestException("上传失败");
        }
        try {

            InstruCaliFileV2 maintainFile = new InstruCaliFileV2(
                    caliId,
                    multipartFile.getOriginalFilename(),
                    file.getName(),
                    suffix,
                    file.getPath(),
                    type,
                    FileUtil.getSize(multipartFile.getSize())
            );

            fileRepository.save(maintainFile);

        } catch (Exception e) {
            FileUtil.del(file);
            throw e;
        }
    }

    @Override
    public List<InstruCaliFileV2> getByCaliId(Long caliId) {
        return fileRepository.findByCaliId(caliId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        // 删除
        fileRepository.deleteAllByIdIn(ids);
    }
}
