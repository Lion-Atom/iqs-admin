package me.zhengjie.service.impl;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;

import me.zhengjie.config.FileProperties;
import me.zhengjie.domain.TrExamDepartFile;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.TrExamDepartFileRepository;
import me.zhengjie.repository.TrainExamDepartRepository;
import me.zhengjie.service.TrExamDepartFileService;
import me.zhengjie.service.dto.TrExamDepartFileDto;
import me.zhengjie.service.dto.TrExamDepartFileQueryCriteria;
import me.zhengjie.service.mapstruct.TrExamDepartFileMapper;
import me.zhengjie.utils.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;


/**
 * @author TongMin Jie
 * @version V1
 * @date 2022/5/10 10:20
 */
@Service
@RequiredArgsConstructor
public class TrExamDepartFileServiceImpl implements TrExamDepartFileService {

    private final TrainExamDepartRepository departRepository;
    private final TrExamDepartFileMapper departFileMapper;
    private final TrExamDepartFileRepository fileRepository;
    private final FileProperties properties;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadFile(Long departId, String name, String fileDesc, MultipartFile multipartFile) {
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
            TrExamDepartFile trExamDepartFile = new TrExamDepartFile(
                    departId,
                    file.getName(),
                    name,
                    fileDesc,
                    suffix,
                    file.getPath(),
                    type,
                    FileUtil.getSize(multipartFile.getSize())
            );

            fileRepository.save(trExamDepartFile);

        } catch (Exception e) {
            FileUtil.del(file);
            throw e;
        }
    }

    @Override
    public Map<String, Object> query(TrExamDepartFileQueryCriteria criteria, Pageable pageable) {
        Page<TrExamDepartFile> page = fileRepository.findAll((root, query, cb) -> QueryHelp.getPredicate(root, criteria, cb), pageable);
        Map<String, Object> map = new HashMap<>();
        long total = 0L;
        List<TrExamDepartFileDto> list = new ArrayList<>();
        String curUser = SecurityUtils.getCurrentUsername();
        Boolean isAdmin = SecurityUtils.isAdmin();
        if (ValidationUtil.isNotEmpty(page.getContent())) {
            list = departFileMapper.toDto(page.getContent());
            list.forEach(dto -> {
                // 限制读写权限为：创建者和管理员
                if (!dto.getCreateBy().equals(curUser) && !isAdmin) {
                    dto.setHasDownloadAuthority(false);
                }
            });
            total = page.getTotalElements();
        }
        map.put("content", list);
        map.put("totalElements", total);
        return map;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        fileRepository.deleteAllByIdIn(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(TrExamDepartFile resources) {
        TrExamDepartFile departFile = fileRepository.findByDepartIdAndName(resources.getDepartId(), resources.getName());
        if (departFile != null && !departFile.getId().equals(resources.getId())) {
            throw new BadRequestException("当前考试题库中存在同名文件！请修改名称！");
        }
        fileRepository.save(resources);
    }
}
