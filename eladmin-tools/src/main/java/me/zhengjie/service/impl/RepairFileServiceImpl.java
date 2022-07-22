package me.zhengjie.service.impl;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.config.FileProperties;
import me.zhengjie.domain.RepairFile;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.EquipRepairRepository;
import me.zhengjie.repository.RepairFileRepository;
import me.zhengjie.service.RepairFileService;
import me.zhengjie.utils.FileUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RepairFileServiceImpl implements RepairFileService {

    private final RepairFileRepository fileRepository;
    private final FileProperties properties;
    private final EquipRepairRepository repairRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadFile(Long repairId, MultipartFile multipartFile) {
        FileUtil.checkSize(properties.getMaxSize(), multipartFile.getSize());
        String suffix = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
        assert suffix != null;
        String type = FileUtil.getFileType(suffix);

        File file = FileUtil.upload(multipartFile, properties.getPath().getPath() + type + File.separator);
        String fileNameFormat = FileUtil.fileNameFormat(multipartFile, "维修确认单", suffix);
        if (ObjectUtil.isNull(file)) {
            throw new BadRequestException("上传失败");
        }
        try {

            RepairFile repairFile = new RepairFile(
                    repairId,
                    fileNameFormat,
                    file.getName(),
                    suffix,
                    file.getPath(),
                    type,
                    FileUtil.getSize(multipartFile.getSize())
            );

            fileRepository.save(repairFile);

        } catch (Exception e) {
            FileUtil.del(file);
            throw e;
        }
    }

    @Override
    public List<RepairFile> getByRepairId(Long repairId) {
        /*EquipRepair repair = repairRepository.findById(repairId).orElseGet(EquipRepair::new);
        ValidationUtil.isNull(repair.getId(), "EquipRepair", "id", repairId);*/
        return fileRepository.findByRepairId(repairId);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        // 删除
        fileRepository.deleteAllByIdIn(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delByRepairIdAndName(Long repairId, String realName) {

    }
}
