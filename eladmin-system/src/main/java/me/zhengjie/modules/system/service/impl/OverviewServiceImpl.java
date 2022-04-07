package me.zhengjie.modules.system.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.base.CommonDTO;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.*;
import me.zhengjie.modules.system.domain.*;
import me.zhengjie.modules.system.repository.*;
import me.zhengjie.modules.system.service.OverviewService;
import me.zhengjie.modules.system.service.dto.OverviewQueryCriteria;
import me.zhengjie.repository.*;
import me.zhengjie.service.FileDeptService;
import me.zhengjie.utils.QueryHelp;
import me.zhengjie.utils.SecurityUtils;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/5/24 17:47
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "dept")
public class OverviewServiceImpl implements OverviewService {

    private final DeptRepository deptRepository;
    private final UserRepository userRepository;
    private final FileCategoryRepository fileCategoryRepository;
    private final LocalStorageRepository localStorageRepository;
    private final DictRepository dictRepository;
    private final DictDetailRepository dictDetailRepository;
    private final FileLevelRepository fileLevelRepository;
    private final FileDeptService fileDeptService;
    private final LocalStorageSmallRepository storageSmallRepository;
    private final ToolsTaskRepository toolsTaskRepository;
    private final IssueRepository issueRepository;
    private final AuditPlanRepository planRepository;
    private final AuditorRepository auditorRepository;

    @Override
//    @Cacheable(key = "'id:overview'")
    public Map<String, Object> queryAll() {
        Map<String, Object> map = new HashMap<>();
        //获取部门总数
        Integer deptCount = deptRepository.getDeptCount();
        map.put("dept", deptCount);
        //获取人员总数
        Integer userCount = userRepository.getUserCount();
        map.put("user", userCount);
        //获取人员任务总数
        // 所有任务
        Integer taskCount;
        /*if(SecurityUtils.getIsAdmin()){
        // 管理员权限查询全部任务数据
            taskCount = toolsTaskRepository.findAllCount();
        }else{
        // 非管理员权限只能查询个人任务数目
            taskCount = toolsTaskRepository.findCountByUserId(SecurityUtils.getCurrentUserId());
        }*/
        // taskCount = toolsTaskRepository.findAllCount();
        taskCount = toolsTaskRepository.findAllNotDoneCount(false);
        map.put("task", taskCount);
        //获取文件分类总数
        Integer fileCategoryCount = fileCategoryRepository.getFileCategoryCount();
        map.put("fileCategory", fileCategoryCount);
        //获取8D总数
        Integer issueCount = issueRepository.getIssueCount();
        map.put("issue", issueCount);
        //获取文件总数
        Integer fileCount = localStorageRepository.getFileCount();
        map.put("file", fileCount);
        return map;
    }

    @Override
    public Map<String, Object> queryFilesByType(Boolean isAdmin) {
//        Dict dict = dictRepository.getByName("file_type");
        List<Long> deptIdList = new ArrayList<>();
        String departName = "All";
        if (!isAdmin) {
            //非管理员则只能查看权限内部门对应的文件数量
            Long deptId = SecurityUtils.getCurrentDeptId();
            Dept dept = deptRepository.findById(deptId).orElseGet(Dept::new);
            if (dept != null) {
                departName = dept.getName();
            }
            deptIdList.add(deptId);
            List<FileDept> data = fileDeptService.findByPid(deptId);
            // 然后把子节点的ID都加入到集合中
            deptIdList.addAll(fileDeptService.getDeptChildren(data));
        }
        List<DictDetail> dictDetails = dictDetailRepository.findDictDetailByDictId(CommonConstants.DICT_FILE_TYPE);
        Map<String, Object> map = new HashMap<>();
        if (ValidationUtil.isNotEmpty(dictDetails)) {
            List<CommonDTO> list = new ArrayList<>();
            dictDetails.forEach(detail -> {
                // key:detail.getValue() 文件类型值
                //value:文件数目
                //map.put(detail.getValue(), localStorageRepository.getCountByFileType(detail.getValue()));
                Integer count = 0;
                if (isAdmin) {
                    count = localStorageRepository.getCountByFileType(detail.getValue());
                } else {
                    count = localStorageRepository.getCountByFileTypeAndDeptIdIn(detail.getValue(), deptIdList);
                }
                CommonDTO commonDTO = new CommonDTO();
                commonDTO.setName(detail.getLabel());
                commonDTO.setOtherValue(detail.getValue());
                commonDTO.setValue(String.valueOf(count));
                list.add(commonDTO);
            });
            int totalCount = localStorageRepository.getFileCount();
            map.put("content", totalCount);
            map.put("totalElements", list);
            map.put("scope", departName);
        }
        return map;
    }

