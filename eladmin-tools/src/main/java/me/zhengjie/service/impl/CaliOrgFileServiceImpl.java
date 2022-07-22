package me.zhengjie.service.impl;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.config.FileProperties;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.Auditor;
import me.zhengjie.domain.AuditorFile;
import me.zhengjie.domain.CaliOrgFile;
import me.zhengjie.domain.PreTrail;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.AuditorFileRepository;
import me.zhengjie.repository.AuditorRepository;
import me.zhengjie.repository.CaliOrgFileRepository;
import me.zhengjie.repository.PreTrailRepository;
import me.zhengjie.service.AuditorFileService;
import me.zhengjie.service.CaliOrgFileService;
import me.zhengjie.utils.FileUtil;
import me.zhengjie.utils.SecurityUtils;
import me.zhengjie.utils.StringUtils;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/9/14 16:03
 */
@Service
@RequiredArgsConstructor
public class CaliOrgFileServiceImpl implements CaliOrgFileService {

    private final CaliOrgFileRepository fileRepository;
    private final FileProperties properties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadFile(Long caliOrgId, MultipartFile multipartFile) {

        FileUtil.checkSize(properties.getMaxSize(), multipartFile.getSize());
        String suffix = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
        assert suffix != null;
        String type = FileUtil.getFileType(suffix);

        File file = FileUtil.upload(multipartFile, properties.getPath().getPath() + type + File.separator);
        String fileNameFormat = FileUtil.fileNameFormat(multipartFile, "资格证明", suffix);
        if (ObjectUtil.isNull(file)) {
            throw new BadRequestException("上传失败");
        }
        try {

            CaliOrgFile caliOrgFile = new CaliOrgFile(
                    caliOrgId,
                    "仪校机构",
                    fileNameFormat,
                    file.getName(),
                    suffix,
                    file.getPath(),
                    type,
                    FileUtil.getSize(multipartFile.getSize())
            );

            fileRepository.save(caliOrgFile);

        } catch (Exception e) {
            FileUtil.del(file);
            throw e;
        }
    }

    @Override
    public List<CaliOrgFile> findByCaliOrgId(Long caliOrgId) {
        return fileRepository.findByCaliOrgId(caliOrgId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        // 删除校验:前端已做校验
        fileRepository.deleteAllByIdIn(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delByCaliOrgIdAndName(Long caliOrgId, String realName) {
        fileRepository.deleteByCaliOrgIdAndRealName(caliOrgId,realName);
    }
}
