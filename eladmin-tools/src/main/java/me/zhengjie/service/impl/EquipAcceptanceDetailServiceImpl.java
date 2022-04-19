package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.EquipAcceptance;
import me.zhengjie.domain.EquipAcceptanceDetail;
import me.zhengjie.repository.EquipAcceptanceDetailRepository;
import me.zhengjie.repository.EquipAcceptanceRepository;
import me.zhengjie.service.EquipAcceptanceDetailService;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipAcceptanceDetailServiceImpl implements EquipAcceptanceDetailService {

    private final EquipAcceptanceDetailRepository detailRepository;
    private final EquipAcceptanceRepository acceptanceRepository;

    @Override
    public List<EquipAcceptanceDetail> findByAcceptanceId(Long acceptanceId) {
        EquipAcceptance acceptance = acceptanceRepository.findById(acceptanceId).orElseGet(EquipAcceptance::new);
        ValidationUtil.isNull(acceptance.getId(), "EquipAcceptance", "id", acceptanceId);
        return detailRepository.findByAcceptanceId(acceptanceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(List<EquipAcceptanceDetail> resources) {
        if (ValidationUtil.isNotEmpty(resources)) {
            long acceptanceId = resources.get(0).getAcceptanceId();
            EquipAcceptance acceptance = acceptanceRepository.findById(acceptanceId).orElseGet(EquipAcceptance::new);
            ValidationUtil.isNull(acceptance.getId(), "EquipAcceptance", "id", acceptanceId);
            detailRepository.saveAll(resources);
        }
    }
}