    @Override
    public Map<String, Object> queryFilesByLevel(Boolean isAdmin) {
        List<FileLevel> fileLevels = fileLevelRepository.findAll();
        List<Long> deptIdList = new ArrayList<>();
        String departName = "All";
        if (!isAdmin) {
            //非管理员则只能查看权限内部门对应的文件数量
            Long deptId = SecurityUtils.getCurrentDeptId();
            Dept dept = deptRepository.findById(deptId).orElseGet(Dept::new);
            if (dept != null) {
                departName = dept.getName();
            }
            deptIdList.add(deptId);
            List<FileDept> data = fileDeptService.findByPid(deptId);
            // 然后把子节点的ID都加入到集合中
            deptIdList.addAll(fileDeptService.getDeptChildren(data));
            //非管理员不可查看一级文件
            fileLevels.removeIf(level -> level.getPid() == null);
        }
        Map<String, Object> map = new HashMap<>();
        if (ValidationUtil.isNotEmpty(fileLevels)) {
            List<CommonDTO> list = new ArrayList<>();
            fileLevels.forEach(level -> {
                Integer count = 0;
                if (isAdmin) {
                    count = localStorageRepository.getCountByFileLevelId(level.getId());
                } else {
                    count = localStorageRepository.getCountByFileLevelIdAndDeptIdIn(level.getId(), deptIdList);
                }
                CommonDTO commonDTO = new CommonDTO();
                commonDTO.setId(level.getId());
                commonDTO.setName(level.getName());
                commonDTO.setValue(String.valueOf(count));
                list.add(commonDTO);
            });
            int totalCount = localStorageRepository.getFileCount();
            map.put("content", totalCount);
            map.put("totalElements", list);
            map.put("scope", departName);
        }
        return map;
    }

    @Override
    public Map<String, Object> queryFilesByFileDept(Boolean isAdmin) {
        //获取一级部门数据
        List<Dept> deptList = new ArrayList<>();
        List<Long> deptIdList = new ArrayList<>();
        String departName = "All";
        if (isAdmin) {
            //管理员查询所有
            deptList = deptRepository.findByPidIsNull();
        } else {
            //非管理员只能查看权限内部门
            Long deptId = SecurityUtils.getCurrentDeptId();
            Dept dept = deptRepository.findById(deptId).orElseGet(Dept::new);
            if (dept != null) {
                departName = dept.getName();
            }
            deptIdList.add(deptId);
//            List<FileDept> data = fileDeptService.findByPid(deptId);
            // 然后把子节点的ID都加入到集合中
//            deptIdList.addAll(fileDeptService.getDeptChildren(data));
            deptList = deptRepository.findAllById(deptIdList);
        }
        Map<String, Object> map = new HashMap<>();
        if (ValidationUtil.isNotEmpty(deptList)) {
            List<String> xAxisList = new ArrayList<>();
            List<CommonDTO> yAxisList = new ArrayList<>();
            deptList.forEach(dept -> {
                //xAxis:dept.getName()
                xAxisList.add(dept.getName());
                //yAxis:部门（包含子部门）对应的文件数目
                CommonDTO yAxis = new CommonDTO();
                Set<Long> deptIds = new HashSet<>();
                deptIds.add(dept.getId());
                // 先查找是否存在子节点
                List<FileDept> data = fileDeptService.findByPid(dept.getId());
                // 然后把子节点的ID都加入到集合中
                deptIds.addAll(fileDeptService.getDeptChildren(data));
                int count = localStorageRepository.getCountByDeptIdIn(deptIds);
                yAxis.setId(dept.getId());
                yAxis.setName(dept.getName());
                yAxis.setValue(String.valueOf(count));
                yAxisList.add(yAxis);
            });
            map.put("xAxis", xAxisList);
            map.put("yAxis", yAxisList);
            map.put("scope", departName);
        }
        return map;
    }

