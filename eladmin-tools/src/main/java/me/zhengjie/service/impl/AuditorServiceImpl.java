package me.zhengjie.service.impl;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.RequiredArgsConstructor;
import me.zhengjie.base.CommonDTO;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.*;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.*;
import me.zhengjie.service.AuditorService;
import me.zhengjie.service.dto.AuditorDto;
import me.zhengjie.service.dto.AuditorQueryCriteria;
import me.zhengjie.service.dto.AuditorQueryDto;
import me.zhengjie.service.mapstruct.AuditorMapper;
import me.zhengjie.utils.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author tmj
 * @version 1.0
 * @date 2021/9/7 10:39
 */
@Service
@RequiredArgsConstructor
public class AuditorServiceImpl implements AuditorService {

    private final AuditorRepository auditorRepository;
    private final AuditorMapper auditorMapper;
    private final PreTrailRepository preTrailRepository;
    private final ApproverRepository approverRepository;
    private final AuditPlanRepository auditPlanRepository;
    private final FileDeptRepository fileDeptRepository;
    private final CommonUtils commonUtils;
    private final AuditorFileRepository auditorFileRepository;

    @Override
    public AuditorDto findById(Long id) {
        Auditor auditor = auditorRepository.findById(id).orElseGet(Auditor::new);
        ValidationUtil.isNull(auditor.getId(), "Auditor", "id", id);
        // 或需要处理批准人信息、审核人员的部门等信息等--目前不需要
        AuditorDto dto = auditorMapper.toDto(auditor);
        // 获取审核人员的公司、部门信息
        Approver user = approverRepository.findById(auditor.getUserId()).orElseGet(Approver::new);
        ValidationUtil.isNull(user.getId(), "Approver", "id", auditor.getUserId());
        dto.setUsername(user.getUsername());
        getAuditorMore(dto, user);
        // 获取批准人姓名
        Approver approver = approverRepository.findById(dto.getApprovedBy()).orElseGet(Approver::new);
        ValidationUtil.isNull(approver.getId(), "Approver", "id", dto.getApprovedBy());
        if (approver.getFileDept() != null) {
            dto.setApprover(approver.getFileDept().getName() + " - " + approver.getUsername());
        }
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Auditor resources) {
        Long userId = resources.getUserId();
        // 人员重复校验
        List<Auditor> auditors = auditorRepository.findByUserIdAndSystem(userId, resources.getSystem());
        if (ValidationUtil.isNotEmpty(auditors)) {
            throw new BadRequestException("This auditor has Existed!【" + resources.getSystem() + "】体系，该成员信息已录入，请勿重复添加！");
        }
//        Long superiorId = SecurityUtils.getCurrentUserSuperior() == null ? commonUtils.getZlbMaster() : SecurityUtils.getCurrentUserSuperior();
        // 判断有效期
        if (resources.getNextCertificationTime() != null) {
            // 粗略计算，误差一天内
            Timestamp validLine = resources.getNextCertificationTime();
            Date now = new Date();
            if (validLine.getTime() > now.getTime()) {
                long diff = validLine.getTime() - now.getTime();
                int validTime = (int) (diff / (24 * 60 * 60 * 1000)) == 0 ? 1 : (int) (diff / (24 * 60 * 60 * 1000));
                if (validTime > 30 && 90 >= validTime) {
                    // 30~90天为有效
                    resources.setStatus(CommonConstants.AUDIT_PLAN_STATUS_VALID);
                } else if (30 >= validTime) {
                    // 30天内即将过期
                    resources.setStatus(CommonConstants.AUDITOR_STATUS_SOON_TO_EXPIRE);
                } else {
                    resources.setStatus(CommonConstants.AUDIT_PLAN_STATUS_VALID);
                }
                resources.setApprovalStatus(CommonConstants.AUDIT_PLAN_STATUS_TO_ACTIVATED);
            } else {
                resources.setStatus(CommonConstants.AUDITOR_STATUS_EXPIRE);
                resources.setApprovalStatus(CommonConstants.AUDIT_PLAN_STATUS_UNABLE_ACTIVATED);
            }
        }
        resources.setApprovedBy(commonUtils.getSuperiorId());
        Auditor newAuditor = auditorRepository.save(resources);

        // 创建审批任务
        //创建审批任务给质量部Master
        PreTrail preTrail = new PreTrail();
        preTrail.setPreTrailNo(createNoFormat());
        preTrail.setStorageId(newAuditor.getId());
        preTrail.setSrcPath(String.valueOf(newAuditor.getCertificationTime()));
        preTrail.setTarPath(String.valueOf(newAuditor.getNextCertificationTime()));
        preTrail.setSuffix(newAuditor.getCertificationUnit());
        preTrail.setVersion(newAuditor.getSystem());
        preTrail.setSize(newAuditor.getValidity() + "年");
        preTrail.setType(CommonConstants.TRAIL_TYPE_AUDITOR);
        preTrail.setRealName(getAuditorName(newAuditor.getUserId()));
        preTrail.setChangeDesc("新建审核人员：[" + getAuditorName(newAuditor.getUserId()) + "]待批准");
        preTrail.setIsDel(CommonConstants.IS_DEL);
        // 指定审批人
        preTrail.setApprovedBy(commonUtils.getSuperiorId());
        preTrailRepository.save(preTrail);
    }

