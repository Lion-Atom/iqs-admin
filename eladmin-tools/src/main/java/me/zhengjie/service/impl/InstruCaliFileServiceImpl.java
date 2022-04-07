package me.zhengjie.service.impl;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.config.FileProperties;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.CalibrationFile;
import me.zhengjie.domain.InstruCali;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.InstruCaliFileRepository;
import me.zhengjie.repository.InstruCaliRepository;
import me.zhengjie.service.InstruCaliFileService;
import me.zhengjie.service.dto.InstruCaliFileQueryCriteria;
import me.zhengjie.utils.FileUtil;
import me.zhengjie.utils.QueryHelp;
import org.aspectj.apache.bcel.classfile.Constant;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2022/3/14 13:14
 */
@Service
@RequiredArgsConstructor
public class InstruCaliFileServiceImpl implements InstruCaliFileService {

    private final InstruCaliRepository caliRepository;
    private final InstruCaliFileRepository fileRepository;
    private final FileProperties properties;

    @Override
    public List<CalibrationFile> queryAll(InstruCaliFileQueryCriteria criteria) {
        Sort sort = Sort.by(Sort.Direction.DESC, "isLatest");
        return fileRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), sort);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadFile(Long caliId, Boolean isLatest,String caliResult, String failDesc, MultipartFile multipartFile) {

        InstruCali cali = caliRepository.findById(caliId).orElseGet(InstruCali::new);
        FileUtil.checkSize(properties.getMaxSize(), multipartFile.getSize());
        String suffix = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
        assert suffix != null;
        String type = FileUtil.getFileType(suffix);

        // 替代之前的最新校准报告
        if (isLatest) {
            fileRepository.updateToOld(caliId);
            if (cali.getId() != null) {
                cali.setStatus(CommonConstants.INSTRU_CALI_STATUS_FINISHED);
                caliRepository.save(cali);
            }
        }

        File file = FileUtil.upload(multipartFile, properties.getPath().getPath() + type + File.separator);
        if (ObjectUtil.isNull(file)) {
            throw new BadRequestException("上传失败");
        }
        try {

            CalibrationFile caliFile = new CalibrationFile(
                    caliId,
                    "仪器校准",
                    isLatest,
                    caliResult,
                    failDesc,
                    multipartFile.getOriginalFilename(),
                    file.getName(),
                    suffix,
                    file.getPath(),
                    type,
                    FileUtil.getSize(multipartFile.getSize())
            );

            fileRepository.save(caliFile);

        } catch (Exception e) {
            FileUtil.del(file);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        // 删除校验
        fileRepository.deleteAllByIdIn(ids);
    }
}
