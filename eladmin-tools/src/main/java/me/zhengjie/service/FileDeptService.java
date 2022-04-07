package me.zhengjie.service;

import me.zhengjie.domain.FileDept;

import java.util.Collection;
import java.util.List;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/5/14 16:49
 */
public interface FileDeptService {

    /**
     * 根据PID查询
     *
     * @param pid /
     * @return /
     */
    List<FileDept> findByPid(long pid);

    /**
     * 获取子集部门
     *
     * @param deptList
     * @return
     */
    List<Long> getDeptChildren(List<FileDept> deptList);

    /**
     * 获取
     *
     * @param deptIdList
     * @return
     */
    List<Long> getChildrenIds(List<Long> deptIdList);

}
