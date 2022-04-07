package me.zhengjie.service;

import me.zhengjie.domain.IssueAction;
import me.zhengjie.service.dto.ActionQueryCriteria;
import me.zhengjie.service.dto.IssueActionDto;
import me.zhengjie.service.dto.IssueActionQueryDto;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/30 16:11
 */
public interface IssueActionService {

    /**
     * 查询措施
     *
     * @param issueId 问题标识
     * @return 围堵措施列表
     */
    List<IssueActionDto> findByIssueId(Long issueId);

    /**
     * 更改措施信息
     *
     * @param resources 围堵措施信息
     */
    void update(IssueActionDto resources);

    /**
     * 新增措施信息
     *
     * @param resources 删除措施信息
     */
    void create(IssueActionDto resources);


    /**
     * 删除措施
     *
     * @param resources 措施信息
     */
    void delete(IssueAction resources);


    /**
     * 多条件查询措施
     *
     * @param queryDto 查询条件
     * @return 措施信息
     */
    List<IssueActionDto> findByExample(IssueActionQueryDto queryDto);

    /**
     * 多条件查询措施
     *
     * @param issueId 问题标识
     * @return 措施信息
     */
    List<IssueActionDto> findCanRemoveByIssueId(Long issueId);


    /**
     * 根据标识查询行动信息
     *
     * @param id 行动标识
     * @return 行动信息
     */
    IssueActionDto findById(Long id);


    /**
     * 查询个人8D任务
     *
     * @param currentUserId 登陆人姓名
     * @return 个人任务信息
     */
    List<IssueActionDto> findActionByUserId(Long currentUserId);

    /**
     * 查询全部数据
     *
     * @param criteria /
     * @return /
     */
    List<IssueActionDto> queryAll(ActionQueryCriteria criteria);

    /**
     * @param criteria 查询条件
     * @param pageable 分页器
     * @return 个人8D任务列表
     */
    Map<String, Object> queryAll(ActionQueryCriteria criteria, Pageable pageable);

    /**
     * 导出数据
     *
     * @param queryAll 待导出的数据
     * @param response /
     * @throws IOException /
     */
    void download(List<IssueActionDto> queryAll, HttpServletResponse response) throws IOException;
}