    @Override
    public Map<String, Object> queryIssuesByExecuteType() {
        List<DictDetail> dictDetails = dictDetailRepository.findDictDetailByDictId(CommonConstants.DICT_E_EXECUTE);
        Map<String, Object> map = new HashMap<>();
        if (ValidationUtil.isNotEmpty(dictDetails)) {
            List<String> xAxisList = new ArrayList<>();
            List<CommonDTO> yAxisList = new ArrayList<>();
            dictDetails.forEach(detail -> {
                // 执行选择作为x轴数据
                xAxisList.add(detail.getValue());
                //yAxis:部门（包含子部门）对应的文件数目
                CommonDTO yAxis = new CommonDTO();
                List<Issue> list = new ArrayList<>();
                list = issueRepository.findByHasReport(detail.getValue());

                yAxis.setName(detail.getValue());
                yAxis.setValue(String.valueOf(list.size()));

                yAxisList.add(yAxis);

            });
            xAxisList.add("待审批");
            CommonDTO yAxis = new CommonDTO();
            List<Issue> list = new ArrayList<>();
            list = issueRepository.findByHasReportIsNull();

            yAxis.setName("待审批");
            yAxis.setValue(String.valueOf(list.size()));
            yAxisList.add(yAxis);

            map.put("xAxis", xAxisList);
            map.put("yAxis", yAxisList);
            map.put("scope", "全部");
        }
        return map;
    }

    @Override
    public Map<String, Object> queryAuditPlansByType() {
        Map<String, Object> map = new HashMap<>();
        Dict dict = dictRepository.findByName(CommonConstants.AUDIT_TYPE);
        if (dict != null) {
            List<DictDetail> dictDetails = dictDetailRepository.findDictDetailByDictId(dict.getId());
            if (ValidationUtil.isNotEmpty(dictDetails)) {
                List<String> xAxisList = new ArrayList<>();
                List<CommonDTO> yAxisList = new ArrayList<>();
                dictDetails.forEach(detail -> {
                    // 执行选择作为x轴数据
                    xAxisList.add(detail.getValue());
                    //yAxis:部门（包含子部门）对应的文件数目
                    CommonDTO yAxis = new CommonDTO();
                    List<AuditPlan> list = new ArrayList<>();
                    list = planRepository.findByType(detail.getValue());
                    yAxis.setName(detail.getValue());
                    yAxis.setValue(String.valueOf(list.size()));
                    yAxisList.add(yAxis);
                });
                map.put("xAxis", xAxisList);
                map.put("yAxis", yAxisList);
                map.put("scope", "全部");
            }
        }
        return map;
    }

    @Override
    public Map<String, Object> queryAuditorBySystem() {
        Map<String, Object> map = new HashMap<>();
        Dict dict = dictRepository.findByName(CommonConstants.AUDIT_SYSTEM_TYPE);
        if (dict != null) {
            List<DictDetail> dictDetails = dictDetailRepository.findDictDetailByDictId(dict.getId());
            if (ValidationUtil.isNotEmpty(dictDetails)) {
                List<CommonDTO> list = new ArrayList<>();
                dictDetails.forEach(detail -> {
                    CommonDTO commonDTO = new CommonDTO();
                    Integer count = auditorRepository.findBySystem(detail.getValue());
                    commonDTO.setId(detail.getId());
                    commonDTO.setName(detail.getValue());
                    commonDTO.setValue(String.valueOf(count));
                    list.add(commonDTO);
                });
                // map.put("content", totalCount);
                map.put("totalElements", list);
                map.put("scope", "全部");
            }
        }
        return map;
    }

