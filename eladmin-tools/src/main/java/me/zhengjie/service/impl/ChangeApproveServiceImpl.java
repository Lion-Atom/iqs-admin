package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.Change;
import me.zhengjie.domain.ChangeApprove;
import me.zhengjie.repository.ChangeApproveRepository;
import me.zhengjie.repository.ChangeRepository;
import me.zhengjie.service.ChangeApproveService;
import me.zhengjie.service.ChangeApproveService;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2022/1/18 14:08
 */
@Service
@RequiredArgsConstructor
public class ChangeApproveServiceImpl implements ChangeApproveService {

    private final ChangeApproveRepository approveRepository;
    private final ChangeRepository changeRepository;

    @Override
    public ChangeApprove findByChangeId(Long changeId) {
        // 查询变更判null
        Change change = changeRepository.findById(changeId).orElseGet(Change::new);
        ValidationUtil.isNull(change.getId(), "Change", "id", changeId);
        return approveRepository.findByChangeId(changeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChangeApprove create(ChangeApprove resources) {
        // 查询变更判null
        Change change = changeRepository.findById(resources.getChangeId()).orElseGet(Change::new);
        ValidationUtil.isNull(change.getId(), "Change", "id", resources.getChangeId());
        change.setStatus("批准中");
        changeRepository.save(change);
        return approveRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ChangeApprove resources) {
        // 查询变更判null
        Change change = changeRepository.findById(resources.getChangeId()).orElseGet(Change::new);
        ValidationUtil.isNull(change.getId(), "Change", "id", resources.getChangeId());
        if(resources.getFinishedStep() == 2) {
            change.setStatus("已批准");
        }
        changeRepository.save(change);
        approveRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        approveRepository.deleteAllByIdIn(ids);
    }
}
