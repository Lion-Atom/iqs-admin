package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.*;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.*;
import me.zhengjie.service.TeamMemberService;
import me.zhengjie.service.dto.TeamMemberDto;
import me.zhengjie.service.mapstruct.TeamMemberMapper;
import me.zhengjie.utils.RedisUtils;
import me.zhengjie.utils.SecurityUtils;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

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
@CacheConfig(cacheNames = "teamMembers")
public class TeamMemberServiceImpl implements TeamMemberService {

    private final IssueRepository issueRepository;
    private final TeamMembersRepository teamMembersRepository;
    private final TeamMemberMapper teamMemberMapper;
    private final RedisUtils redisUtils;
    private final ApproverRepository approverRepository;
    private final TeamRepository teamRepository;
    private final TimeManagementRepository managementRepository;
    private final FileDeptRepository fileDeptRepository;

    @Override
//    @Cacheable(key = "'issueId:' + #p0")
    public Map<String, Object> findByIssueId(Long issueId) {
        Map<String, Object> map = new HashMap<>();
        List<TeamMember> teamMembers = teamMembersRepository.findByIssueId(issueId);
        List<TeamMemberDto> membersDtoList = new ArrayList<>();
        if (ValidationUtil.isNotEmpty(teamMembers)) {
            membersDtoList = teamMemberMapper.toDto(teamMembers);
            membersDtoList.forEach(mem -> {
                // 添加用户信息
                if (mem.getUserId() != null) {
                    Approver approver = approverRepository.findById(mem.getUserId()).orElseGet(Approver::new);
                    ValidationUtil.isNull(approver.getId(), "Approver", "id", mem.getUserId());
                    mem.setUserName(approver.getUsername());
                    mem.setPhone(approver.getPhone());
                    mem.setEmail(approver.getEmail());
                    if (approver.getFileDept() != null) {
                        FileDept dept = approver.getFileDept();
                        mem.setDeptName(dept.getName());
                        //  todo查找所在公司（顶级部门）
                        if (dept.getPid() != null) {
                            Long pid = dept.getPid();
                            String topName;
                            do {
                                FileDept pDept = fileDeptRepository.findById(pid).orElseGet(FileDept::new);
                                pid = pDept.getPid();
                                topName = pDept.getName();
                            } while (pid != null);
                            mem.setCompanyName(topName);
                        } else {
                            mem.setCompanyName(approver.getFileDept().getName());
                        }
                    }
                }
            });
        }
        map.put("content", membersDtoList);
        return map;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(TeamMember resources) {
        // 权限判断
        checkEditAuthorized(resources.getIssueId());
        // 修改权限判断
        checkSubmitAuthorized(resources.getIssueId());
        Issue issue = issueRepository.findById(resources.getIssueId()).orElseGet(Issue::new);
        ValidationUtil.isNull(issue.getId(), "Issue", "id", resources.getIssueId());
        Team team = teamRepository.findByIssueId(issue.getId());
        // 判断小组是否存在
        if (team != null) {
            resources.setTeamId(team.getId());
        } else {
            throw new BadRequestException("No Team!找不到问题对应的任务小组！");
        }
        // 判断成员是否已存在
        List<TeamMember> members = teamMembersRepository.findByIssueIdAndUserId(issue.getId(), resources.getUserId());
        if (ValidationUtil.isNotEmpty(members)) {
            throw new BadRequestException("This member has Existed!该成员已在小组中，请勿重复添加！");
        }
        teamMembersRepository.save(resources);
        // 修改问题状态为：进行中、清空关闭时间、时长等记录
        issue.setStatus(CommonConstants.D_STATUS_IN_PROCESS);
        issue.setCloseTime(null);
        // issue.setScore(null);
        issue.setDuration(null);
        issueRepository.save(issue);

        // 此处是D1，后续步骤设置为false
        TimeManagement timeManagement = managementRepository.findByIssueId(issue.getId());
        timeManagement.setD1Status(false);
        timeManagement.setD2Status(false);
        timeManagement.setD3Status(false);
        timeManagement.setD4Status(false);
        timeManagement.setD5Status(false);
        timeManagement.setD6Status(false);
        timeManagement.setD7Status(false);
        timeManagement.setD8Status(false);
        managementRepository.save(timeManagement);
    }

    @Override
//    @CacheEvict(key = "'issueId:' + #p0.issueId")
    @Transactional(rollbackFor = Exception.class)
    public void update(TeamMember resources) {
        // 权限判断
        checkEditAuthorized(resources.getIssueId());
        // 修改权限判断
        checkSubmitAuthorized(resources.getIssueId());
        // 查询问题判null
        Issue issue = issueRepository.findById(resources.getIssueId()).orElseGet(Issue::new);
        ValidationUtil.isNull(issue.getId(), "Issue", "id", resources.getIssueId());
        TeamMember members = teamMembersRepository.findById(resources.getId()).orElseGet(TeamMember::new);
        // TeamMember members = teamMembersRepository.findByIssueIdAndTeamIdAndUserId(resources.getIssueId(), resources.getTeamId(),resources.getUserId());
        // ValidationUtil.isNull(members.getIssueId(), "TeamMember", "issueId", resources.getIssueId());
        ValidationUtil.isNull(members.getId(), "TeamMember", "id", resources.getId());
        resources.setIssueId(members.getIssueId());
        resources.setTeamId(members.getTeamId());
        teamMembersRepository.save(resources);

        // 修改问题状态为：进行中、清空关闭时间、时长等记录
        issue.setStatus(CommonConstants.D_STATUS_IN_PROCESS);
        issue.setCloseTime(null);
        // issue.setScore(null);
        issue.setDuration(null);
        issueRepository.save(issue);

        // 此处是D1，后续步骤设置为false
        TimeManagement timeManagement = managementRepository.findByIssueId(issue.getId());
        timeManagement.setD1Status(false);
        timeManagement.setD2Status(false);
        timeManagement.setD3Status(false);
        timeManagement.setD4Status(false);
        timeManagement.setD5Status(false);
        timeManagement.setD6Status(false);
        timeManagement.setD7Status(false);
        timeManagement.setD8Status(false);
        managementRepository.save(timeManagement);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {

        //修改问题状态
        List<Long> idList = new ArrayList<>(ids);
        Long issueId = teamMembersRepository.findIssueIdById(idList.get(0));
        checkEditAuthorized(issueId);
        Issue issue = issueRepository.findById(issueId).orElseGet(Issue::new);
        ValidationUtil.isNull(issue.getId(), "Issue", "id", issueId);
        // 修改问题状态为：进行中、清空关闭时间、时长等记录
        issue.setStatus(CommonConstants.D_STATUS_IN_PROCESS);
        issue.setCloseTime(null);
        // issue.setScore(null);
        issue.setDuration(null);
        issueRepository.save(issue);

        teamMembersRepository.deleteAllByIdIn(ids);

        // 此处是D1，后续步骤设置为false
        TimeManagement timeManagement = managementRepository.findByIssueId(issue.getId());
        timeManagement.setD1Status(false);
        timeManagement.setD2Status(false);
        timeManagement.setD3Status(false);
        timeManagement.setD4Status(false);
        timeManagement.setD5Status(false);
        timeManagement.setD6Status(false);
        timeManagement.setD7Status(false);
        timeManagement.setD8Status(false);
        managementRepository.save(timeManagement);
    }

    @Override
    public void checkEditAuthorized(Long issueId) {
        Boolean isAdmin = SecurityUtils.isAdmin();
        List<TeamMember> oldMembers = teamMembersRepository.findByIssueId(issueId);
        List<TeamMember> curMembers = teamMembersRepository.findByIssueIdAndUserId(issueId, SecurityUtils.getCurrentUserId());
        if (ValidationUtil.isNotEmpty(oldMembers)) {
            // 后来的审核人不是小组成员，则无权更改信息
            if (ValidationUtil.isEmpty(curMembers) && !isAdmin) {
                throw new BadRequestException("No Access!不属于该组成员，无权更改此问题！");
            }
        }
    }

    @Override
    public void checkSubmitAuthorized(Long issueId) {
        Boolean isAdmin = SecurityUtils.isAdmin();
        Long userId = SecurityUtils.getCurrentUserId();
        List<String> teamRoles = new ArrayList<>();
        teamRoles.add(CommonConstants.D_TEAM_ROLE_LEADER);
        teamRoles.add(CommonConstants.D_TEAM_ROLE_MANAGER);
        List<TeamMember> curMembers = teamMembersRepository.findByIssueIdAndUserIdAndRole(issueId, userId, teamRoles);
        // 后来的审核人不是小组成员，则无权更改信息
        if (ValidationUtil.isEmpty(curMembers) && !isAdmin) {
            throw new BadRequestException("No Access!除项目组长和管理员外，其他人无权修改此数据！");
        }

    }
}
