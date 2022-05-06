package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.TrainNewStaff;
import me.zhengjie.service.TrainNewStaffService;
import me.zhengjie.service.dto.TrainNewStaffDto;
import me.zhengjie.service.dto.TrainNewStaffQueryCriteria;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TrainNewStaffServiceImpl implements TrainNewStaffService {

    @Override
    public List<TrainNewStaffDto> queryAll(TrainNewStaffQueryCriteria criteria) {
        return null;
    }

    @Override
    public void download(List<TrainNewStaffDto> queryAll, HttpServletResponse response) throws IOException {

    }

    @Override
    public Map<String, Object> queryAll(TrainNewStaffQueryCriteria criteria, Pageable pageable) {
        return null;
    }

    @Override
    public TrainNewStaffDto findById(Long id) {
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(TrainNewStaff resource) {

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(TrainNewStaff resource) {

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {

    }
}
