package me.zhengjie.service.impl;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.config.FileProperties;
import me.zhengjie.domain.TrCertificationFile;
import me.zhengjie.domain.TrNewStaffFile;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.TrCertificationFileRepository;
import me.zhengjie.repository.TrNewStaffFileRepository;
import me.zhengjie.repository.TrainCertificationRepository;
import me.zhengjie.repository.TrainNewStaffRepository;
import me.zhengjie.service.TrCertificationFileService;
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
public class TrCertificationFileServiceImpl implements TrCertificationFileService {

    private final TrCertificationFileRepository fileRepository;
    private final FileProperties properties;
    private final TrainCertificationRepository certificationRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadFile(Long trCertificationId, MultipartFile multipartFile) {
        FileUtil.checkSize(properties.getMaxSize(), multipartFile.getSize());
        String suffix = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
        assert suffix != null;
        String type = FileUtil.getFileType(suffix);

        File file = FileUtil.upload(multipartFile, properties.getPath().getPath() + type + File.separator);
        if (ObjectUtil.isNull(file)) {
            throw new BadRequestException("上传失败");
        }
        try {

            TrCertificationFile certificationFile = new TrCertificationFile(
                    trCertificationId,
                    multipartFile.getOriginalFilename(),
                    file.getName(),
                    suffix,
                    file.getPath(),
                    type,
                    FileUtil.getSize(multipartFile.getSize()),
                    0L
            );

            fileRepository.save(certificationFile);

        } catch (Exception e) {
            FileUtil.del(file);
            throw e;
        }
    }

    @Override
    public List<TrCertificationFile> getByTrCertificationId(Long trCertificationId) {
        return fileRepository.findByTrCertificationId(trCertificationId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        // 删除
        fileRepository.deleteAllByIdIn(ids);
    }
}
