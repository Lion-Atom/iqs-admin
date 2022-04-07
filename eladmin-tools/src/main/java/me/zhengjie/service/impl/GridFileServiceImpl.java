package me.zhengjie.service.impl;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.config.FileProperties;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.GridFile;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.GridFileRepository;
import me.zhengjie.service.GridFileService;
import me.zhengjie.service.dto.InstruGridFileQueryCriteria;
import me.zhengjie.utils.FileUtil;
import me.zhengjie.utils.QueryHelp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GridFileServiceImpl implements GridFileService {

    private final GridFileRepository fileRepository;
    private final FileProperties properties;

    @Override
    public List<GridFile> queryByExample(InstruGridFileQueryCriteria criteria) {
        return fileRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadGridFile(String fileType, MultipartFile multipartFile) {
        FileUtil.checkSize(properties.getMaxSize(), multipartFile.getSize());
        String suffix = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
        assert suffix != null;
        String type = FileUtil.getFileType(suffix);

        File file = FileUtil.upload(multipartFile, properties.getPath().getPath() + type + File.separator);
        if (ObjectUtil.isNull(file)) {
            throw new BadRequestException("上传失败");
        }
        try {

            GridFile caliFile = new GridFile(
                    fileType,
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
    public void deleteGridFiles(Set<Long> ids) {
        // 删除校验:前端已做校验
        fileRepository.deleteAllByIdIn(ids);
    }
}
