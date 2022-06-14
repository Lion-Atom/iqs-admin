package me.zhengjie.service.impl;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.config.FileProperties;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.TrExamDepartFile;
import me.zhengjie.domain.TrScheduleFile;
import me.zhengjie.domain.TrScheduleFileV2;
import me.zhengjie.domain.TrainMaterialFile;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.TrExamDepartFileRepository;
import me.zhengjie.repository.TrScheduleFileRepository;
import me.zhengjie.repository.TrScheduleFileV2Repository;
import me.zhengjie.repository.TrainMaterialFileRepository;
import me.zhengjie.service.TrScheduleFileService;
import me.zhengjie.service.dto.TrainMaterialSyncDto;
import me.zhengjie.utils.FileUtil;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TrScheduleFileServiceImpl implements TrScheduleFileService {

    private final TrScheduleFileV2Repository fileV2Repository;
    private final TrScheduleFileRepository fileRepository;
    private final FileProperties properties;
    private final TrainMaterialFileRepository trMaterialFileRepository;
    private final TrExamDepartFileRepository trExamDepartFileRepository;

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
    public List<TrScheduleFileV2> getByTrScheduleId(Long trScheduleId) {
        return fileV2Repository.findByTrScheduleId(trScheduleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        // 删除
        fileRepository.deleteAllByIdIn(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadFileV2(String name, Long trScheduleId, String fileType, String fileSource, String author, Boolean isInternal, String toolType, String fileDesc, MultipartFile multipartFile) {
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
                    null,
                    author,
                    isInternal,
                    toolType,
                    fileType,
                    fileSource,
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
    public List<TrScheduleFileV2> getByTrScheduleIdAndType(Long trScheduleId, String fileType) {
        return fileV2Repository.findByTrScheduleIdAndType(trScheduleId, fileType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncFiles(TrainMaterialSyncDto syncDto) {
        fileV2Repository.deleteByTrScheduleIdAndFileSourceAndFileType(syncDto.getTrScheduleId(), CommonConstants.TRAIN_SCHEDULE_FILE_SOURCE_SELECTED, syncDto.getFileType());
        if (!syncDto.getBindingFileIds().isEmpty()) {
            // 非空条件下查询培训材料或者部门考试试题信息集合
            if (syncDto.getFileType().equals(CommonConstants.TRAIN_SCHEDULE_FILE_TYPE_MATERIAL)) {
                List<TrainMaterialFile> trMaterialFiles = trMaterialFileRepository.findByIdIn(syncDto.getBindingFileIds());
                if (ValidationUtil.isNotEmpty(trMaterialFiles)) {
                    List<TrScheduleFileV2> scheduleFiles = new ArrayList<>();
                    trMaterialFiles.forEach(file -> {
                        TrScheduleFileV2 trScheduleFile = new TrScheduleFileV2(
                                syncDto.getTrScheduleId(),
                                file.getId(),
                                file.getAuthor(),
                                file.getIsInternal(),
                                file.getToolType(),
                                syncDto.getFileType(),
                                CommonConstants.TRAIN_SCHEDULE_FILE_SOURCE_SELECTED,
                                file.getRealName(),
                                file.getName(),
                                file.getSuffix(),
                                file.getPath(),
                                file.getType(),
                                file.getSize()
                        );
                        scheduleFiles.add(trScheduleFile);
                    });
                    fileV2Repository.saveAll(scheduleFiles);
                }
            } else if (syncDto.getFileType().equals(CommonConstants.TRAIN_SCHEDULE_FILE_TYPE_EXAM)) {
                List<TrExamDepartFile> trExamDepartFiles = trExamDepartFileRepository.findByIdIn(syncDto.getBindingFileIds());
                if (ValidationUtil.isNotEmpty(trExamDepartFiles)) {
                    List<TrScheduleFileV2> scheduleFiles = new ArrayList<>();
                    trExamDepartFiles.forEach(file -> {
                        TrScheduleFileV2 trScheduleFile = new TrScheduleFileV2(
                                syncDto.getTrScheduleId(),
                                file.getId(),
                                null,
                                null,
                                null,
                                syncDto.getFileType(),
                                CommonConstants.TRAIN_SCHEDULE_FILE_SOURCE_SELECTED,
                                file.getRealName(),
                                file.getName(),
                                file.getSuffix(),
                                file.getPath(),
                                file.getType(),
                                file.getSize()
                        );
                        scheduleFiles.add(trScheduleFile);
                    });
                    fileV2Repository.saveAll(scheduleFiles);
                }
            }
        }
    }
}
