package me.zhengjie.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.Issue;
import me.zhengjie.domain.IssueCause;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.repository.IssueCauseRepository;
import me.zhengjie.repository.IssueRepository;
import me.zhengjie.repository.WhyRepository;
import me.zhengjie.service.IssueCauseService;
import me.zhengjie.service.TeamMemberService;
import me.zhengjie.service.dto.*;
import me.zhengjie.service.dto.IssueCauseDto;
import me.zhengjie.service.mapstruct.FishBoneMapper;
import me.zhengjie.service.mapstruct.IssueCauseMapper;
import me.zhengjie.utils.QueryHelp;
import me.zhengjie.utils.SecurityUtils;
import me.zhengjie.utils.StringUtils;
import me.zhengjie.utils.ValidationUtil;
import me.zhengjie.utils.enums.DataScopeEnum;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/28 17:46
 */
@Service
@RequiredArgsConstructor
public class IssueCauseServiceImpl implements IssueCauseService {

    private final IssueRepository issueRepository;
    private final TeamMemberService teamMemberService;
    private final IssueCauseRepository issueCauseRepository;
    private final IssueCauseMapper issueCauseMapper;
    private final WhyRepository whyRepository;
    private final FishBoneMapper fishBoneMapper;

    @Override
    public List<IssueCauseDto> queryAll(IssueCauseQueryCriteria criteria, Boolean isQuery) throws Exception {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        String dataScopeType = SecurityUtils.getDataScopeType();
        if (isQuery) {
            if (dataScopeType.equals(DataScopeEnum.ALL.getValue())) {
                criteria.setPidIsNull(true);
            }
            List<Field> fields = QueryHelp.getAllFields(criteria.getClass(), new ArrayList<>());
            List<String> fieldNames = new ArrayList<String>() {{
                add("pidIsNull");
            }};
            for (Field field : fields) {
                //设置对象的访问权限，保证对private的属性的访问
                field.setAccessible(true);
                Object val = field.get(criteria);
                if (fieldNames.contains(field.getName())) {
                    continue;
                }
                if (ObjectUtil.isNotNull(val)) {
                    criteria.setPidIsNull(null);
                    break;
                }
            }
        }
        List<IssueCauseDto> list = issueCauseMapper.toDto(issueCauseRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), sort));
        // 如果为空，就代表为自定义权限或者本级权限，就需要去重，不理解可以注释掉，看查询结果
        if (StringUtils.isBlank(dataScopeType)) {
            return deduplication(list);
        }
        return list;
    }

    private List<IssueCauseDto> deduplication(List<IssueCauseDto> list) {
        List<IssueCauseDto> causeDtos = new ArrayList<>();
        for (IssueCauseDto causeDto : list) {
            boolean flag = true;
            for (IssueCauseDto dto : list) {
                if (dto.getId().equals(causeDto.getPid())) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                causeDtos.add(causeDto);
            }
        }
        return causeDtos;
    }

    @Override
    public IssueCauseDto findById(Long id) {
        IssueCause cause = issueCauseRepository.findById(id).orElseGet(IssueCause::new);
        ValidationUtil.isNull(cause.getId(), "IssueCause", "id", id);
        return issueCauseMapper.toDto(cause);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(IssueCause resources) {

        Long issueId = resources.getIssueId();
        teamMemberService.checkEditAuthorized(issueId);

        //重名校验
        IssueCause cause = issueCauseRepository.findByName(resources.getName());
        if (cause != null) {
            throw new EntityExistException(IssueCause.class, "name", resources.getName());
        }
        //新增时候的原因占比校验
        Double nowPer = resources.getContribution();
        Long oldPid = resources.getPid();
        Long pid = resources.getPid();
        if (nowPer > 100 || nowPer < 0) {
            throw new BadRequestException("contribution should not over 100%!百分比应介于0-100之间");
        }
        if (oldPid != null) {
            // 查询所有上级
            Long topId;
            do {
                IssueCause pCause = issueCauseRepository.findById(oldPid).orElseGet(IssueCause::new);
                oldPid = pCause.getPid();
                topId = pCause.getId();
            } while (oldPid != null);
            Double oldCount = issueCauseRepository.getCountByPidIsNull(issueId, topId);
            Double broCount = issueCauseRepository.getContributionSumByPId(pid);
            Double maxPer = 100 - (oldCount == null ? 0 : oldCount) - (broCount == null ? 0 : broCount);

            // 获取节点的总值
            if (nowPer > maxPer) {
                throw new BadRequestException("contribution should not over 100%!百分比应介于0-" + maxPer + "之间");
            } else {
//                List<Long> pidList = new ArrayList<>();
//                pidList.add(pid);
                // 查询所有上级
                do {
                    Double pPer;
                    IssueCause pCause = issueCauseRepository.findById(pid).orElseGet(IssueCause::new);
                    Double broPer = issueCauseRepository.getContributionSumByPId(pid);
                    if (pid.equals(resources.getPid())) {
                        if (broPer != null) {
                            pPer = broPer + nowPer;
                        } else {
                            pPer = nowPer;
                        }
                    } else {
                        pPer = broPer;
                    }
                    // 更改为不是“根本原因”并重置百分比
                    issueCauseRepository.updateContribution(pid, pPer);
                    // todo 或考虑暂时不删除5whys,当前先注释删除
                    // whyRepository.deleteByCauseId(pCause.getId());
                    pid = pCause.getPid();

                } while (pid != null);

            }
        }
        // 计算子节点数目
        resources.setSubCount(0);

        issueCauseRepository.save(resources);
        // 变更上级节点的子节点数目
        updateSubCnt(resources.getPid());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(IssueCause resources) {
        Long issueId = resources.getIssueId();
        Long id = resources.getId();
        teamMemberService.checkEditAuthorized(issueId);
        if (resources.getPid() != null && id.equals(resources.getPid())) {
            throw new BadRequestException("上级不能为自己");
        }

        IssueCause cause = issueCauseRepository.findById(id).orElseGet(IssueCause::new);
        // 重名校验
        IssueCause old = issueCauseRepository.findByName(resources.getName());
        if (old != null && !old.getId().equals(resources.getId())) {
            throw new EntityExistException(IssueCause.class, "name", resources.getName());
        }
        // 根本原因切换提示
        ValidationUtil.isNull(cause.getId(), "IssueCause", "id", id);
        resources.setId(cause.getId());

        // 编辑-原因占比校验
        Double nowPer = resources.getContribution();
        Long oldPid = resources.getPid();
        Long pid = resources.getPid();
        if (nowPer > 100 || nowPer < 0) {
            throw new BadRequestException("contribution should not over 100%!百分比应介于0-100之间");
        }
        if (oldPid != null) {
            // 查询所有上级
            Long topId;
            do {
                IssueCause pCause = issueCauseRepository.findById(oldPid).orElseGet(IssueCause::new);
                oldPid = pCause.getPid();
                topId = pCause.getId();
            } while (oldPid != null);
            Double oldCount = issueCauseRepository.getCountByPidIsNull(issueId, topId);
            Double broCount = issueCauseRepository.getBroContributionSumByPId(pid, id);
            Double maxPer = 100 - (oldCount == null ? 0 : oldCount) - (broCount == null ? 0 : broCount);
            Double min = issueCauseRepository.getContributionSumByPId(id);
            Double minPer = min == null ? 0d : min;
            // 获取节点的总值
            if (nowPer > maxPer || nowPer < minPer) {
                throw new BadRequestException("contribution should not over 100%!百分比应介于" +
                        minPer + "-" + maxPer + "之间");
            } else {
//                List<Long> pidList = new ArrayList<>();
//                pidList.add(pid);
                // 查询所有上级
                do {
                    Double pPer;
                    IssueCause pCause = issueCauseRepository.findById(pid).orElseGet(IssueCause::new);
                    Double broPer = issueCauseRepository.getBroContributionSumByPId(pid, id);
                    if (pid.equals(resources.getPid())) {
                        if (broPer != null) {
                            pPer = broPer + nowPer;
                        } else {
                            pPer = nowPer;
                        }
                    } else {
                        pPer = broPer;
                    }
                    // 更改为不是“根本原因”并重置百分比
                    issueCauseRepository.updateContribution(pid, pPer);
                    // todo 或考虑暂时不删除5whys,当前先注释删除
                    // whyRepository.deleteByCauseId(pCause.getId());
                    pid = pCause.getPid();

                } while (pid != null);

            }
        }

        issueCauseRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<IssueCauseDto> issueCauseDtos) {
        for (IssueCauseDto causeDto : issueCauseDtos) {
            Long pid = causeDto.getPid();
            teamMemberService.checkEditAuthorized(causeDto.getIssueId());
            issueCauseRepository.deleteById(causeDto.getId());
            // 更新父节点的原因占比
            Double broPer = issueCauseRepository.getContributionSumByPId(pid);
            if (broPer == null) {
                broPer = 0d;
            }
            issueCauseRepository.updateContribution(pid, broPer);
            updateSubCnt(pid);
        }
    }

    private void updateSubCnt(Long pid) {
        if (pid != null) {
            // 变更子节点数目
            int count = issueCauseRepository.countByPid(pid);
            issueCauseRepository.updateSubCntById(count, pid, false);
        }
    }

    @Override
    public List<IssueCause> findByPid(long pid) {
        return issueCauseRepository.findByPid(pid);
    }

    @Override
    public Set<IssueCauseDto> getDeleteIssueCauses(List<IssueCause> issueCauseList, Set<IssueCauseDto> issueCauseDtos) {
        for (IssueCause cause : issueCauseList) {
            teamMemberService.checkEditAuthorized(cause.getIssueId());
            issueCauseDtos.add(issueCauseMapper.toDto(cause));
            List<IssueCause> causeList = issueCauseRepository.findByPid(cause.getId());
            if (ValidationUtil.isNotEmpty(causeList)) {
                getDeleteIssueCauses(causeList, issueCauseDtos);
            }
        }
        return issueCauseDtos;
    }


    @Override
    public List<IssueCauseDto> getSuperior(IssueCauseDto issueCauseDto, List<IssueCause> issueCauses) {
        if (issueCauseDto.getPid() == null) {
            issueCauses.addAll(issueCauseRepository.findByPidIsNull());
            return issueCauseMapper.toDto(issueCauses);
        }
        issueCauses.addAll(issueCauseRepository.findByPid(issueCauseDto.getPid()));
        return getSuperior(findById(issueCauseDto.getPid()), issueCauses);
    }

    @Override
    public Object buildTree(List<IssueCauseDto> issueCauseDtos) {
        Set<IssueCauseDto> trees = getTrees(issueCauseDtos);
        Map<String, Object> map = new HashMap<>(2);
        map.put("totalElements", issueCauseDtos.size());
        map.put("content", CollectionUtil.isEmpty(trees) ? issueCauseDtos : trees);
        return map;
    }

    private Set<IssueCauseDto> getTrees(List<IssueCauseDto> issueCauseDtos) {
        Set<IssueCauseDto> trees = new LinkedHashSet<>();
        Set<IssueCauseDto> depts = new LinkedHashSet<>();
        List<String> causeNames = issueCauseDtos.stream().map(IssueCauseDto::getName).collect(Collectors.toList());
        boolean isChild;
        for (IssueCauseDto issueCauseDto : issueCauseDtos) {
            isChild = false;
            if (issueCauseDto.getPid() == null) {
                trees.add(issueCauseDto);
            }
            for (IssueCauseDto it : issueCauseDtos) {
                if (it.getPid() != null && issueCauseDto.getId().equals(it.getPid())) {
                    isChild = true;
                    if (issueCauseDto.getChildren() == null) {
                        issueCauseDto.setChildren(new ArrayList<>());
                    }
                    issueCauseDto.getChildren().add(it);
                }
            }
            if (isChild) {
                depts.add(issueCauseDto);
            } else if (issueCauseDto.getPid() != null && !causeNames.contains(findById(issueCauseDto.getPid()).getName())) {
                depts.add(issueCauseDto);
            }
        }

        if (CollectionUtil.isEmpty(trees)) {
            trees = depts;
        }
        return trees;
    }

    @Override
    public List<IssueCauseDto> findByExample(IssueCauseQueryDto queryDto) {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        return issueCauseMapper.toDto(issueCauseRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, queryDto, criteriaBuilder), sort));
    }

    @Override
    public List<IssueCauseDto> findByIssueId(Long issueId) {
        /*List<IssueCauseDto> list = new ArrayList<>();
        List<IssueCause> causeList = issueCauseRepository.findByIssueIdAndPidIsNull(issueId);

        if (ValidationUtil.isNotEmpty(causeList)) {
            list = issueCauseMapper.toDto(causeList);
            list.forEach(cause -> {

            });
        }*/

        return issueCauseMapper.toDto(issueCauseRepository.findByIssueIdAndPidIsNull(issueId));
    }

    @Override
    public Object createTree(Long issueId) {
        Issue issue = issueRepository.findById(issueId).orElseGet(Issue::new);
        ValidationUtil.isNull(issue.getId(), "Issue", "id", issueId);
        List<IssueCauseDto> causeDtos = issueCauseMapper.toDto(issueCauseRepository.findByIssueId(issueId));
        if (ValidationUtil.isNotEmpty(causeDtos)) {
            causeDtos.forEach(issueCauseDto -> {
                String contribution;
                if (issueCauseDto.getContribution() == 0d) {
                    contribution = "0%";
                } else {
                    contribution = issueCauseDto.getContribution()+"%";
                }
                issueCauseDto.setName(issueCauseDto.getName() + "[" + contribution + "]");
            });
        }
        Set<IssueCauseDto> trees = getTrees(causeDtos);
        // List<FishBoneDto> list = fishBoneMapper.toDto(new ArrayList<>(trees));
        Map<String, Object> map = new HashMap<>(2);
        map.put("issueTitle", issue.getIssueTitle());
        map.put("content", fishBoneMapper.toDto(CollectionUtil.isEmpty(trees) ? causeDtos : new ArrayList<>(trees)));
        return map;
    }
}
