package me.zhengjie.service.impl;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.config.FileProperties;
import me.zhengjie.domain.FileDept;
import me.zhengjie.domain.TrainMaterialFile;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.FileDeptRepository;
import me.zhengjie.repository.TrainMaterialFileRepository;
import me.zhengjie.service.TrainMaterialFileService;
import me.zhengjie.service.dto.TrainMaterialFileDto;
import me.zhengjie.service.dto.TrainMaterialFileQueryCriteria;
import me.zhengjie.service.mapstruct.TrainMaterialFileMapper;
import me.zhengjie.utils.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author TongMin Jie
 * @version V1
 * @date 2022/5/12 14:36
 */
@Service
@RequiredArgsConstructor
public class TrainMaterialFileServiceImpl implements TrainMaterialFileService {

    private final FileDeptRepository departRepository;
    private final TrainMaterialFileRepository materialFileRepository;
    private final TrainMaterialFileMapper materialFileMapper;
    private final FileProperties properties;

    @Override
    public Map<String, Object> query(TrainMaterialFileQueryCriteria criteria, Pageable pageable) {
        Page<TrainMaterialFile> page = materialFileRepository.findAll((root, query, cb) -> QueryHelp.getPredicate(root, criteria, cb), pageable);
        return PageUtil.toPage(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadFile(String name, Long departId, String author, String version, Boolean isInternal, String toolType, String fileDesc, Boolean enabled, MultipartFile multipartFile) {
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
            TrainMaterialFile materialFile = new TrainMaterialFile(
                    departId,
                    author,
                    version,
                    isInternal,
                    toolType,
                    file.getName(),
                    name,
                    fileDesc,
                    suffix,
                    file.getPath(),
                    type,
                    FileUtil.getSize(multipartFile.getSize()),
                    enabled
            );
            materialFileRepository.save(materialFile);
        } catch (Exception e) {
            FileUtil.del(file);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(TrainMaterialFile resources) {
        // todo 是否校验有无权限删改
        materialFileRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        // todo 是否校验有无权限删改
        materialFileRepository.deleteAllByIdIn(ids);
    }

    @Override
    public List<TrainMaterialFileDto> queryAll(TrainMaterialFileQueryCriteria criteria) {
        List<TrainMaterialFileDto> list = new ArrayList<>();
        List<TrainMaterialFile> files = materialFileRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
        if (ValidationUtil.isNotEmpty(files)) {
            Set<Long> deptIds = new HashSet<>();
            Map<Long, String> deptMap = new HashMap<>();
            list = materialFileMapper.toDto(files);
            list.forEach(staff -> {
                deptIds.add(staff.getDepartId());
            });
            initDepartName(list, deptIds, deptMap);
        }
        return list;
    }

    private void initDepartName(List<TrainMaterialFileDto> list, Set<Long> deptIds, Map<Long, String> deptMap) {
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
    public void download(List<TrainMaterialFileDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (TrainMaterialFileDto dto : queryAll) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("材料名称", dto.getName());
            map.put("作者", dto.getAuthor());
            map.put("部门", dto.getDepartName());
            map.put("材料描述", dto.getFileDesc());
            map.put("材料出处", dto.getIsInternal() ? "内部" : "外部");
            map.put("认证专业工具", dto.getToolType());
            map.put("版本", dto.getVersion());
            map.put("材料格式", dto.getType());
            map.put("材料大小", dto.getSize());
            map.put("创建日期", dto.getCreateTime());
            map.put("上传者", dto.getCreateBy());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public void updateFile(Long id, String name, Long departId, String author, String version, Boolean isInternal, String toolType, String fileDesc, Boolean enabled, MultipartFile multipartFile) {
        TrainMaterialFile materialFile = materialFileRepository.findById(id).orElseGet(TrainMaterialFile::new);
        ValidationUtil.isNull(materialFile.getId(), "TrainMaterialFile", "id", id);
        // 编辑外围信息
        materialFile.setAuthor(author);
        materialFile.setDepartId(departId);
        materialFile.setEnabled(enabled);
        materialFile.setFileDesc(fileDesc);
        materialFile.setIsInternal(isInternal);
        materialFile.setToolType(toolType);
        materialFile.setVersion(version);
        if (multipartFile == null) {
            materialFileRepository.save(materialFile);
        } else {
            // 更新文件内容
            FileUtil.checkSize(properties.getMaxSize(), multipartFile.getSize());
            String suffix = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
            assert suffix != null;
            String type = FileUtil.getFileType(suffix);
            File file = FileUtil.upload(multipartFile, properties.getPath().getPath() + type + File.separator);
            if (ObjectUtil.isNull(file)) {
                throw new BadRequestException("上传失败");
            }
            try {
                materialFile.setRealName(file.getName());
                materialFile.setPath(file.getPath());
                materialFile.setSize(FileUtil.getSize(multipartFile.getSize()));
                materialFile.setType(type);
                materialFile.setSuffix(suffix);
                materialFileRepository.save(materialFile);

            } catch (Exception e) {
                FileUtil.del(file);
                throw e;
            }
        }
    }
}
