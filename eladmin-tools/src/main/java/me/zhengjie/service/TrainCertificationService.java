package me.zhengjie.service;

import me.zhengjie.domain.TrainCertification;
import me.zhengjie.domain.TrainNewStaff;
import me.zhengjie.service.dto.TrainCertificationDto;
import me.zhengjie.service.dto.TrainCertificationDto;
import me.zhengjie.service.dto.TrainCertificationQueryCriteria;
import me.zhengjie.service.dto.TrainNewStaffQueryCriteria;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author TongMin Jie
 * @date 2022-05-07
 */
public interface TrainCertificationService {

    /**
     * 查询全部数据
     *
     * @param criteria /
     * @return /
     */
    List<TrainCertificationDto> queryAll(TrainCertificationQueryCriteria criteria);

    /**
     * 导出数据
     *
     * @param queryAll 待导出的数据
     * @param response /
     * @throws IOException /
     */
    void download(List<TrainCertificationDto> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 分页查询
     *
     * @param criteria 条件
     * @param pageable 分页参数
     * @return /
     */
    Map<String, Object> queryAll(TrainCertificationQueryCriteria criteria, Pageable pageable);

    /**
     * 根据ID查询培训-认证信息
     *
     * @param id /
     * @return /
     */
    TrainCertificationDto findById(Long id);

    /**
     * 更新
     *
     * @param resource 培训-认证信息
     */
    void update(TrainCertification resource);

    /**
     * 新增培训-认证信息
     *
     * @param resource 培训-认证信息
     */
    void create(TrainCertificationDto resource);

    /**
     * 删除
     *
     * @param ids /
     */
    void delete(Set<Long> ids);
}
