package me.zhengjie.service.impl;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;

import me.zhengjie.config.FileProperties;
import me.zhengjie.domain.FileDept;
import me.zhengjie.domain.TrExamDepartFile;
import me.zhengjie.domain.TrScheduleFile;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.FileDeptRepository;
import me.zhengjie.repository.TrExamDepartFileRepository;
import me.zhengjie.repository.TrScheduleFileRepository;
import me.zhengjie.repository.TrainExamDepartRepository;
import me.zhengjie.service.TrExamDepartFileService;
import me.zhengjie.service.dto.TrExamDepartFileDto;
import me.zhengjie.service.dto.TrExamDepartFileQueryCriteria;
import me.zhengjie.service.dto.TrainExamFileQueryByExample;
import me.zhengjie.service.mapstruct.TrExamDepartFileMapper;
import me.zhengjie.utils.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.swing.text.StyledEditorKit;
import java.io.File;
import java.io.IOException;
import java.util.*;


/**
 * @author TongMin Jie
 * @version V1
 * @date 2022/5/10 10:20
 */
@Service
@RequiredArgsConstructor
public class TrExamDepartFileServiceImpl implements TrExamDepartFileService {

    private final FileDeptRepository departRepository;
    private final TrExamDepartFileMapper departFileMapper;
    private final TrExamDepartFileRepository fileRepository;
    private final FileProperties properties;
    private final TrScheduleFileRepository trScheduleFileRepository;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadFile(Long departId, String name, Boolean enabled, String fileDesc, MultipartFile multipartFile) {
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
                    FileUtil.getSize(multipartFile.getSize()),
                    enabled
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

    @Override
    public List<TrExamDepartFileDto> queryAll(TrExamDepartFileQueryCriteria criteria) {
        List<TrExamDepartFileDto> list = new ArrayList<>();
        List<TrExamDepartFile> staffs = fileRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
        if (ValidationUtil.isNotEmpty(staffs)) {
            Set<Long> deptIds = new HashSet<>();
            Map<Long, String> deptMap = new HashMap<>();
            list = departFileMapper.toDto(staffs);
            list.forEach(staff -> {
                deptIds.add(staff.getDepartId());
            });
            initDepartName(list, deptIds, deptMap);
        }
        return list;
    }

    private void initDepartName(List<TrExamDepartFileDto> list, Set<Long> deptIds, Map<Long, String> deptMap) {
        if (!deptIds.isEmpty()) {
            List<FileDept> deptList = departRepository.findByIdIn(deptIds);
            deptList.forEach(dept -> {
                deptMap.put(dept.getId(), dept.getName());
            });
            if (ValidationUtil.isNotEmpty(deptList)) {
                list.forEach(dto -> {
                    dto.setDepartName(deptMap.get(dto.getDepartId()));
                });
            }
        }
    }

    @Override
    public void download(List<TrExamDepartFileDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (TrExamDepartFileDto dto : queryAll) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("试卷名称", dto.getName());
            map.put("部门", dto.getDepartName());
            map.put("试卷状态", dto.getEnabled() ? "有效" : "废除");
            map.put("试卷描述", dto.getFileDesc());
            map.put("试卷格式", dto.getType());
            map.put("试卷大小", dto.getSize());
            map.put("创建日期", dto.getCreateTime());
            map.put("创建人", dto.getCreateBy());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public void uploadScheduleFile(Long trScheduleId, Set<Long> departIds, String name, Boolean enabled, String fileDesc, MultipartFile multipartFile) {
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
            if (!departIds.isEmpty()) {
                List<TrExamDepartFile> examFiles = new ArrayList<>();
                String finalName = name;
                departIds.forEach(departId -> {
                    TrExamDepartFile trExamDepartFile = new TrExamDepartFile(
                            departId,
                            file.getName(),
                            finalName,
                            fileDesc,
                            suffix,
                            file.getPath(),
                            type,
                            FileUtil.getSize(multipartFile.getSize()),
                            enabled
                    );
                    examFiles.add(trExamDepartFile);
                });
                fileRepository.saveAll(examFiles);
                // todo 同步到培训计划附件
                TrScheduleFile scheduleFile = new TrScheduleFile(
                        trScheduleId,
                        "培训题库",
                        file.getName(),
                        name,
                        suffix,
                        file.getPath(),
                        type,
                        FileUtil.getSize(multipartFile.getSize())
                );
                trScheduleFileRepository.save(scheduleFile);
            }
        } catch (Exception e) {
            FileUtil.del(file);
            throw e;
        }
    }

    @Override
    public List<TrExamDepartFileDto> findByExample(TrainExamFileQueryByExample queryDto) {
        List<TrExamDepartFileDto> list = new ArrayList<>();
        List<TrExamDepartFile> files = fileRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, queryDto, criteriaBuilder));
       // todo 根据名称、类型和大小去重
        if (ValidationUtil.isNotEmpty(files)) {
            Set<Long> deptIds = new HashSet<>();
            Map<Long, String> deptMap = new HashMap<>();
            list = departFileMapper.toDto(files);
            list.forEach(staff -> {
                deptIds.add(staff.getDepartId());
            });
            initDepartName(list, deptIds, deptMap);
        }
        return list;
    }
}
