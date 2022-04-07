package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.Change;
import me.zhengjie.domain.ChangeFactor;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.repository.ChangeFactorRepository;
import me.zhengjie.repository.ChangeFileRepository;
import me.zhengjie.repository.ChangeRepository;
import me.zhengjie.service.ChangeFactorService;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2022/1/18 10:55
 */
@Service
@RequiredArgsConstructor
public class ChangeFactorServiceImpl implements ChangeFactorService {

    private final ChangeFactorRepository factorRepository;
    private final ChangeRepository changeRepository;
    private final ChangeFileRepository fileRepository;

    @Override
    public List<ChangeFactor> findByChangeId(Long changeId) {
        // 查询变更判null
        Change change = changeRepository.findById(changeId).orElseGet(Change::new);
        ValidationUtil.isNull(change.getId(), "Change", "id", changeId);
        return factorRepository.findByChangeId(changeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChangeFactor create(ChangeFactor resources) {
        // 查询变更判null
        Change change = changeRepository.findById(resources.getChangeId()).orElseGet(Change::new);
        ValidationUtil.isNull(change.getId(), "Change", "id", resources.getChangeId());
        //重名校验
        ChangeFactor factor = factorRepository.findByNameAndChangeId(resources.getChangeId(), resources.getName());
        if (factor != null) {
            throw new EntityExistException(ChangeFactor.class, "name", resources.getName());
        }
        return factorRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ChangeFactor resources) {
        // 查询变更判null
        Change change = changeRepository.findById(resources.getChangeId()).orElseGet(Change::new);
        ValidationUtil.isNull(change.getId(), "Change", "id", resources.getChangeId());
        //重名校验
        ChangeFactor factor = factorRepository.findByNameAndChangeId(resources.getChangeId(), resources.getName());
        if (factor != null && !factor.getId().equals(resources.getId())) {
            throw new EntityExistException(ChangeFactor.class, "name", resources.getName());
        }
        factorRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        // 批量删除因素信息
        factorRepository.deleteAllByIdIn(ids);
        // 批量删除因素下附件信息
        fileRepository.deleteByFactorIdIn(ids);
    }
}
