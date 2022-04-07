package me.zhengjie.service.impl;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/5/14 16:50
 */

import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.FileDept;
import me.zhengjie.repository.FileDeptRepository;
import me.zhengjie.service.FileDeptService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Zheng Jie
 * @date 2019-03-25
 */
@Service
@RequiredArgsConstructor
public class FileDeptServiceImpl implements FileDeptService {

    private final FileDeptRepository fileDeptRepository;

    @Override
    public List<FileDept> findByPid(long pid) {
        return fileDeptRepository.findByPid(pid);
    }

    @Override
    public List<Long> getDeptChildren(List<FileDept> deptList) {
        List<Long> list = new ArrayList<>();
        deptList.forEach(dept -> {
                    if (dept != null && dept.getEnabled()) {
                        List<FileDept> depts = fileDeptRepository.findByPid(dept.getId());
                        if (deptList.size() != 0) {
                            list.addAll(getDeptChildren(depts));
                        }
                        list.add(dept.getId());
                    }
                }
        );
        return list;
    }

    @Override
    public List<Long> getChildrenIds(List<Long> deptIdList) {
        List<Long> list = new ArrayList<>();
        deptIdList.forEach(id -> {
                    List<FileDept> depts = fileDeptRepository.findByPid(id);
                    if (deptIdList.size() != 0) {
                        list.addAll(getDeptChildren(depts));
                    }
                    list.add(id);

                }
        );
        return list;
    }
}
