package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.Issue;
import me.zhengjie.domain.IssueCause;
import me.zhengjie.domain.TimeManagement;
import me.zhengjie.domain.Why;
import me.zhengjie.repository.IssueCauseRepository;
import me.zhengjie.repository.IssueRepository;
import me.zhengjie.repository.TimeManagementRepository;
import me.zhengjie.repository.WhyRepository;
import me.zhengjie.service.WhyService;
import me.zhengjie.service.dto.CauseWhysDto;
import me.zhengjie.service.mapstruct.CauseSmallMapper;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/29 15:57
 */
@Service
@RequiredArgsConstructor
public class WhyServiceImpl implements WhyService {

    private final WhyRepository whyRepository;
    private final IssueCauseRepository issueCauseRepository;
    private final TimeManagementRepository timeMangeRepository;
    private final IssueRepository issueRepository;
    private final CauseSmallMapper causeSmallMapper;

    @Override
    public List<Why> findByCauseId(Long causeId) {
        IssueCause cause = issueCauseRepository.findById(causeId).orElseGet(IssueCause::new);
        ValidationUtil.isNull(cause.getId(), "Issue", "id", causeId);
        return whyRepository.findByCauseId(causeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(List<Why> whys) {
        if (ValidationUtil.isNotEmpty(whys)) {
            Long causeId = whys.get(0).getCauseId();

            IssueCause cause = issueCauseRepository.findById(causeId).orElseGet(IssueCause::new);
            ValidationUtil.isNull(cause.getId(), "Issue", "id", causeId);

            Issue issue = issueRepository.findById(cause.getIssueId()).orElseGet(Issue::new);
            ValidationUtil.isNull(issue.getId(), "Issue", "id", cause.getIssueId());

            whyRepository.saveAll(whys);

            // 修改问题状态为：进行中
            issue.setStatus(CommonConstants.D_STATUS_IN_PROCESS);
            issue.setCloseTime(null);
            // issue.setScore(null);
            issue.setDuration(null);
            issueRepository.save(issue);

            TimeManagement timeManagement = timeMangeRepository.findByIssueId(cause.getIssueId());
            timeManagement.setD4Status(false);
            timeManagement.setD5Status(false);
            timeManagement.setD6Status(false);
            timeManagement.setD7Status(false);
            timeManagement.setD8Status(false);
            timeMangeRepository.save(timeManagement);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(List<Why> whys) {
        if (ValidationUtil.isNotEmpty(whys)) {

            // 判断id是否为空
            Long causeId = whys.get(0).getCauseId();

            whyRepository.deleteByCauseId(causeId);

            IssueCause cause = issueCauseRepository.findById(causeId).orElseGet(IssueCause::new);
            ValidationUtil.isNull(cause.getId(), "Issue", "id", causeId);

            Issue issue = issueRepository.findById(cause.getIssueId()).orElseGet(Issue::new);
            ValidationUtil.isNull(issue.getId(), "Issue", "id", cause.getIssueId());

            whyRepository.saveAll(whys);

            // 修改问题状态为：进行中
            issue.setStatus(CommonConstants.D_STATUS_IN_PROCESS);
            issue.setCloseTime(null);
            // issue.setScore(null);
            issue.setDuration(null);
            issueRepository.save(issue);

            TimeManagement timeManagement = timeMangeRepository.findByIssueId(cause.getIssueId());
            timeManagement.setD4Status(false);
            timeManagement.setD5Status(false);
            timeManagement.setD6Status(false);
            timeManagement.setD7Status(false);
            timeManagement.setD8Status(false);
            timeMangeRepository.save(timeManagement);
        }
    }

    @Override
    public List<CauseWhysDto> findByIssueId(Long issueId) {
        List<CauseWhysDto> causeWhysList = new ArrayList<>();
        List<IssueCause> causeList = issueCauseRepository.findByIssueId(issueId);
        if (ValidationUtil.isNotEmpty(causeList)) {
            causeWhysList = causeSmallMapper.toDto(causeList);
            causeWhysList.removeIf(cause -> cause.getIsExact().equals(false));
            causeWhysList.forEach(cause -> {
                List<Why> whys = whyRepository.findByCauseId(cause.getId());
                // 过滤无5whys的原因
                cause.setWhyList(whys);
            });
            Iterator<CauseWhysDto> iterator = causeWhysList.iterator();
            if (iterator.hasNext()) {
                CauseWhysDto dto = iterator.next();
                if (ValidationUtil.isEmpty(dto.getWhyList())) {
                    iterator.remove();
                }
            }
        }
        return causeWhysList;
    }
}
