package me.zhengjie.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import lombok.RequiredArgsConstructor;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.CsFeedback;
import me.zhengjie.domain.TrainCertification;
import me.zhengjie.repository.CsFeedbackFileRepository;
import me.zhengjie.repository.CsFeedbackRepository;
import me.zhengjie.service.CsFeedbackService;
import me.zhengjie.service.dto.CsFeedbackDto;
import me.zhengjie.service.dto.CsFeedbackQueryCriteria;
import me.zhengjie.service.dto.TrainNewStaffDto;
import me.zhengjie.service.mapstruct.CsFeedbackMapper;
import me.zhengjie.utils.FileUtil;
import me.zhengjie.utils.PageUtil;
import me.zhengjie.utils.QueryHelp;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author TongMin Jie
 * @version V1
 * @date 2022/7/15 11:02
 */
@Service
@RequiredArgsConstructor
@DS("self")
public class CsFeedbackServiceImpl implements CsFeedbackService {

    private final CsFeedbackRepository feedbackRepository;
    private final CsFeedbackFileRepository fileRepository;
    private final CsFeedbackMapper csFeedbackMapper;


    @Override
    public List<CsFeedback> queryAll(CsFeedbackQueryCriteria criteria) {
        return feedbackRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
    }

    @Override
    public void download(List<CsFeedback> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (CsFeedback dto : queryAll) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("客户名称", dto.getCompanyName());
            map.put("反馈类型", dto.getType());
            map.put("问题描述", dto.getDesc());
            map.put("QQ号码", dto.getQq());
            map.put("手机号码", dto.getPhone());
            map.put("电子邮箱", dto.getEmail());
            map.put("反馈状态", dto.getStatus());
            map.put("创建人员", dto.getCreateBy());
            map.put("创建日期", dto.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public Map<String, Object> queryAll(CsFeedbackQueryCriteria criteria, Pageable pageable) {
        Page<CsFeedback> page = feedbackRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        return PageUtil.toPage(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(CsFeedback resource) {
        CsFeedback entity = feedbackRepository.findById(resource.getId()).orElseGet(CsFeedback::new);
        ValidationUtil.isNull(entity.getId(), "CsFeedback", "id", resource.getId());
        feedbackRepository.save(resource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(CsFeedbackDto resource) {
        CsFeedback csFeedback = csFeedbackMapper.toEntity(resource);
        csFeedback.setStatus("新建");
        CsFeedback fed = feedbackRepository.save(csFeedback);
        // 文件列表
        if (ValidationUtil.isNotEmpty(resource.getFileList())) {
            resource.getFileList().forEach(file -> {
                file.setCsFeedbackId(fed.getId());
            });
            fileRepository.saveAll(resource.getFileList());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        feedbackRepository.deleteAllByIdIn(ids);
        // 删除用户反馈附件
        fileRepository.deleteByCsFeedbackIdIn(ids);
    }
}
