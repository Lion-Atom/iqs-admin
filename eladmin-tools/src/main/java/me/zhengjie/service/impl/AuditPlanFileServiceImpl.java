package me.zhengjie.service.impl;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.config.FileProperties;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.AuditPlan;
import me.zhengjie.domain.AuditPlanFile;
import me.zhengjie.domain.PreTrail;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.AuditPlanRepository;
import me.zhengjie.repository.PlanFileRepository;
import me.zhengjie.repository.PreTrailRepository;
import me.zhengjie.service.AuditPlanFileService;
import me.zhengjie.utils.FileUtil;
import me.zhengjie.utils.SecurityUtils;
import me.zhengjie.utils.StringUtils;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/9/28 15:05
 */
@Service
@RequiredArgsConstructor
public class AuditPlanFileServiceImpl implements AuditPlanFileService {

    private final AuditPlanRepository auditPlanRepository;
    private final PlanFileRepository planFileRepository;
    private final FileProperties properties;
    private final PreTrailRepository preTrailRepository;

    @Override
    public List<AuditPlanFile> findByPlanIdAndTemplateId(Long planId, Long templateId) {
        List<AuditPlanFile> list = new ArrayList<>();
        if (templateId == 0L) {
            list = planFileRepository.findByPlanIdAndTemplateIdIsNull(planId);
        } else {
            list = planFileRepository.findByPlanIdAndTemplateId(planId, templateId);
        }
        return list;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadFile(Long planId, Long templateId, MultipartFile multipartFile) {
        if (templateId == 0L) {
            templateId = null;
        }
        // 查询问题判null
        AuditPlan plan = auditPlanRepository.findById(planId).orElseGet(AuditPlan::new);
        if (plan == null) {
            throw new BadRequestException("未查到该审核计划信息");
        }
        FileUtil.checkSize(properties.getMaxSize(), multipartFile.getSize());
        String suffix = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
        String type = FileUtil.getFileType(suffix);
        File file = FileUtil.upload(multipartFile, properties.getPath().getPath() + type + File.separator);
        if (ObjectUtil.isNull(file)) {
            throw new BadRequestException("上传失败");
        }
        try {

            AuditPlanFile planFile = new AuditPlanFile(
                    plan.getId(),
                    templateId,
                    multipartFile.getOriginalFilename(),
                    file.getName(),
                    suffix,
                    file.getPath(),
                    type,
                    FileUtil.getSize(multipartFile.getSize()),
                    null
            );

            planFileRepository.save(planFile);

            // 触发审批机制，分两种情况：
            // 非审批状态下，将审核人员审批状态设为待审批
            // 待审批状态下，无需多余的处理，只待激活
            if (!plan.getApprovalStatus().equals(CommonConstants.AUDIT_PLAN_STATUS_TO_ACTIVATED)) {
                plan.setApprovalStatus(CommonConstants.AUDIT_PLAN_STATUS_TO_ACTIVATED);
                plan.setApprovedTime(null);
                auditPlanRepository.save(plan);
                // 创建伪审批任务
                //  非待审批，需要创建新审批任务给质量部Master
                List<PreTrail> list = preTrailRepository.findAllByStorageId(planId, CommonConstants.NOT_DEL);
                if (ValidationUtil.isNotEmpty(list)) {
                    // 仅取最新一条数据，理论上也只会有一条数据
                    PreTrail one = list.get(0);
                    // 创建审批任务
                    //创建审批任务给指定的审批者
                    PreTrail preTrail = new PreTrail();
                    preTrail.setPreTrailNo(createNoFormat());
                    preTrail.setStorageId(planId);
                    preTrail.setSrcPath(one.getSrcPath());
                    preTrail.setTarPath(one.getTarPath());
                    preTrail.setSuffix(one.getSuffix());
                    preTrail.setVersion(one.getVersion());
                    preTrail.setSize(one.getSize());
                    preTrail.setType(CommonConstants.TRAIL_TYPE_AUDIT_PLAN);
                    preTrail.setRealName(one.getRealName());
                    preTrail.setChangeDesc("新建审核计划：[" + one.getRealName() + "]待批准");
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
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        // 删除校验:前端校验？
        planFileRepository.deleteAllByIdIn(ids);
    }
}