    @Override
    public Map<String, Object> queryAuditorByReason() {
        Map<String, Object> map = new HashMap<>();
        Dict dict = dictRepository.findByName(CommonConstants.AUDIT_REASON_TYPE);
        if (dict != null) {
            List<DictDetail> dictDetails = dictDetailRepository.findDictDetailByDictId(dict.getId());
            if (ValidationUtil.isNotEmpty(dictDetails)) {
                List<CommonDTO> list = new ArrayList<>();
                dictDetails.forEach(detail -> {
                    CommonDTO commonDTO = new CommonDTO();
                    Integer count = planRepository.findByReason(detail.getValue());
                    commonDTO.setId(detail.getId());
                    commonDTO.setName(detail.getValue());
                    commonDTO.setValue(String.valueOf(count));
                    list.add(commonDTO);
                });
                // map.put("content", totalCount);
                map.put("totalElements", list);
                map.put("scope", "全部");
            }
        }
        return map;
    }

    @Override
    public Map<String, Object> queryAllByCond(OverviewQueryCriteria criteria) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        // 获取部门及其子集合
        List<Long> deptIdList = new ArrayList<>();
        //非管理员则只能查看权限内部门对应的文件数量
        Long deptId = SecurityUtils.getCurrentDeptId();
        deptIdList.add(deptId);
        List<FileDept> data = fileDeptService.findByPid(deptId);
        // 然后把子节点的ID都加入到集合中
        deptIdList.addAll(fileDeptService.getDeptChildren(data));
        String categoryName = criteria.getName();
        Map<String, Object> map = new HashMap<>();
        switch (categoryName) {
            case "departments":
                //查询部门数据新增趋势信息
                List<Dept> deptList = deptRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
                List<String> dTimeList = new ArrayList<>();
                List<CommonDTO> departments = new ArrayList<>();
                if (ValidationUtil.isNotEmpty(deptList)) {
                    //设置一下
                    deptList.forEach(dept -> {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        String time = dateFormat.format(dept.getCreateTime());
                        dTimeList.add(time);
                    });
                }
                List<String> newTimeList = dTimeList.stream().distinct().sorted().collect(Collectors.toList());
                if (ValidationUtil.isNotEmpty(newTimeList)) {
                    newTimeList.forEach(time -> {
                        Integer count = deptRepository.getCountByDateTime(time);
                        CommonDTO commonDTO = new CommonDTO();
                        commonDTO.setName(time);
                        commonDTO.setValue(String.valueOf(count));
                        departments.add(commonDTO);
                    });
                }
                map.put("category", departments);
                break;
            case "members":
                //查询人员数新增趋势信息
                List<User> userList = userRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
                List<String> mTimeList = new ArrayList<>();
                List<CommonDTO> members = new ArrayList<>();
                if (ValidationUtil.isNotEmpty(userList)) {
                    //设置一下
                    userList.forEach(user -> {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        String time = dateFormat.format(user.getCreateTime());
                        mTimeList.add(time);
                    });
                }
                List<String> newMTimeList = mTimeList.stream().distinct().sorted().collect(Collectors.toList());
                if (ValidationUtil.isNotEmpty(newMTimeList)) {
                    newMTimeList.forEach(time -> {
                        Integer count = userRepository.getCountByDateTime(time);
                        CommonDTO commonDTO = new CommonDTO();
                        commonDTO.setName(time);
                        commonDTO.setValue(String.valueOf(count));
                        members.add(commonDTO);
                    });
                }
                map.put("category", members);
                break;
            case "tasks":
                //查询人员数新增趋势信息
                // criteria.setApprovedBy(currentUserId);
                List<ToolsTask> taskList = toolsTaskRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
                List<String> tTimeList = new ArrayList<>();
                List<CommonDTO> tasks = new ArrayList<>();
                if (ValidationUtil.isNotEmpty(taskList)) {
                    //设置一下
                    taskList.forEach(user -> {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        String time = dateFormat.format(user.getCreateTime());
                        tTimeList.add(time);
                    });
                }
                List<String> newTTimeList = tTimeList.stream().distinct().sorted().collect(Collectors.toList());
                if (ValidationUtil.isNotEmpty(newTTimeList)) {
                    newTTimeList.forEach(time -> {
                        Integer count = toolsTaskRepository.getTaskCountByDateTime(time);
                        Integer personalCount = toolsTaskRepository.getPersonalTaskCountByDateTime(time, currentUserId);
                        CommonDTO commonDTO = new CommonDTO();
                        commonDTO.setName(time);
                        commonDTO.setValue(String.valueOf(count));
                        commonDTO.setOtherValue(String.valueOf(personalCount));
                        tasks.add(commonDTO);
                    });
                }
                map.put("category", tasks);
                break;
            case "fileCategories":
                //查询文件分类数据趋势信息
                List<FileCategory> categoryList = fileCategoryRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
                List<String> cTimeList = new ArrayList<>();
                List<CommonDTO> fileCategories = new ArrayList<>();
                if (ValidationUtil.isNotEmpty(categoryList)) {
                    //设置一下
                    categoryList.forEach(user -> {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        String time = dateFormat.format(user.getCreateTime());
                        cTimeList.add(time);
                    });
                }
                List<String> newCTimeList = cTimeList.stream().distinct().sorted().collect(Collectors.toList());
                if (ValidationUtil.isNotEmpty(newCTimeList)) {
                    newCTimeList.forEach(time -> {
                        Integer count = fileCategoryRepository.getCountByDateTime(time);
                        CommonDTO commonDTO = new CommonDTO();
                        commonDTO.setName(time);
                        commonDTO.setValue(String.valueOf(count));
                        fileCategories.add(commonDTO);
                    });
                }
                map.put("category", fileCategories);
                break;
            case "issues":
                //查询文件分类数据趋势信息
                List<Issue> issueLists = issueRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
                List<String> iTimeList = new ArrayList<>();
                List<CommonDTO> issues = new ArrayList<>();
                if (ValidationUtil.isNotEmpty(issueLists)) {
                    //设置一下
                    issueLists.forEach(user -> {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        String time = dateFormat.format(user.getCreateTime());
                        iTimeList.add(time);
                    });
                }
                List<String> newITimeList = iTimeList.stream().distinct().sorted().collect(Collectors.toList());
                if (ValidationUtil.isNotEmpty(newITimeList)) {
                    newITimeList.forEach(time -> {
                        Integer count = issueRepository.getCountByDateTime(time);
                        CommonDTO commonDTO = new CommonDTO();
                        commonDTO.setName(time);
                        commonDTO.setValue(String.valueOf(count));
                        issues.add(commonDTO);
                    });
                }
                map.put("category", issues);
                break;
            case "localStorages":
                //查询文件数据趋势信息
                List<LocalStorageSmall> fileList = storageSmallRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
                List<String> fTimeList = new ArrayList<>();
                List<CommonDTO> files = new ArrayList<>();
                if (ValidationUtil.isNotEmpty(fileList)) {
                    //设置一下
                    fileList.forEach(user -> {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        String time = dateFormat.format(user.getCreateTime());
                        fTimeList.add(time);
                    });
                }
                List<String> newFTimeList = fTimeList.stream().distinct().sorted().collect(Collectors.toList());
                if (ValidationUtil.isNotEmpty(newFTimeList)) {
                    newFTimeList.forEach(time -> {
                        Integer count = storageSmallRepository.getCountByDateTime(time);
                        Integer departCount = localStorageRepository.getDepartCountByDateTime(time, deptIdList);
                        CommonDTO commonDTO = new CommonDTO();
                        commonDTO.setName(time);
                        commonDTO.setValue(String.valueOf(count));
                        commonDTO.setOtherValue(String.valueOf(departCount));
                        files.add(commonDTO);
                    });
                }
                map.put("category", files);
                break;
            default:
                break;
        }
        return map;
    }

}

