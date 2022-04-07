package me.zhengjie.service.impl;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.config.FileProperties;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.*;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.AuditorFileRepository;
import me.zhengjie.repository.AuditorRepository;
import me.zhengjie.repository.PreTrailRepository;
import me.zhengjie.service.AuditorFileService;
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
public class AuditorFileServiceImpl implements AuditorFileService {

    private final AuditorRepository auditorRepository;
    private final AuditorFileRepository auditorFileRepository;
    private final FileProperties properties;
    private final PreTrailRepository preTrailRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadFile(Long auditorId, MultipartFile multipartFile) {
        // todo 权限校验

        // 查询问题判null
        Auditor auditor = auditorRepository.findById(auditorId).orElseGet(Auditor::new);
        if (auditor == null) {
            throw new BadRequestException("未查到该审核人员信息");
        }
        FileUtil.checkSize(properties.getMaxSize(), multipartFile.getSize());
        String suffix = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
        String type = FileUtil.getFileType(suffix);
        File file = FileUtil.upload(multipartFile, properties.getPath().getPath() + type + File.separator);
        if (ObjectUtil.isNull(file)) {
            throw new BadRequestException("上传失败");
        }
        try {

            AuditorFile auditorFile = new AuditorFile(
                    auditor.getId(),
                    multipartFile.getOriginalFilename(),
                    file.getName(),
                    suffix,
                    file.getPath(),
                    type,
                    FileUtil.getSize(multipartFile.getSize()),
                    null
            );

            auditorFileRepository.save(auditorFile);

            // 触发审批机制，分两种情况：
            // 非审批状态下，将审核人员审批状态设为待审批
            // 待审批状态下，无需多余的处理，只待激活
            if (!auditor.getApprovalStatus().equals(CommonConstants.AUDIT_PLAN_STATUS_TO_ACTIVATED)) {
                auditor.setApprovalStatus(CommonConstants.AUDIT_PLAN_STATUS_TO_ACTIVATED);
                auditorRepository.save(auditor);
                // 创建伪审批任务
                // todo 非待审批，需要创建新审批任务给质量部Master
                List<PreTrail> list = preTrailRepository.findAllByStorageId(auditorId, CommonConstants.NOT_DEL);
                if (ValidationUtil.isNotEmpty(list)) {
                    // 仅取最新一条数据，理论上也只会有一条数据
                    PreTrail one = list.get(0);
                    // 创建审批任务
                    //创建审批任务给质量部Master
                    PreTrail preTrail = new PreTrail();
                    preTrail.setPreTrailNo(createNoFormat());
                    preTrail.setStorageId(auditor.getId());
                    preTrail.setSrcPath(one.getSrcPath());
                    preTrail.setTarPath(one.getTarPath());
                    preTrail.setSuffix(one.getSuffix());
                    preTrail.setVersion(one.getVersion());
                    preTrail.setSize(one.getSize());
                    preTrail.setType(CommonConstants.TRAIL_TYPE_AUDITOR);
                    preTrail.setRealName(one.getRealName());
                    preTrail.setChangeDesc("新建审核人员：[" + one.getRealName() + "]待批准");
                    preTrail.setIsDel(CommonConstants.IS_DEL);
                    // 指定审批人
                    preTrail.setApprovedBy(one.getApprovedBy());
                    preTrailRepository.save(preTrail);
                }
            }

        } catch (Exception e) {
            FileUtil.del(file);
            throw e;
        }
    }

    private String createNoFormat() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmssS");
        return StringUtils.getPinyin(SecurityUtils.getCurrentDeptName()) + "-" + "audit" + "-" + format.format(date);
    }

    @Override
    public List<AuditorFile> findByAuditorId(Long auditorId) {
        return auditorFileRepository.findByAuditorId(auditorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        // 删除校验:前端校验？
        auditorFileRepository.deleteAllByIdIn(ids);
    }
}
