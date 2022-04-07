package me.zhengjie.service;

import me.zhengjie.domain.Issue;
import me.zhengjie.service.dto.IssueDto;
import me.zhengjie.service.dto.IssueQueryCriteria;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/21 18:02
 */
public interface IssueService {

    /**
     * 根据ID查询
     * @param id /
     * @return /
     */
    IssueDto findById(Long id);

    /**
     * 创建
     * @param resources /
     */
    void create(Issue resources);

    /**
     * 编辑
     * @param resources /
     */
    void update(Issue resources);

    /**
     * 删除
     * @param ids /
     */
    void delete(Set<Long> ids);

    /**
     * 分页查询
     * @param criteria 条件
     * @param pageable 分页参数
     * @return /
     */
    Map<String,Object> queryAll(IssueQueryCriteria criteria, Pageable pageable);

    /**
     * 查询全部数据
     * @param criteria /
     * @return /
     */
    List<IssueDto> queryAll(IssueQueryCriteria criteria);

    /**
     * 导出数据
     * @param queryAll 待导出的数据
     * @param response /
     * @throws IOException /
     */
    void download(List<IssueDto> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 验证是否被用户关联
     * @param ids /
     */
    void verification(Set<Long> ids);
}
