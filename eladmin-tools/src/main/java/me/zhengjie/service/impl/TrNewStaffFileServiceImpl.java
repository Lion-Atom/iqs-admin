package me.zhengjie.service.impl;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.config.FileProperties;
import me.zhengjie.domain.RepairFile;
import me.zhengjie.domain.TrNewStaffFile;
import me.zhengjie.domain.TrainNewStaff;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.EquipRepairRepository;
import me.zhengjie.repository.RepairFileRepository;
import me.zhengjie.repository.TrNewStaffFileRepository;
import me.zhengjie.repository.TrainNewStaffRepository;
import me.zhengjie.service.RepairFileService;
import me.zhengjie.service.TrNewStaffFileService;
import me.zhengjie.utils.FileUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TrNewStaffFileServiceImpl implements TrNewStaffFileService {

    private final TrNewStaffFileRepository fileRepository;
    private final FileProperties properties;
    private final TrainNewStaffRepository staffRepository;

    @Override
    public void uploadFile(Long trNewStaffId, MultipartFile multipartFile) {
        FileUtil.checkSize(properties.getMaxSize(), multipartFile.getSize());
        String suffix = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
        assert suffix != null;
        String type = FileUtil.getFileType(suffix);

        File file = FileUtil.upload(multipartFile, properties.getPath().getPath() + type + File.separator);
        if (ObjectUtil.isNull(file)) {
            throw new BadRequestException("上传失败");
        }
        try {

            TrNewStaffFile trNewStaffFile = new TrNewStaffFile(
                    trNewStaffId,
                    multipartFile.getOriginalFilename(),
                    file.getName(),
                    suffix,
                    file.getPath(),
                    type,
                    FileUtil.getSize(multipartFile.getSize())
            );

            fileRepository.save(trNewStaffFile);

        } catch (Exception e) {
            FileUtil.del(file);
            throw e;
        }
    }

    @Override
    public List<TrNewStaffFile> getByTrNewStaffId(Long trNewStaffId) {
        return fileRepository.findByTrNewStaffId(trNewStaffId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        // 删除
        fileRepository.deleteAllByIdIn(ids);
    }
}
