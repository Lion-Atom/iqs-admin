package me.zhengjie.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.FileDept;
import me.zhengjie.domain.TrExamStaffTranscript;
import me.zhengjie.domain.TrainExamStaff;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.repository.FileDeptRepository;
import me.zhengjie.repository.TrExamStaffTranscriptRepository;
import me.zhengjie.repository.TrainExamStaffRepository;
import me.zhengjie.service.TrainExamStaffService;
import me.zhengjie.service.dto.TrExamStaffDto;
import me.zhengjie.service.dto.TrainExamStaffQueryCriteria;
import me.zhengjie.service.mapstruct.TrExamStaffMapper;
import me.zhengjie.utils.FileUtil;
import me.zhengjie.utils.QueryHelp;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author TongMin Jie
 * @version V1
 * @date 2022/5/16 9:47
 */
@Service
@RequiredArgsConstructor
public class TrainExamStaffServiceImpl implements TrainExamStaffService {

    private final TrExamStaffMapper staffMapper;
    private final TrainExamStaffRepository staffRepository;
    private final FileDeptRepository deptRepository;
    private final TrExamStaffTranscriptRepository transcriptRepository;

    @Override
    public List<TrExamStaffDto> queryAll(TrainExamStaffQueryCriteria criteria) {
        List<TrExamStaffDto> list = new ArrayList<>();
        List<TrainExamStaff> staffs = staffRepository.findAll(((root, query, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder)));
        if (ValidationUtil.isNotEmpty(staffs)) {
            Set<Long> deptIds = new HashSet<>();
            Map<Long, String> deptMap = new HashMap<>();
            list = staffMapper.toDto(staffs);
            list.forEach(staff -> {
                deptIds.add(staff.getDepartId());
                initStffTranscript(staff);
            });
            initStaffDepartName(list, deptIds, deptMap);
        }
        return list;
    }

    private void initStffTranscript(TrExamStaffDto staff) {
        List<TrExamStaffTranscript> transcripts = transcriptRepository.findByTrExamStaffId(staff.getId());
        TrExamStaffTranscript lastTrans = transcriptRepository.findByTrExamStaffIdOrderByResitSort(staff.getId());
        if (ValidationUtil.isNotEmpty(transcripts)) {
            staff.setTranscriptList(transcripts);
            staff.setIsPassed(lastTrans.getExamPassed());
            staff.setLastExamDate(lastTrans.getExamDate());
            staff.setLastScore(lastTrans.getExamScore());
            staff.setNextExamDate(lastTrans.getNextDate());
        }
    }

    private void initStaffDepartName(List<TrExamStaffDto> list, Set<Long> deptIds, Map<Long, String> deptMap) {
        if (!deptIds.isEmpty()) {
            List<FileDept> deptList = deptRepository.findByIdIn(deptIds);
            deptList.forEach(dept -> {
                deptMap.put(dept.getId(), dept.getName());
            });
            if (ValidationUtil.isNotEmpty(deptList)) {
                list.forEach(dto -> {
                    dto.setDepartName(deptMap.get(dto.getDepartId()));
                });
            }
        }
    }

    @Override
    public void download(List<TrExamStaffDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (TrExamStaffDto dto : queryAll) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("员工姓名", dto.getStaffName());
            map.put("部门", dto.getDepartName());
            map.put("上级主管", dto.getSuperior());
            map.put("岗位", dto.getJobName());
            map.put("车间", dto.getWorkshop());
            map.put("考试日期", dto.getLastExamDate());
            map.put("考试分数", dto.getLastScore());
            map.put("是否通过", dto.getIsPassed() ? "通过" : "未通过");
            map.put("下次考试日期", dto.getNextExamDate());
            map.put("创建日期", dto.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public Map<String, Object> queryAll(TrainExamStaffQueryCriteria criteria, Pageable pageable) {
        Map<String, Object> map = new HashMap<>();
        Page<TrainExamStaff> page = staffRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        List<TrExamStaffDto> list = new ArrayList<>();
        long total = 0L;
        if (ValidationUtil.isNotEmpty(page.getContent())) {
            list = staffMapper.toDto(page.getContent());
            // 根据试卷信息返回考试结果等信息
            list.forEach(this::initStffTranscript);
            total = page.getTotalElements();
        }
        map.put("content", list);
        map.put("totalElements", total);
        return map;
    }

    @Override
    public TrExamStaffDto findById(Long id) {
        TrainExamStaff staff = staffRepository.findById(id).orElseGet(TrainExamStaff::new);
        ValidationUtil.isNull(staff.getId(), "TrainNewStaff", "id", id);
        TrExamStaffDto dto = staffMapper.toDto(staff);
        // todo 查询考试信息
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(TrExamStaffDto resource) {
        TrainExamStaff staff = staffRepository.findAllByDepartIdAndStaffName(resource.getDepartId(), resource.getStaffName());
        if (staff != null) {
            throw new EntityExistException(TrainExamStaff.class, "staffName", resource.getStaffName());
        }
        TrainExamStaff examStaff = staffRepository.save(staffMapper.toEntity(resource));
        // 保存考试信息及试卷信息
        if (ValidationUtil.isNotEmpty(resource.getTranscriptList())) {
            // 保存考试信息
            resource.getTranscriptList().forEach(transcript -> {
                transcript.setTrExamStaffId(examStaff.getId());
            });
            transcriptRepository.saveAll(resource.getTranscriptList());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(TrainExamStaff resource) {
        TrainExamStaff entity = staffRepository.findById(resource.getId()).orElseGet(TrainExamStaff::new);
        ValidationUtil.isNull(entity.getId(), "TrainNewStaff", "id", resource.getId());
        TrainExamStaff staff = staffRepository.findAllByDepartIdAndStaffName(resource.getDepartId(), resource.getStaffName());
        if (staff != null && !staff.getId().equals(resource.getId())) {
            throw new EntityExistException(TrainExamStaff.class, "staffName", resource.getStaffName());
        }
        staffRepository.save(resource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        staffRepository.deleteAllByIdIn(ids);
        // 删除相关附件
        transcriptRepository.deleteByTrExamStaffIdIn(ids);
    }
}
