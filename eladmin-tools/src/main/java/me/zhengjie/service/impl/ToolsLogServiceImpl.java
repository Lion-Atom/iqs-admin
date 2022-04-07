package me.zhengjie.service.impl;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.ToolsLog;
import me.zhengjie.repository.ToolsLogRepository;
import me.zhengjie.service.ToolsLogService;
import me.zhengjie.service.dto.ToolsLogDelCond;
import me.zhengjie.service.dto.ToolsLogQueryCriteria;
import me.zhengjie.service.mapstruct.ToolsLogMapper;
import me.zhengjie.utils.FileUtil;
import me.zhengjie.utils.PageUtil;
import me.zhengjie.utils.QueryHelp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/5/31 16:33
 */
@Service
@RequiredArgsConstructor
public class ToolsLogServiceImpl implements ToolsLogService {

    private final ToolsLogRepository toolsLogRepository;
    private final ToolsLogMapper toolsLogMapper;

    @Override
    public Object queryAll(ToolsLogQueryCriteria criteria, Pageable pageable) {
        Page<ToolsLog> page = toolsLogRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        return PageUtil.toPage(page.map(toolsLogMapper::toDto));
    }

    @Override
    public List<ToolsLog> queryAll(ToolsLogQueryCriteria criteria) {
        return toolsLogRepository.findAll(((root, criteriaQuery, cb) -> QueryHelp.getPredicate(root, criteria, cb)));
    }

    @Override
    public void download(List<ToolsLog> toolsLogs, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (ToolsLog log : toolsLogs) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("用户名", log.getUsername());
            map.put("描述", log.getDescription());
            map.put("更改明细", log.getDescriptionDetail());
            map.put("创建日期", log.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delLogByCond(ToolsLogDelCond cond) {
        if (cond != null) {
            toolsLogRepository.deleteAllByBindingId(cond.getBindingId());
        }
    }
}
