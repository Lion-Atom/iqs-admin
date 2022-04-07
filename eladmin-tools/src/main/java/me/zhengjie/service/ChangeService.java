package me.zhengjie.service;

import me.zhengjie.domain.Change;
import me.zhengjie.service.dto.ChangeQueryCriteria;
import me.zhengjie.service.dto.ChangeDto;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2022/1/17 11:25
 */
public interface ChangeService {
    
    /**
     * 查询全部数据
     *
     * @param criteria /
     * @return /
     */
    List<ChangeDto> queryAll(ChangeQueryCriteria criteria);

    /**
     * 分页查询
     *
     * @param criteria 条件
     * @param pageable 分页参数
     * @return /
     */
    Map<String, Object> queryAll(ChangeQueryCriteria criteria, Pageable pageable);

    /**
     * 导出数据
     *
     * @param queryAll 待导出的数据
     * @param response /
     * @throws IOException /
     */
    void download(List<ChangeDto> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 根据ID查询
     *
     * @param changeId /
     * @return /
     */
    ChangeDto findById(Long changeId);

    /**
     * 新增/创建
     *
     * @param resources /
     */
    ChangeDto create(Change resources);

    /**
     * 编辑
     *
     * @param resources /
     */
    Map<String,Object> update(Change resources);

    /**
     * 删除
     *
     * @param ids /
     */
    void delete(Set<Long> ids);
}
