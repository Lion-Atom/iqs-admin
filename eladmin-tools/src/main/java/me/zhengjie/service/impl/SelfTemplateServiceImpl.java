package me.zhengjie.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.PlanTemplate;
import me.zhengjie.domain.SelfTemplate;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.repository.PlanTemplateRepository;
import me.zhengjie.repository.SelfTemplateRepository;
import me.zhengjie.service.AuditPlanService;
import me.zhengjie.service.SelfTemplateService;
import me.zhengjie.service.dto.SelfTemplateDto;
import me.zhengjie.service.dto.SelfTemplateQueryDto;
import me.zhengjie.service.mapstruct.SelfTemplateMapper;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/11/25 9:39
 */
@Service
@RequiredArgsConstructor
public class SelfTemplateServiceImpl implements SelfTemplateService {

    private final SelfTemplateRepository selfTemplateRepository;
    private final SelfTemplateMapper selfTemplateMapper;
    private final AuditPlanService auditPlanService;
    private final PlanTemplateRepository planTemplateRepository;

    @Override
    public List<SelfTemplateDto> findByTemplateId(Long templateId) {
        List<SelfTemplateDto> list = new ArrayList<>();
        List<SelfTemplate> templates = selfTemplateRepository.findByTemplateId(templateId);
        if (ValidationUtil.isNotEmpty(templates)) {
            list = selfTemplateMapper.toDto(templates);
            list.forEach(dto -> {
                dto.setOldFlag(true);
                // 获取pItemName
                /*if (dto.getPid() != null) {
                    SelfTemplate parent = selfTemplateRepository.findById(dto.getPid()).orElseGet(SelfTemplate::new);
                    ValidationUtil.isNull(parent.getId(), "SelfTemplate", "id", dto.getPid());
                    dto.setPItemName(parent.getItemName());
                }*/
            });
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(SelfTemplateDto resources) {
        Long templateId = resources.getTemplateId();
        PlanTemplate template = planTemplateRepository.findById(templateId).orElseGet(PlanTemplate::new);
        ValidationUtil.isNull(template.getId(), "PlanTemplate", "id", templateId);
        // 判断是否具备修改审核计划权限
        auditPlanService.checkHasAuthExecute(template.getPlanId());

        SelfTemplate old;
        if (resources.getPid() != null) {
            old = selfTemplateRepository.findByItemNameAndPid(templateId, resources.getItemName(), resources.getPid());
        } else {
            old = selfTemplateRepository.findByItemNameAndPidIsNull(templateId, resources.getItemName());
        }
        if (resources.getOldFlag()) {
            if (old != null && !old.getId().equals(resources.getId())) {
                throw new EntityExistException(SelfTemplate.class, "itemName", resources.getItemName());
            }
            if (ValidationUtil.isNotEmpty(resources.getChildren())) {
                List<SelfTemplate> children = selfTemplateMapper.toEntity(resources.getChildren());
                children.forEach(child -> {
                    child.setPid(resources.getId());
                    child.setTemplateId(resources.getTemplateId());
                });
                selfTemplateRepository.saveAll(children);
            }
        } else {
            // 判重
            if (old != null) {
                throw new EntityExistException(SelfTemplate.class, "itemName", resources.getItemName());
            }
            SelfTemplate self = selfTemplateMapper.toEntity(resources);
            self.setId(null);
            SelfTemplate parent = selfTemplateRepository.save(self);
            if (ValidationUtil.isNotEmpty(resources.getChildren())) {
                List<SelfTemplate> children = selfTemplateMapper.toEntity(resources.getChildren());
                children.forEach(child -> {
                    child.setPid(parent.getId());
                    child.setTemplateId(resources.getTemplateId());
                });
                selfTemplateRepository.saveAll(children);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdate(List<SelfTemplate> resources) {
        Long templateId = resources.get(0).getTemplateId();
        PlanTemplate template = planTemplateRepository.findById(templateId).orElseGet(PlanTemplate::new);
        ValidationUtil.isNull(template.getId(), "PlanTemplate", "id", templateId);
        // 判断是否具备修改审核计划权限
        auditPlanService.checkHasAuthExecute(template.getPlanId());
        selfTemplateRepository.saveAll(resources);
    }

    @Override
    public List<SelfTemplate> findByExample(SelfTemplateQueryDto queryDto) {
        List<SelfTemplate> list = new ArrayList<>();
        SelfTemplate parent = selfTemplateRepository.findByTemplateIdAndId(queryDto.getId(), queryDto.getTemplateId());
        list.add(parent);
        if (parent != null) {
            list.addAll(selfTemplateRepository.findByPid(queryDto.getId()));
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        ids.forEach(id -> {
            Long templateId = selfTemplateRepository.findTemplateIdById(id);
            PlanTemplate template = planTemplateRepository.findById(templateId).orElseGet(PlanTemplate::new);
            ValidationUtil.isNull(template.getId(), "PlanTemplate", "id", templateId);
            // 判断是否具备修改审核计划权限
            auditPlanService.checkHasAuthExecute(template.getPlanId());
            ids.addAll(selfTemplateRepository.findChildIdByPid(id));
        });
        selfTemplateRepository.deleteAllByIdIn(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SelfTemplate resource) {
        Long templateId = resource.getTemplateId();
        PlanTemplate template = planTemplateRepository.findById(templateId).orElseGet(PlanTemplate::new);
        ValidationUtil.isNull(template.getId(), "PlanTemplate", "id", templateId);
        // 判断是否具备修改审核计划权限
        auditPlanService.checkHasAuthExecute(template.getPlanId());
        SelfTemplate self;
        // 根节点
        if (resource.getPid() == null) {
            self = selfTemplateRepository.findByItemNameAndPidIsNull(templateId, resource.getItemName());
        } else {
            self = selfTemplateRepository.findByItemNameAndPid(templateId, resource.getItemName(), resource.getPid());
        }
        if (self != null && !self.getId().equals(resource.getId())) {
            throw new EntityExistException(SelfTemplate.class, "itemName", resource.getItemName());
        }
        selfTemplateRepository.save(resource);
    }

    @Override
    public Object getTreeByTemplateId(Long templateId) {

        Set<SelfTemplateDto> trees = new HashSet<>();
//        List<SelfTemplate> roots = selfTemplateRepository.findByTemplateIdAndPidIsNull(templateId);
        List<SelfTemplate> all = selfTemplateRepository.findByTemplateId(templateId);
        if (ValidationUtil.isNotEmpty(all)) {
            trees = getTrees(selfTemplateMapper.toDto(all));
        }
        return trees;
    }

    @Override
    public SelfTemplateDto getById(Long id) {
        SelfTemplate selfTemplate = selfTemplateRepository.findById(id).orElseGet(SelfTemplate::new);
        ValidationUtil.isNull(selfTemplate.getId(), "SelfTemplate", "id", id);
        SelfTemplateDto dto = selfTemplateMapper.toDto(selfTemplate);
        dto.setOldFlag(true);
        if (dto.getPid() != null) {
            SelfTemplate parent = selfTemplateRepository.findById(dto.getPid()).orElseGet(SelfTemplate::new);
            ValidationUtil.isNull(parent.getId(), "SelfTemplate", "id", dto.getPid());
            dto.setPItemName(parent.getItemName());
        }
        return dto;
    }

    private Set<SelfTemplateDto> getTrees(List<SelfTemplateDto> list) {
        Set<SelfTemplateDto> trees = new LinkedHashSet<>();
        Set<SelfTemplateDto> temps = new LinkedHashSet<>();
        List<String> itemNames = list.stream().map(SelfTemplateDto::getItemName).collect(Collectors.toList());
        boolean isChild;
        for (SelfTemplateDto dto : list) {
            isChild = false;
            if (dto.getPid() == null) {
                trees.add(dto);
            }
            for (SelfTemplateDto it : list) {
                if (it.getPid() != null && dto.getId().equals(it.getPid())) {
                    isChild = true;
                    if (dto.getChildren() == null) {
                        dto.setChildren(new ArrayList<>());
                    }
                    // 获取pItemName
                   /* SelfTemplate parent = selfTemplateRepository.findById(it.getPid()).orElseGet(SelfTemplate::new);
                    ValidationUtil.isNull(parent.getId(), "SelfTemplate", "id", it.getPid());
                    it.setPItemName(parent.getItemName());*/
                    dto.getChildren().add(it);
                }
            }
            if (isChild) {
                temps.add(dto);
            } else if (dto.getPid() != null && !itemNames.contains(findById(dto.getPid()).getItemName())) {
                temps.add(dto);
            }
        }

        if (CollectionUtil.isEmpty(trees)) {
            trees = temps;
        }
        return trees;
    }

    private SelfTemplateDto findById(Long id) {
        SelfTemplate selfTemplate = selfTemplateRepository.findById(id).orElseGet(SelfTemplate::new);
        ValidationUtil.isNull(selfTemplate.getId(), "SelfTemplate", "id", id);
        return selfTemplateMapper.toDto(selfTemplate);
    }
}
