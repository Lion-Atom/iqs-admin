package me.zhengjie.service;

import me.zhengjie.domain.InstruCali;
import me.zhengjie.service.dto.InstruCaliDto;
import me.zhengjie.service.dto.InstruCaliQueryCriteria;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2022/3/14 13:25
 */
public interface InstruCaliService {

    /**
     * 查询全部数据
     *
     * @param criteria /
     * @return /
     */
    List<InstruCaliDto> queryAll(InstruCaliQueryCriteria criteria);

    /**
     * 导出数据
     *
     * @param queryAll 待导出的数据
     * @param response /
     * @throws IOException /
     */
    void download(List<InstruCaliDto> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 分页查询
     *
     * @param criteria 条件
     * @param pageable 分页参数
     * @return /
     */
    Map<String, Object> queryAll(InstruCaliQueryCriteria criteria, Pageable pageable);

    /**
     * 根据ID查询仪器校准信息
     *
     * @param id /
     * @return /
     */
    InstruCaliDto findById(Long id);


    /**
     * 更新
     *
     * @param resource 仪器校准信息
     */
    void update(InstruCali resource);


    /**
     * 新增仪器校准信息
     *
     * @param resource 仪器校准信息
     */
    void create(InstruCaliDto resource);


    /**
     * 删除
     *
     * @param ids /
     */
    void delete(Set<Long> ids);
}
