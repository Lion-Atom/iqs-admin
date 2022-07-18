package me.zhengjie.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import lombok.RequiredArgsConstructor;
import me.zhengjie.config.FileProperties;
import me.zhengjie.domain.CsFeedbackFile;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.CsFeedbackFileRepository;
import me.zhengjie.service.CsFeedbackFileService;
import me.zhengjie.utils.FileUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * @author TongMin Jie
 * @version V1
 * @date 2022/7/15 11:20
 */
@Service
@RequiredArgsConstructor
@DS("self")
public class CsFeedbackFileServiceImpl implements CsFeedbackFileService {

    private final CsFeedbackFileRepository fileRepository;
    private final FileProperties properties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadFile(Long csFeedbackId, MultipartFile multipartFile) {
        FileUtil.checkSize(properties.getMaxSize(), multipartFile.getSize());
        String suffix = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
        assert suffix != null;
        String type = FileUtil.getFileType(suffix);

        File file = FileUtil.upload(multipartFile, properties.getPath().getPath() + type + File.separator);
        if (ObjectUtil.isNull(file)) {
            throw new BadRequestException("上传失败");
        }
        try {

            CsFeedbackFile csFeedbackFile = new CsFeedbackFile(
                    csFeedbackId,
                    multipartFile.getOriginalFilename(),
                    file.getName(),
                    suffix,
                    file.getPath(),
                    type,
                    FileUtil.getSize(multipartFile.getSize())
            );

            fileRepository.save(csFeedbackFile);

        } catch (Exception e) {
            FileUtil.del(file);
            throw e;
        }
    }

    @Override
    public List<CsFeedbackFile> getByCsFeedbackId(Long csFeedbackId) {
        return fileRepository.findByCsFeedbackId(csFeedbackId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        fileRepository.deleteAllByIdIn(ids);
    }
}
