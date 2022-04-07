package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.Change;
import me.zhengjie.domain.ChangeManagement;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.repository.ChangeManagementRepository;
import me.zhengjie.repository.ChangeRepository;
import me.zhengjie.service.ChangeManagementService;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author tmj
 * @version 1.0
 * @date 2022/1/18 14:08
 */
@Service
@RequiredArgsConstructor
public class ChangeManagementServiceImpl implements ChangeManagementService {

    private final ChangeManagementRepository managementRepository;
    private final ChangeRepository changeRepository;

    @Override
    public ChangeManagement findByChangeId(Long changeId) {
        // 查询变更判null
        Change change = changeRepository.findById(changeId).orElseGet(Change::new);
        ValidationUtil.isNull(change.getId(), "Change", "id", changeId);
        return managementRepository.findByChangeId(changeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChangeManagement create(ChangeManagement resources) {
        // 查询变更判null
        Change change = changeRepository.findById(resources.getChangeId()).orElseGet(Change::new);
        ValidationUtil.isNull(change.getId(), "Change", "id", resources.getChangeId());
        // 变更状态切换
        change.setStatus("管理中");
        changeRepository.save(change);
        return managementRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> update(ChangeManagement resources) {
        Map<String, Object> map = new HashMap<>();
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        // 查询变更判null
        Change change = changeRepository.findById(resources.getChangeId()).orElseGet(Change::new);
        ValidationUtil.isNull(change.getId(), "Change", "id", resources.getChangeId());
        if (resources.getFinishedStep().equals(2)) {
            // 解锁3.变更通过/关闭
            change.setStatus("待批准");
            changeRepository.save(change);
            list.add(3);
        }
        managementRepository.save(resources);
        map.put("steps", list);
        map.put("num", change.getChangeNum());
        return map;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        managementRepository.deleteAllByIdIn(ids);
    }
}
