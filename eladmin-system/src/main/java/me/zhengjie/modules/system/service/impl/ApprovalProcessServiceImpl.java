package me.zhengjie.modules.system.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.FileApprovalProcess;
import me.zhengjie.modules.system.domain.ApprovalProcess;
import me.zhengjie.modules.system.domain.User;
import me.zhengjie.modules.system.repository.ApprovalProcessRepository;
import me.zhengjie.modules.system.repository.UserRepository;
import me.zhengjie.modules.system.service.ApprovalProcessService;
import me.zhengjie.modules.system.service.dto.ApprovalProcessDto;
import me.zhengjie.modules.system.service.dto.ApprovalProcessQueryCriteria;
import me.zhengjie.modules.system.service.mapstruct.ApprovalProcessMapper;
import me.zhengjie.utils.PageUtil;
import me.zhengjie.utils.QueryHelp;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/6/24 15:28
 */
@Service
@RequiredArgsConstructor
public class ApprovalProcessServiceImpl implements ApprovalProcessService {

    private final ApprovalProcessRepository approvalProcessRepository;

    private final ApprovalProcessMapper approvalProcessMapper;

    private final UserRepository userRepository;

    @Override
    public Map<String, Object> queryAll(ApprovalProcessQueryCriteria criteria, Pageable pageable) {
        /* sort无效
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        pageable.getSortOr(sort);*/
        Page<ApprovalProcess> page = approvalProcessRepository.findAll((root, query, cb) -> QueryHelp.getPredicate(root, criteria, cb), pageable);
        // 获取审批者姓名
        List<ApprovalProcessDto> list = new ArrayList<>();
        long total = 0L;
        if (ValidationUtil.isNotEmpty(page.getContent())) {
            list = approvalProcessMapper.toDto(page.getContent());
            Map<Long, String> apMap = new HashMap<>();
            List<Long> appIds = new ArrayList<>();
            list.forEach(dto -> {
                /*// 获取首批人
                ToolsUser user = userRepository.findById(dto.getApprovedBy()).orElseGet(ToolsUser::new);
                ValidationUtil.isNull(user.getId(), "ToolsUser", "id", dto.getApprovedBy());
                if(user.getDept()!=null){
                    dto.setApprover(user.getDept().getName()+" - "+user.getUsername());
                }else {
                    dto.setApprover(user.getUsername());
                }*/
                appIds.add(dto.getApprovedBy());
            });
            List<User> users = userRepository.findAllById(appIds);
            users.forEach(user -> {
                if(user.getDept()!=null){
                    user.setUsername(user.getDept().getName()+" - "+user.getUsername());
                }else {
                    user.setUsername(user.getUsername());
                }
                apMap.put(user.getId(), user.getUsername());
            });
            list.forEach(dto -> {
                dto.setApprover(apMap.get(dto.getApprovedBy()));
            });
            total = page.getTotalElements();
        }
        Map<String, Object> map = new LinkedHashMap<>(2);
        map.put("content", list);
        map.put("totalElements", total);
        return map;
        // return PageUtil.toPage(page.map(approvalProcessMapper::toDto));
    }

    @Override
    public void update(ApprovalProcess process) {
        // Long approvedBy = process.getApprovedBy();
        ApprovalProcess toolsTask = approvalProcessRepository.findById(process.getId()).orElseGet(ApprovalProcess::new);
        ValidationUtil.isNull(toolsTask.getId(), "ApprovalProcess", "id", toolsTask.getId());
        toolsTask.copy(process);
        // 删除原有的审批记录的后续审批信息
        // approvalProcessRepository.deleteAllByBindingIdAndCreateTime(toolsTask.getBindingId(),toolsTask.getCreateTime());
        approvalProcessRepository.save(toolsTask);
        //todo 更改审批人后，变更对应的后续所有的审批流程信息:变更内容：审批编号+审批人员

    }
}
