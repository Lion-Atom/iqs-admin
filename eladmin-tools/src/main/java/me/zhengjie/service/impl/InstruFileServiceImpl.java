package me.zhengjie.service.impl;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.config.FileProperties;
import me.zhengjie.domain.InstrumentFile;
import me.zhengjie.domain.TrExamStaffTranscript;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.InstruFileRepository;
import me.zhengjie.repository.TrExamStaffTranscriptRepository;
import me.zhengjie.service.InstruFileService;
import me.zhengjie.service.TrExamStaffTranscriptionService;
import me.zhengjie.utils.DateUtil;
import me.zhengjie.utils.FileUtil;
import me.zhengjie.utils.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.text.ParseException;
import java.util.List;
import java.util.Set;

/**
 * @author TongMin Jie
 * @version V1
 * @date 2022/5/27 16:24
 */
@Service
@RequiredArgsConstructor
public class InstruFileServiceImpl implements InstruFileService {

    private final InstruFileRepository instruFileRepository;
    private final FileProperties properties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadFile(Long instruId, String name, MultipartFile multipartFile) {
        FileUtil.checkSize(properties.getMaxSize(), multipartFile.getSize());
        String suffix = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
        assert suffix != null;
        String type = FileUtil.getFileType(suffix);

        File file = FileUtil.upload(multipartFile, properties.getPath().getPath() + type + File.separator);
        if (ObjectUtil.isNull(file)) {
            throw new BadRequestException("上传失败");
        }
        try {
            name = StringUtils.isBlank(name) ? FileUtil.getFileNameNoEx(multipartFile.getOriginalFilename()) : name;
            InstrumentFile instrumentFile = new InstrumentFile(
                    instruId,
                    file.getName(),
                    name,
                    suffix,
                    file.getPath(),
                    type,
                    FileUtil.getSize(multipartFile.getSize())
            );
            instruFileRepository.save(instrumentFile);
        } catch (Exception e) {
            FileUtil.del(file);
            throw e;
        }
    }

    @Override
    public List<InstrumentFile> getByInstruId(Long instruId) {
        return instruFileRepository.findByInstruId(instruId);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        instruFileRepository.deleteAllByIdIn(ids);
    }

    @Override
    public void uploadFileV2(Long instruId, MultipartFile multipartFile) {
        FileUtil.checkSize(properties.getMaxSize(), multipartFile.getSize());
        String suffix = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
        assert suffix != null;
        String type = FileUtil.getFileType(suffix);

        File file = FileUtil.upload(multipartFile, properties.getPath().getPath() + type + File.separator);
        String fileNameFormat = FileUtil.fileNameFormat(multipartFile, "异常报告", suffix);

        if (ObjectUtil.isNull(file)) {
            throw new BadRequestException("上传失败");
        }
        try {
            String name = FileUtil.getFileNameNoEx(multipartFile.getOriginalFilename()) + "异常报告";
            InstrumentFile instrumentFile = new InstrumentFile(
                    instruId,
                    fileNameFormat,
                    name,
                    suffix,
                    file.getPath(),
                    type,
                    FileUtil.getSize(multipartFile.getSize())
            );
            instruFileRepository.save(instrumentFile);
        } catch (Exception e) {
            FileUtil.del(file);
            throw e;
        }
    }
}
