package me.zhengjie.service;

import me.zhengjie.domain.Auditor;
import me.zhengjie.service.dto.AuditorDto;
import me.zhengjie.service.dto.AuditorQueryCriteria;
import me.zhengjie.service.dto.AuditorQueryDto;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/9/7 10:36
 */
public interface AuditorService {

    /**
     * 根据ID查询
     *
     * @param id /
     * @return /
     */
    AuditorDto findById(Long id);

    /**
     * 创建
     *
     * @param resources /
     */
    void create(Auditor resources);

    /**
     * 编辑
     *
     * @param resources /
     */
    void update(Auditor resources);

    /**
     * 删除
     *
     * @param ids /
     */
    void delete(Set<Long> ids);

    /**
     * 分页查询
     *
     * @param criteria 条件
     * @param pageable 分页参数
     * @return /
     */
    Map<String, Object> queryAll(AuditorQueryCriteria criteria, Pageable pageable);

    /**
     * 查询全部数据
     *
     * @param criteria /
     * @return /
     */
    List<AuditorDto> queryAll(AuditorQueryCriteria criteria);

    /**
     * 导出数据
     *
     * @param queryAll 待导出的数据
     * @param response /
     * @throws IOException /
     */
    void download(List<AuditorDto> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 验证是否被用户关联
     *
     * @param ids /
     */
    void verification(Set<Long> ids);


    /**
     * 激活审核人员审核流程
     *
     * @param auditorId 审核人员标识
     */
    void activatedById(Long auditorId);

    /**
     * 条件查询审核人员信息
     *
     * @param queryDto 查询条件
     * @return 审核人员信息列表
     */
    List<AuditorDto> findByExample(AuditorQueryDto queryDto);

    /**
     * @return 审核人员有效期分布
     */
    Map<String, Object> queryAuditorsByStatus();

    /**
     * @return 审核人员部门分布
     */
    Map<String, Object> queryAuditorsByDept();

    /**
     * 查询个人审核员证件信息
     *
     * @param userId 员工ID
     * @return 审核员信息
     */
    List<Auditor> findByUserId(Long userId);
}
