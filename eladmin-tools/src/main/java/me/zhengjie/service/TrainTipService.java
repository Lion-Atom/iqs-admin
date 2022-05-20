package me.zhengjie.service;

import me.zhengjie.service.dto.TrainTipQueryCriteria;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * @author TongMin Jie
 * @version V1
 * @date 2022/5/20 16:47
 */
public interface TrainTipService {

    /**
     * 分页查询
     *
     * @param criteria 条件
     * @param pageable 分页参数
     * @return /
     */
    Map<String,Object> queryAll(TrainTipQueryCriteria criteria, Pageable pageable);
}
