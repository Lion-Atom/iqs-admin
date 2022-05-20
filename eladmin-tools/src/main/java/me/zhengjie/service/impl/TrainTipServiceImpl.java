package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.TrainCertification;
import me.zhengjie.domain.TrainSchedule;
import me.zhengjie.domain.TrainTip;
import me.zhengjie.repository.TrainCertificationRepository;
import me.zhengjie.repository.TrainScheduleRepository;
import me.zhengjie.repository.TrainTipRepository;
import me.zhengjie.service.TrainTipService;
import me.zhengjie.service.dto.TrainTipDto;
import me.zhengjie.service.dto.TrainTipQueryCriteria;
import me.zhengjie.service.mapstruct.TrTipMapper;
import me.zhengjie.utils.QueryHelp;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author TongMin Jie
 * @version V1
 * @date 2022/5/20 16:49
 */
@Service
@RequiredArgsConstructor
public class TrainTipServiceImpl implements TrainTipService {

    private final TrainTipRepository tipRepository;
    private final TrTipMapper tipMapper;
    private final TrainCertificationRepository certRepository;
    private final TrainScheduleRepository scheduleRepository;


    @Override
    public Map<String, Object> queryAll(TrainTipQueryCriteria criteria, Pageable pageable) {
        Map<String, Object> map = new HashMap<>();
        Page<TrainTip> page = tipRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        List<TrainTipDto> list = new ArrayList<>();
        long total = 0L;
        if (ValidationUtil.isNotEmpty(page.getContent())) {
            list = tipMapper.toDto(page.getContent());
            list.forEach(tip->{
               if(tip.getTrainType().equals(CommonConstants.TRAIN_TIP_TYPE_CERTIFICATION)){
                   // 获取证书信息
                   TrainCertification cert = certRepository.findById(tip.getBindingId()).orElseGet(TrainCertification::new);
                   ValidationUtil.isNull(cert.getId(), "TrainCertification", "id", tip.getBindingId());
                   if(CommonConstants.STAFF_CER_TYPE_LIST.contains(cert.getCertificationType())) {
                       tip.setBindingName(cert.getStaffName()+"-"+cert.getJobType());
                   } else {
                       tip.setBindingName(cert.getStaffName()+"-"+cert.getJobName());
                   }
               } else {
                   // 获取日程安排信息
                   TrainSchedule schedule = scheduleRepository.findById(tip.getBindingId()).orElseGet(TrainSchedule::new);
                   ValidationUtil.isNull(schedule.getId(), "TrainSchedule", "id", tip.getBindingId());
                   tip.setBindingName(schedule.getTrainTitle());
               }
            });
            total = page.getTotalElements();
        }
        map.put("content", list);
        map.put("totalElements", total);
        return map;
    }
}