    private String getAuditorName(Long userId) {
        Approver approver = approverRepository.findById(userId).orElseGet(Approver::new);
        ValidationUtil.isNull(approver.getId(), "Approver", "id", userId);
        // 添加用户信息
        StringBuilder sb = new StringBuilder();
        if (approver.getFileDept() != null) {
            FileDept dept = approver.getFileDept();
            sb.append(dept.getName());
        }
        sb.append("-");
        sb.append(approver.getUsername());
        return sb.toString();
    }

    private String createNoFormat() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmssS");
        return StringUtils.getPinyin(SecurityUtils.getCurrentDeptName()) + "-" + "audit" + "-" + format.format(date);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Auditor resources) {
        Long auditorId = resources.getId();
        Auditor auditor = auditorRepository.findById(auditorId).orElseGet(Auditor::new);
        ValidationUtil.isNull(auditor.getId(), "Auditor", "id", resources.getId());
        // 目前设为生效已审批就不要再更新了
        if (auditor.getApprovalStatus().equals(CommonConstants.AUDIT_PLAN_STATUS_APPROVED) &&
                auditor.getStatus().equals(CommonConstants.AUDIT_PLAN_STATUS_VALID)) {
            throw new BadRequestException("Status Valid!当前审核人员处于生效期，无需更新！");
        }

        // 若是‘被驳回’则修改后自动改为待激活
        if (resources.getApprovalStatus().equals(CommonConstants.AUDIT_PLAN_STATUS_REFUSED)) {
            resources.setApprovalStatus(CommonConstants.AUDIT_PLAN_STATUS_TO_ACTIVATED);
            //创建审批任务给质量部Master
            PreTrail preTrail = new PreTrail();
            preTrail.setPreTrailNo(createNoFormat());
            preTrail.setStorageId(resources.getId());
            preTrail.setSrcPath(String.valueOf(resources.getCertificationTime()));
            preTrail.setTarPath(String.valueOf(resources.getNextCertificationTime()));
            preTrail.setSuffix(resources.getCertificationUnit());
            preTrail.setVersion(resources.getSystem());
            preTrail.setSize(resources.getValidity() + "年");
            preTrail.setType(CommonConstants.TRAIL_TYPE_AUDITOR);
            preTrail.setRealName(getAuditorName(resources.getUserId()));
            preTrail.setChangeDesc("审核人员：[" + getAuditorName(resources.getUserId()) + "]被驳回后重新发起审批");
            preTrail.setIsDel(CommonConstants.IS_DEL);
            // 指定审批人
            preTrail.setApprovedBy(commonUtils.getSuperiorId());
            preTrailRepository.save(preTrail);
        }
        // 若是已过期或者即将过期则重新待激活
        if (resources.getStatus().equals(CommonConstants.AUDITOR_STATUS_SOON_TO_EXPIRE) ||
                resources.getStatus().equals(CommonConstants.AUDITOR_STATUS_EXPIRE)) {
            // 任务信息删除
            PreTrail task = preTrailRepository.findTaskByStorageId(auditorId, CommonConstants.TRAIL_TYPE_AUDITOR, CommonConstants.IS_DEL, false);
            if (task != null) {
                preTrailRepository.delByStorageIdAndIsDel(auditorId, CommonConstants.TRAIL_TYPE_AUDITOR, CommonConstants.IS_DEL, false);
            }
            resources.setApprovalStatus(CommonConstants.AUDIT_PLAN_STATUS_TO_ACTIVATED);
            //创建审批任务给质量部Master
            PreTrail preTrail = new PreTrail();
            preTrail.setPreTrailNo(createNoFormat());
            preTrail.setStorageId(resources.getId());
            preTrail.setSrcPath(String.valueOf(resources.getCertificationTime()));
            preTrail.setTarPath(String.valueOf(resources.getNextCertificationTime()));
            preTrail.setSuffix(resources.getCertificationUnit());
            preTrail.setVersion(resources.getSystem());
            preTrail.setSize(resources.getValidity() + "年");
            preTrail.setType(CommonConstants.TRAIL_TYPE_AUDITOR);
            preTrail.setRealName(getAuditorName(resources.getUserId()));
            preTrail.setChangeDesc("审核人员：[" + getAuditorName(resources.getUserId()) + "]被驳回后重新发起审批");
            preTrail.setIsDel(CommonConstants.IS_DEL);
            // 指定审批人
            preTrail.setApprovedBy(commonUtils.getSuperiorId());
            preTrailRepository.save(preTrail);
        }

        // 判断有效期是否要更新
        if (!resources.getNextCertificationTime().equals(auditor.getNextCertificationTime())) {
            // 粗略计算，误差一天内
            Timestamp validLine = resources.getNextCertificationTime();
            Date now = new Date();
            if (validLine.getTime() > now.getTime()) {
                long diff = validLine.getTime() - now.getTime();
                int validTime = (int) (diff / (24 * 60 * 60 * 1000)) == 0 ? 1 : (int) (diff / (24 * 60 * 60 * 1000));
                if (validTime > CommonConstants.AUDIT_DAYS_MONTH && CommonConstants.AUDIT_DAYS_SEASON >= validTime) {
                    // 30~90天为有效
                    resources.setStatus(CommonConstants.AUDIT_PLAN_STATUS_VALID);
                } else if (CommonConstants.AUDIT_DAYS_MONTH >= validTime) {
                    // 30天内即将过期
                    resources.setStatus(CommonConstants.AUDITOR_STATUS_SOON_TO_EXPIRE);
                } else {
                    resources.setStatus(CommonConstants.AUDIT_PLAN_STATUS_VALID);
                }
                // resources.setApprovalStatus(CommonConstants.AUDIT_PLAN_STATUS_TO_ACTIVATED);
            } else {
                // 前端已限制，原则上不会出现
                throw new BadRequestException("Status Expire Existed!设置的不合理的时间已过期，请重新设置！");
//                resources.setStatus(CommonConstants.AUDITOR_STATUS_EXPIRE);
//                resources.setApprovalStatus(CommonConstants.AUDIT_PLAN_STATUS_UNABLE_ACTIVATED);
            }
        }
        // 是否要限制审批中不可修改？已批准的是否需要再发起新一轮的审批
        PreTrail task = preTrailRepository.findTaskByStorageId(auditorId, CommonConstants.TRAIL_TYPE_AUDITOR, CommonConstants.NOT_DEL, false);
        if (task != null) {
            throw new BadRequestException("Task has Existed!人员处在审核流程中，不可重复发起审批！");
        }
        // 有效可用状态下不可轻易修改
        auditorRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        // 删除相关的审批任务
        preTrailRepository.physicalDeleteAllByStorageIdIn(ids);
        auditorRepository.deleteAllByIdIn(ids);
        // JPA自动删除计划审核人关联信息，此处无需处理
        // todo 是否允许删除？？？
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> queryAll(AuditorQueryCriteria criteria, Pageable pageable) {
        Page<Auditor> page = auditorRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);

        List<AuditorDto> list = new ArrayList<>();
        long total = 0L;
        if (ValidationUtil.isNotEmpty(page.getContent())) {
            list = auditorMapper.toDto(page.getContent());
            list.forEach(dto -> {
                // 获取审核人员的公司、部门信息
                Approver user = approverRepository.findById(dto.getUserId()).orElseGet(Approver::new);
                ValidationUtil.isNull(user.getId(), "Approver", "id", dto.getUserId());
                dto.setUsername(user.getUsername());
                getAuditorMore(dto, user);
                // 获取批准人姓名
                Approver approver = approverRepository.findById(dto.getApprovedBy()).orElseGet(Approver::new);
                ValidationUtil.isNull(approver.getId(), "Approver", "id", dto.getApprovedBy());
                dto.setApprover(approver.getUsername());
                // 获取当前有效期样式:1.三个月-黄色 2.一个月-红色 3.大于三个月，正常显示
                initStyleTypeByNextCertificationTime(dto);
                // 获取相关附件
                List<AuditorFile> files = auditorFileRepository.findByAuditorId(dto.getId());
                dto.setAuditorFiles(files);
            });
            total = page.getTotalElements();
        }
        Map<String, Object> map = new LinkedHashMap<>(2);
        map.put("content", list);
        map.put("totalElements", total);
        return map;
    }

    private void getAuditorMore(AuditorDto dto, Approver user) {
        if (user.getFileDept() != null) {
            FileDept dept = user.getFileDept();
            dto.setDeptName(dept.getName());
            //  todo查找所在公司（顶级部门）
            if (dept.getPid() != null) {
                Long pid = dept.getPid();
                String topName;
                do {
                    FileDept pDept = fileDeptRepository.findById(pid).orElseGet(FileDept::new);
                    pid = pDept.getPid();
                    topName = pDept.getName();
                } while (pid != null);
                dto.setCompanyName(topName);
            } else {
                dto.setCompanyName(user.getFileDept().getName());
            }
        }
    }

    private void initStyleTypeByNextCertificationTime(AuditorDto dto) {
        if (dto.getNextCertificationTime() != null) {
            // 粗略计算，误差一天内
            Timestamp validLine = dto.getNextCertificationTime();
            Date now = new Date();
            if (validLine.getTime() > now.getTime()) {
                long diff = validLine.getTime() - now.getTime();
                int closeDuration = (int) (diff / (24 * 60 * 60 * 1000)) == 0 ? 1 : (int) (diff / (24 * 60 * 60 * 1000));
                if (closeDuration > CommonConstants.AUDIT_DAYS_MONTH && CommonConstants.AUDIT_DAYS_SEASON >= closeDuration) {
                    dto.setStyleType("warn");
                } else if (CommonConstants.AUDIT_DAYS_MONTH >= closeDuration) {
                    dto.setStyleType("alert");
                } else {
                    dto.setStyleType("primary");
                }
            } else {
                dto.setStyleType("gray");
                // todo 定时任务-走查是否有审核人员有效期过期
                //  dto.setStatus(CommonConstants.AUDITOR_STATUS_EXPIRE);
            }
        }
    }

    @Override
    public List<AuditorDto> queryAll(AuditorQueryCriteria criteria) {
        List<Auditor> auditorList = auditorRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
        List<AuditorDto> list = new ArrayList<>();
        if (ValidationUtil.isNotEmpty(auditorList)) {
            list = auditorMapper.toDto(auditorList);
            list.forEach(dto -> {
                // 获取审核人员的公司、部门信息
                Approver user = approverRepository.findById(dto.getUserId()).orElseGet(Approver::new);
                ValidationUtil.isNull(user.getId(), "Approver", "id", dto.getUserId());
                dto.setUsername(user.getUsername());
                getAuditorMore(dto, user);
                // 获取批准人姓名
                Approver approver = approverRepository.findById(dto.getApprovedBy()).orElseGet(Approver::new);
                ValidationUtil.isNull(approver.getId(), "Approver", "id", dto.getApprovedBy());
                dto.setApprover(approver.getUsername());
            });
        }
        return list;
    }


    @Override
    public List<AuditorDto> findByExample(AuditorQueryDto queryDto) {
        List<Auditor> auditorList = auditorRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, queryDto, criteriaBuilder));
        List<AuditorDto> list = new ArrayList<>();
        if (ValidationUtil.isNotEmpty(auditorList)) {
            list = auditorMapper.toDto(auditorList);
            list.forEach(dto -> {
                // 获取审核人员的公司、部门信息
                Approver user = approverRepository.findById(dto.getUserId()).orElseGet(Approver::new);
                ValidationUtil.isNull(user.getId(), "Approver", "id", dto.getUserId());
                dto.setUsername(user.getUsername());
                getAuditorMore(dto, user);
            });
        }
        return list;
    }

    @Override
    public Map<String, Object> queryAuditorsByStatus() {
        Map<String, Object> map = new HashMap<>();

        List<CommonDTO> list = new ArrayList<>();
        CommonConstants.AUDITOR_STATUS_LIST.forEach(status -> {
            int count = 0;
            count = auditorRepository.getCountByStatus(status);
            CommonDTO commonDTO = new CommonDTO();
            commonDTO.setId(null);
            commonDTO.setName(status);
            commonDTO.setValue(String.valueOf(count));
            list.add(commonDTO);
        });
        int totalCount = auditorRepository.getAuditorCount();
        map.put("content", totalCount);
        map.put("totalElements", list);
        map.put("scope", "All");

        return map;
    }

    @Override
    public Map<String, Object> queryAuditorsByDept() {
        Map<String, Object> map = new HashMap<>();
        // 获取审核人员部门集合
        List<Auditor> auditors = auditorRepository.findAll();
        if (ValidationUtil.isNotEmpty(auditors)) {
            List<String> xAxisList = new ArrayList<>();
            Map<Long, String> deptMap = new HashMap<>();
            List<CommonDTO> yAxisList = new ArrayList<>();
            List<Long> userIds = new ArrayList<>();
            auditors.forEach(auditor -> {
                userIds.add(auditor.getUserId());
            });
            List<Approver> users = approverRepository.findByIdIn(userIds);
            if (ValidationUtil.isNotEmpty(users)) {
                users.forEach(user -> {
                    deptMap.put(user.getId(), user.getFileDept().getName());
                });
            }

            Multimap<String, Long> multiMap = HashMultimap.create();
            for (Map.Entry<Long, String> entry : deptMap.entrySet()) {
                multiMap.put(entry.getValue(), entry.getKey());
            }

            for (Map.Entry<String, Collection<Long>> entry : multiMap.asMap().entrySet()) {
                xAxisList.add(entry.getKey());
                CommonDTO yAxis = new CommonDTO();
                yAxis.setName(entry.getKey());
                yAxis.setValue(String.valueOf(entry.getValue().size()));

                yAxisList.add(yAxis);
            }
            map.put("xAxis", xAxisList);
            map.put("yAxis", yAxisList);
            map.put("scope", "全部");
        }
        return map;
    }

    @Override
    public List<Auditor> findByUserId(Long userId) {
        return auditorRepository.findByUserId(userId);
    }

    @Override
    public void download(List<AuditorDto> auditorDtos, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (AuditorDto auditorDto : auditorDtos) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("审核人员姓名", auditorDto.getUsername());
            map.put("公司", auditorDto.getCompanyName());
            map.put("部门", auditorDto.getDeptName());
            map.put("体系", auditorDto.getSystem());
            map.put("状态", auditorDto.getStatus());
            map.put("认证时间", auditorDto.getCertificationTime());
            map.put("有效期限", auditorDto.getValidity());
            map.put("下一次认证时间", auditorDto.getNextCertificationTime());
            map.put("认证单位", auditorDto.getCertificationUnit());
            map.put("批准人", auditorDto.getApprover());
            map.put("批准时间", auditorDto.getApprovedTime());
            map.put("创建日期", auditorDto.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public void verification(Set<Long> ids) {
        // 判断有无在审核计划中起作用的审核员
        for (Long id : ids) {
            Auditor auditor = auditorRepository.findById(id).orElseGet(Auditor::new);
            ValidationUtil.isNull(auditor.getId(), "Auditor", "id", id);
            if (ValidationUtil.isNotEmpty(Collections.singletonList(auditor.getPlans()))) {
                throw new BadRequestException("所选审核人员目前参与审核计划中，请解除后再试！");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activatedById(Long auditorId) {
        Auditor auditor = auditorRepository.findById(auditorId).orElseGet(Auditor::new);
        ValidationUtil.isNull(auditor.getId(), "Auditor", "id", auditorId);
        PreTrail task = preTrailRepository.findTaskByStorageId(auditorId, CommonConstants.TRAIL_TYPE_AUDITOR, CommonConstants.IS_DEL, false);
        if (task != null) {
            // 仅取最新一条数据，理论上也只会有一条数据
//            Long superiorId = SecurityUtils.getCurrentUserSuperior() == null ? commonUtils.getZlbMaster() : SecurityUtils.getCurrentUserSuperior();
            task.setRealName(getAuditorName(auditor.getUserId()));
            task.setChangeDesc("新建审核人员：[" + getAuditorName(auditor.getUserId()) + "]待批准");
            task.setIsDel(CommonConstants.NOT_DEL);
//            task.setApprovedBy(superiorId);
            preTrailRepository.save(task);
            // 变更审核人员的审核状态
            auditor.setApprovalStatus(CommonConstants.AUDIT_PLAN_STATUS_WAIT);
            auditorRepository.save(auditor);
        } else {
            throw new BadRequestException("This auditor no Task Existed!未查到任务信息！");
        }
    }
}
