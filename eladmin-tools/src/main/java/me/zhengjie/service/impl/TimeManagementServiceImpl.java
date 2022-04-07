package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.Issue;
import me.zhengjie.domain.TimeManagement;
import me.zhengjie.repository.IssueRepository;
import me.zhengjie.repository.TimeManagementRepository;
import me.zhengjie.service.TeamMemberService;
import me.zhengjie.service.TimeManagementService;
import me.zhengjie.service.dto.TimeManagementDto;
import me.zhengjie.service.mapstruct.TimeManageMapper;
import me.zhengjie.utils.RedisUtils;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;

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
@CacheConfig(cacheNames = "timeManagement")
public class TimeManagementServiceImpl implements TimeManagementService {

    private final TeamMemberService teamMemberService;
    private final TimeManagementRepository timeMangeRepository;
    private final TimeManageMapper timeManageMapper;
    private final IssueRepository issueRepository;
    private final RedisUtils redisUtils;

    @Override
//    @Cacheable(key = "'issueId:' + #p0")
    public TimeManagementDto findByIssueId(Long issueId) {
        TimeManagementDto timeManagementDto = new TimeManagementDto();
        TimeManagement timeManagement = timeMangeRepository.findByIssueId(issueId);
        if (timeManagement != null) {
            timeManagementDto = timeManageMapper.toDto(timeManagement);
        }
        return timeManagementDto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(TimeManagement resources) {
        Issue issue = issueRepository.findById(resources.getIssueId()).orElseGet(Issue::new);
        ValidationUtil.isNull(issue.getId(), "Issue", "id", resources.getIssueId());
        timeMangeRepository.save(resources);
        // 修改问题状态为：进行中
        issue.setStatus(CommonConstants.D_STATUS_IN_PROCESS);
        issueRepository.save(issue);
    }

    @Override
    @CacheEvict(key = "'issueId:' + #p0.issueId")
    @Transactional(rollbackFor = Exception.class)
    public void update(TimeManagementDto resources) {
        // 权限判断
        teamMemberService.checkEditAuthorized(resources.getIssueId());
        // 查询问题判null
        Issue issue = issueRepository.findById(resources.getIssueId()).orElseGet(Issue::new);
        ValidationUtil.isNull(issue.getId(), "Issue", "id", resources.getIssueId());

        TimeManagement timeManagement = timeMangeRepository.findById(resources.getId()).orElseGet(TimeManagement::new);
        ValidationUtil.isNull(timeManagement.getId(), "TimeManagement", "id", resources.getId());
        resources.setId(timeManagement.getId());
        // timeManagement.copy(resources);

        // 此处是D1，后续步骤设置为false
        judgeStep(resources);

        timeMangeRepository.save(timeManageMapper.toEntity(resources));

        // 修改问题状态为：进行中
        if (resources.getCurStep().equals(CommonConstants.D_STEP_D8)) {
            //重新计算时长:当前时间-createTime
            issue.setDuration(ValidationUtil.getDuration(timeManagement.getCreateTime()));
            issue.setCloseTime(new Timestamp(new Date().getTime()));
            issue.setStatus(CommonConstants.D_STATUS_DONE);
        } else {
            issue.setStatus(CommonConstants.D_STATUS_IN_PROCESS);
            issue.setCloseTime(null);
            // issue.setScore(null);
            issue.setDuration(null);
        }

        issueRepository.save(issue);
    }

    private void judgeStep(TimeManagementDto resources) {
        switch (resources.getCurStep()) {
            case CommonConstants.D_STEP_D1:
                resources.setD2Status(false);
                resources.setD3Status(false);
                resources.setD4Status(false);
                resources.setD5Status(false);
                resources.setD6Status(false);
                resources.setD7Status(false);
                resources.setD8Status(false);
                break;
            case CommonConstants.D_STEP_D2:
                resources.setD3Status(false);
                resources.setD4Status(false);
                resources.setD5Status(false);
                resources.setD6Status(false);
                resources.setD7Status(false);
                resources.setD8Status(false);
                break;
            case CommonConstants.D_STEP_D3:
                resources.setD4Status(false);
                resources.setD5Status(false);
                resources.setD6Status(false);
                resources.setD7Status(false);
                resources.setD8Status(false);
                break;
            case CommonConstants.D_STEP_D4:
                resources.setD5Status(false);
                resources.setD6Status(false);
                resources.setD7Status(false);
                resources.setD8Status(false);
                break;
            case CommonConstants.D_STEP_D5:
                resources.setD6Status(false);
                resources.setD7Status(false);
                resources.setD8Status(false);
                break;
            case CommonConstants.D_STEP_D6:
                resources.setD7Status(false);
                resources.setD8Status(false);
                break;
            case CommonConstants.D_STEP_D7:
                resources.setD8Status(false);
                break;
        }
    }
}
