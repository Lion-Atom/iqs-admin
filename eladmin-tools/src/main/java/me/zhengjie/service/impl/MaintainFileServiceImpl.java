package me.zhengjie.service.impl;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.config.FileProperties;
import me.zhengjie.domain.MaintainFile;
import me.zhengjie.domain.RepairFile;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.MaintainFileRepository;
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
public class MaintainFileServiceImpl implements MaintainFileService {

    private final MaintainFileRepository fileRepository;
    private final FileProperties properties;

    @Override
    public void uploadFile(Long maintainId, MultipartFile multipartFile) {
        FileUtil.checkSize(properties.getMaxSize(), multipartFile.getSize());
        String suffix = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
        assert suffix != null;
        String type = FileUtil.getFileType(suffix);

        File file = FileUtil.upload(multipartFile, properties.getPath().getPath() + type + File.separator);
        if (ObjectUtil.isNull(file)) {
            throw new BadRequestException("上传失败");
        }
        try {

            MaintainFile maintainFile = new MaintainFile(
                    maintainId,
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
    public List<MaintainFile> getByMaintainId(Long maintainId) {
        return fileRepository.findByMaintenanceId(maintainId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        // 删除
        fileRepository.deleteAllByIdIn(ids);
    }
}
