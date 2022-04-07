package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.*;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.*;
import me.zhengjie.service.IssueNumService;
import me.zhengjie.service.IssueNumService;
import me.zhengjie.service.TeamMemberService;
import me.zhengjie.service.dto.IssueNumDto;
import me.zhengjie.service.mapstruct.IssueNumMapper;
import me.zhengjie.utils.RedisUtils;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/23 10:14
 */

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/21 18:08
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "issueNum")
public class IssueNumServiceImpl implements IssueNumService {

    private final TimeManagementRepository timeMangeRepository;
    private final IssueRepository issueRepository;
    private final IssueNumRepository issueNumRepository;
    private final IssueNumMapper issueNumMapper;
    private final TeamMemberService teamMemberService;
    private final RedisUtils redisUtils;

    @Override
//    @Cacheable(key = "'issueId:' + #p0")
    public Map<String, Object> findByIssueId(Long issueId) {
        Map<String, Object> map = new HashMap<>();
        List<IssueNum> issueNums = issueNumRepository.findByIssueId(issueId);
        List<IssueNumDto> nums = issueNumMapper.toDto(issueNums);
        map.put("content", nums);
        return map;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(IssueNum resources) {
        teamMemberService.checkEditAuthorized(resources.getIssueId());
        Issue issue = issueRepository.findById(resources.getIssueId()).orElseGet(Issue::new);
        ValidationUtil.isNull(issue.getId(), "Issue", "id", resources.getIssueId());
        // 判断料号是否已存在
        List<IssueNum> members = issueNumRepository.findByIssueIdAndCaPartNum(issue.getId(), resources.getCaPartNum());
        if (ValidationUtil.isNotEmpty(members)) {
            throw new BadRequestException("This record has Existed!该料号已经记录，请勿重复添加！");
        }
        issueNumRepository.save(resources);

        // 修改问题状态为：进行中
        issue.setStatus(CommonConstants.D_STATUS_IN_PROCESS);
        issue.setCloseTime(null);
        // issue.setScore(null);
        issue.setDuration(null);
        issueRepository.save(issue);
        TimeManagement timeManagement = timeMangeRepository.findByIssueId(resources.getIssueId());
        // 此处是D2，后续步骤设置为false
        timeManagement.setD2Status(false);
        timeManagement.setD3Status(false);
        timeManagement.setD4Status(false);
        timeManagement.setD5Status(false);
        timeManagement.setD6Status(false);
        timeManagement.setD7Status(false);
        timeManagement.setD8Status(false);
        timeMangeRepository.save(timeManagement);
    }

    @Override
    @CacheEvict(key = "'issueId:' + #p0.issueId")
    @Transactional(rollbackFor = Exception.class)
    public void update(IssueNum resources) {
        // 查询问题判null
        teamMemberService.checkEditAuthorized(resources.getIssueId());
        Issue issue = issueRepository.findById(resources.getIssueId()).orElseGet(Issue::new);
        ValidationUtil.isNull(issue.getId(), "Issue", "id", resources.getIssueId());
        IssueNum num = issueNumRepository.findById(resources.getId()).orElseGet(IssueNum::new);
        // IssueNum members = IssueNumsRepository.findByIssueIdAndTeamIdAndUserId(resources.getIssueId(), resources.getTeamId(),resources.getUserId());
        // ValidationUtil.isNull(members.getIssueId(), "IssueNum", "issueId", resources.getIssueId());
        ValidationUtil.isNull(num.getId(), "IssueNum", "id", resources.getId());
        resources.setIssueId(num.getIssueId());
        issueNumRepository.save(resources);

        // 修改问题状态为：进行中
        issue.setStatus(CommonConstants.D_STATUS_IN_PROCESS);
        issue.setCloseTime(null);
        // issue.setScore(null);
        issue.setDuration(null);
        issueRepository.save(issue);
        TimeManagement timeManagement = timeMangeRepository.findByIssueId(resources.getIssueId());
        // 此处是D2，后续步骤设置为false
        timeManagement.setD2Status(false);
        timeManagement.setD3Status(false);
        timeManagement.setD4Status(false);
        timeManagement.setD5Status(false);
        timeManagement.setD6Status(false);
        timeManagement.setD7Status(false);
        timeManagement.setD8Status(false);
        timeMangeRepository.save(timeManagement);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {

        // 权限校验
        ids.forEach(teamMemberService::checkEditAuthorized);

        //修改问题状态
        List<Long> idList = new ArrayList<>(ids);
        IssueNum num = issueNumRepository.findIssueIdById(idList.get(0));
        Issue issue = issueRepository.findById(num.getIssueId()).orElseGet(Issue::new);

        // 修改问题状态为：进行中、清空关闭时间、时长等记录
        issue.setStatus(CommonConstants.D_STATUS_IN_PROCESS);
        issue.setCloseTime(null);
        // issue.setScore(null);
        issue.setDuration(null);
        issueRepository.save(issue);

        // 此处是D1，后续步骤设置为false
        TimeManagement timeManagement = timeMangeRepository.findByIssueId(issue.getId());
        timeManagement.setD2Status(false);
        timeManagement.setD3Status(false);
        timeManagement.setD4Status(false);
        timeManagement.setD5Status(false);
        timeManagement.setD6Status(false);
        timeManagement.setD7Status(false);
        timeManagement.setD8Status(false);
        timeMangeRepository.save(timeManagement);

        issueNumRepository.deleteAllByIdIn(ids);
    }
}
