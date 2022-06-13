package me.zhengjie.service.impl;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.config.FileProperties;
import me.zhengjie.domain.TrScheduleFile;
import me.zhengjie.domain.TrScheduleFileV2;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.TrScheduleFileRepository;
import me.zhengjie.repository.TrScheduleFileV2Repository;
import me.zhengjie.service.TrScheduleFileService;
import me.zhengjie.utils.FileUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TrScheduleFileServiceImpl implements TrScheduleFileService {

    private final TrScheduleFileV2Repository fileV2Repository;
    private final TrScheduleFileRepository fileRepository;
    private final FileProperties properties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadFile(Long trScheduleId, String fileType, String name, MultipartFile multipartFile) {
        FileUtil.checkSize(properties.getMaxSize(), multipartFile.getSize());
        String suffix = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
        assert suffix != null;
        String type = FileUtil.getFileType(suffix);

        File file = FileUtil.upload(multipartFile, properties.getPath().getPath() + type + File.separator);
        if (ObjectUtil.isNull(file)) {
            throw new BadRequestException("上传失败");
        }
        try {

            TrScheduleFile scheduleFile = new TrScheduleFile(
                    trScheduleId,
                    fileType,
                    file.getName(),
                    name,
                    suffix,
                    file.getPath(),
                    type,
                    FileUtil.getSize(multipartFile.getSize())
            );

            fileRepository.save(scheduleFile);

        } catch (Exception e) {
            FileUtil.del(file);
            throw e;
        }
    }

    @Override
    public List<TrScheduleFile> getByTrScheduleId(Long trScheduleId) {
        return fileRepository.findByTrScheduleId(trScheduleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        // 删除
        fileRepository.deleteAllByIdIn(ids);
    }

    @Override
    public void uploadFileV2(String name, Long trScheduleId, String fileType, String author,Boolean isInternal, String toolType, String fileDesc, MultipartFile multipartFile) {
        FileUtil.checkSize(properties.getMaxSize(), multipartFile.getSize());
        String suffix = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
        assert suffix != null;
        String type = FileUtil.getFileType(suffix);

        File file = FileUtil.upload(multipartFile, properties.getPath().getPath() + type + File.separator);
        if (ObjectUtil.isNull(file)) {
            throw new BadRequestException("上传失败");
        }
        try {

            TrScheduleFileV2 scheduleFile = new TrScheduleFileV2(
                    trScheduleId,
                    author,
                    isInternal,
                    toolType,
                    fileType,
                    file.getName(),
                    name,
                    suffix,
                    file.getPath(),
                    type,
                    FileUtil.getSize(multipartFile.getSize())
            );

            fileV2Repository.save(scheduleFile);

        } catch (Exception e) {
            FileUtil.del(file);
            throw e;
        }
    }

    @Override
    public List<TrScheduleFile> getByTrScheduleIdAndType(Long trScheduleId, String fileType) {
        return fileRepository.findByTrScheduleIdAndType(trScheduleId,fileType);
    }
}
