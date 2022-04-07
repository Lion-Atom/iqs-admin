/*
 *  Copyright 2019-2020 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package me.zhengjie.service.impl;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.config.FileProperties;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.*;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.repository.*;
import me.zhengjie.service.FileDeptService;
import me.zhengjie.service.dto.*;
import me.zhengjie.service.mapstruct.FileApprovalProcessMapper;
import me.zhengjie.service.mapstruct.LocalStorageMapper;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.mapstruct.PreTrailMapper;
import me.zhengjie.utils.*;
import me.zhengjie.service.LocalStorageService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Tong Minjie
 * @date 2021-07-07
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "localStorage")
public class LocalStorageServiceImpl implements LocalStorageService {

    private final LocalStorageRepository localStorageRepository;
    private final LocalStorageMapper localStorageMapper;
    private final FileProperties properties;
    private final BindingLocalStorageRepository bindingLocalStorageRepository;
    private final BindingDeptRepository bindingDeptRepository;
    private final LocalStorageSmallRepository localStorageSmallRepository;
    private final RedisUtils redisUtils;
    private final ToolsLogRepository toolsLogRepository;
    private final PreTrailRepository preTrailRepository;
    private final FileApprovalProcessRepository fileApprovalProcessRepository;
    private final PreTrailMapper preTrailMapper;
    private final ApproverRepository approverRepository;
    private final FileDeptRepository fileDeptRepository;
    private final FileApprovalProcessMapper fileApprovalProcessMapper;
    private final IssueFileRepository issueFileRepository;
    private final CommonUtils commonUtils;
    private final FileDeptService fileDeptService;
    private final LocalStorageTempRepository localStorageTempRepository;
    private final BindingDeptTempRepository bindingDeptTempRepository;
    private final BindingLocalStorageTempRepository bindingLocalStorageTempRepository;
    private final LocalStorageTempSmallRepository localStorageTempSmallRepository;

    @Override
    //当前文件最好不好设置缓存，或者设置定时更新缓存
//    @Cacheable(key = "'fileList:query:'+#criteria.id", unless = "#criteria.id==null")
    public Object queryAll(LocalStorageQueryCriteria criteria, Pageable pageable) {

        // 判断使用场景：是否是引用为参考文献时候使用
        if (criteria.getIsReference()) {
            // 引用文件须排除【报废文件】
            criteria.setFileStatusExternal(CommonConstants.OBSOLETE_STATUS);
        }

        Page<LocalStorage> page = localStorageRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        /*// 判断是否在可查看范围
        Map<String, Object> map = new HashMap<>();
        List<LocalStorage> list = new ArrayList<>();
        long total = 0L;
        if (ValidationUtil.isNotEmpty(page.getContent())) {
            list = page.getContent();
            if (criteria.getIsReference()) {
                total = list.size();
            } else {
                total = page.getTotalElements();
            }

        }
        map.put("content", list);
        map.put("totalElements", total);
        return map;*/
        return PageUtil.toPage(page.map(localStorageMapper::toDto));
    }

    @Override
    public List<LocalStorageSmall> queryAllByIds(Long[] ids) {
        //设置独立的localStorage不关联任何元素，是防止循环中嵌套循环，甚至造成循环自己拿数据造成栈溢出StackOverFlowError
        return localStorageSmallRepository.findAllByIds(ids);
    }

    @Override
    public List<LocalStorageTempSmall> queryByTempIds(Long[] ids) {
        //设置独立的localStorage不关联任何元素，是防止循环中嵌套循环，甚至造成循环自己拿数据造成栈溢出StackOverFlowError
        return localStorageTempSmallRepository.findAllByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PreTrail uploadPreTrail(Long id, String approvalStatus, MultipartFile multipartFile) {

        LocalStorage old = localStorageRepository.findById(id).orElseGet(LocalStorage::new);

        // 前置判断是否允许上传
        List<FileApprovalProcess> waitList = new ArrayList<>();
        FileApprovalProcess ps = new FileApprovalProcess();
        if (approvalStatus.equals(CommonConstants.WAITING_FOR_STATUS)) {
            ps = fileApprovalProcessRepository.findByLastCreateTime(id, CommonConstants.NOT_DEL, CommonConstants.FILE_TYPE_LIST);
            if (ps != null) {
                // 获取同组数据
                String processNo = ps.getProcessNo().substring(0, ps.getProcessNo().length() - 1) + "%";
                waitList = fileApprovalProcessRepository.findTeamProcessList(id, CommonConstants.NOT_DEL, processNo);
            }
        } else if (old.getLocalStorageTemp() != null) {
            ps = fileApprovalProcessRepository.findByLastCreateTime(old.getLocalStorageTemp().getId(), CommonConstants.NOT_DEL, CommonConstants.FILE_TYPE_LIST);
            if (ps != null) {
                // 获取同组数据
                String processNo = ps.getProcessNo().substring(0, ps.getProcessNo().length() - 1) + "%";
                waitList = fileApprovalProcessRepository.findTeamProcessList(old.getLocalStorageTemp().getId(), CommonConstants.NOT_DEL, processNo);
            }
        }

        if (ValidationUtil.isNotEmpty(waitList)) {
            List<String> results = new ArrayList<>();
            waitList.forEach(p -> {
                if (p.getApprovedResult() != null) {
                    results.add(p.getApprovedResult());
                }
            });
                /*if (results.size() != 0 && results.size() < processList.size()) {
                    throw new BadRequestException("当前修改的文件已进入审批流程，请流程结束后再试！");
                } else if (results.size() == 0) {
                   // todo 尚未审批暂时不处理
                }*/
            if (results.size() != 0 && results.size() < waitList.size()) {
                throw new BadRequestException("当前修改的文件正被审批，请流程结束后再改版！");
            }
        }

        FileUtil.checkSize(properties.getMaxSize(), multipartFile.getSize());
        String suffix = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
        String type = FileUtil.getFileType(suffix);
        File file = FileUtil.upload(multipartFile, properties.getPath().getPath() + type + File.separator);
        if (ObjectUtil.isNull(file)) {
            throw new BadRequestException("Upload Error! 上传失败");
        }
        try {
            LocalStorage localStorage = localStorageRepository.findById(id).orElseGet(LocalStorage::new);
            ValidationUtil.isNull(localStorage.getId(), "LocalStorage", "id", id);
            // 创建编号
            String processNoFormat = createNoFormat();

            //初始版本,初始文件自然不变版本
            // todo 支持自定义版本号
            String version = getNextVersion(localStorage);
            if (approvalStatus.equals(CommonConstants.OBSOLETED_STATUS)) {
                // 获取之前的任务版本号重新发布
                List<PreTrail> list = preTrailRepository.findAllByStorageId(id, CommonConstants.NOT_DEL);
                if (ValidationUtil.isNotEmpty(list)) {
                    version = list.get(0).getVersion();
                }
            }
            //创建待审批任务
            PreTrail preTrail = new PreTrail();
            preTrail.setPreTrailNo(processNoFormat);
            preTrail.setStorageId(localStorage.getId());
            preTrail.setSrcPath(localStorage.getPath());
            preTrail.setTarPath(file.getPath());
            preTrail.setSuffix(suffix);
            preTrail.setVersion(version);
            preTrail.setSize(FileUtil.getSize(multipartFile.getSize()));
            preTrail.setFileType(type);
            preTrail.setType(CommonConstants.TRAIL_TYPE_FILE);
            preTrail.setRealName(file.getName());
            preTrail.setChangeType(CommonConstants.UPGRADE_VERSION);
            preTrail.setChangeDesc("新文件[" + FileUtil.getFileNameNoEx(multipartFile.getOriginalFilename()) + "]替代原文件[" + localStorage.getRealName() + "],文件版本为" + version);
            preTrail.setIsDel(0L);
            // 根据当前登录人查找其上级领导
            // todo 允许选择临时审批者
            Long superior = SecurityUtils.getCurrentUserSuperior();
            Long currentDeptId = SecurityUtils.getCurrentDeptId();
            if (superior != null) {
                preTrail.setApprovedBy(superior);
            } else {
                // todo 循环到一级部门（仅在顶级部门之下）
                // 当前创建者是部门master,需要向上级部门或质量部master发起审批任务
                //当前所在部门
                FileDept dept = fileDeptRepository.findById(currentDeptId).orElseGet(FileDept::new);
                //获取上级部门
                if (dept.getPid() != null) {
                    //非顶级部门
                    // 上级部门
                    FileDept pDept = fileDeptRepository.findById(dept.getPid()).orElseGet(FileDept::new);

                    if (pDept.getPid() != null) {
                        // 非一级部门
                        List<Approver> list = approverRepository.findByDeptIdAndIsMaster(pDept.getId(), true);
                        if (ValidationUtil.isNotEmpty(list)) {
                            preTrail.setApprovedBy(list.get(0).getId());
                        } else {
                            throw new BadRequestException("上级部门[" + pDept.getName() + "]未设置部门管理者，请联系管理员！");
                        }
                    } else {
                        // 一级部门则直接提交质量部master审批
                        preTrail.setApprovedBy(commonUtils.getZlbMaster());
                    }
                } else {
                    // 当前创建者所在部门是顶级部门，向本部门msater发起任务审批（允许自批），向质量部门master发起审批任务请求
                    //无上级，则自批
                    preTrail.setApprovedBy(SecurityUtils.getCurrentUserId());
                }
            }

            // create审批进度表
            // todo 初始化整个审批流程进度 例：研发->研发经理->研发总监->质量部经理(所有审批最终审批者：质量部经理)
            FileApprovalProcess process = new FileApprovalProcess();
            process.setProcessNo(processNoFormat);
            process.setBindingId(localStorage.getId());
            process.setSrcPath(localStorage.getPath());
            process.setTarPath(file.getPath());
            process.setSuffix(suffix);
            process.setVersion(version);
            process.setSize(FileUtil.getSize(multipartFile.getSize()));
            process.setType(type);
            process.setRealName(file.getName());
            process.setChangeType(CommonConstants.UPGRADE_VERSION);
            process.setChangeDesc("新文件[" + FileUtil.getFileNameNoEx(multipartFile.getOriginalFilename()) + "]替代原文件[" + localStorage.getRealName() + "],文件版本为" + version);
            process.setIsDel(0L);

            List<FileApprovalProcess> processList = new ArrayList<>();
            //todo 循环到上级的supperiorId = NULL 部门循环结束->质量部门->结束
            createProcessList(processNoFormat, superior, currentDeptId, process, processList);

            return preTrailRepository.save(preTrail);

        } catch (Exception e) {
            FileUtil.del(file);
            throw e;
        }
    }

    private String createNoFormat() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmssS");
        return StringUtils.getPinyin(SecurityUtils.getCurrentDeptName()) + "-" + format.format(date);
    }

    private String createTempNoFormat() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmssS");
        return StringUtils.getPinyin(SecurityUtils.getCurrentDeptName()) + "-" + "LS" + "-" + format.format(date);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LocalStorage cover(Long id, MultipartFile multipartFile) {

        FileUtil.checkSize(properties.getMaxSize(), multipartFile.getSize());
        String suffix = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
        String type = FileUtil.getFileType(suffix);
        File file = FileUtil.upload(multipartFile, properties.getPath().getPath() + type + File.separator);
        if (ObjectUtil.isNull(file)) {
            throw new BadRequestException("Upload Error! 上传失败");
        }
        try {
            LocalStorage localStorage = localStorageRepository.findById(id).orElseGet(LocalStorage::new);
            ValidationUtil.isNull(localStorage.getId(), "LocalStorage", "id", id);
            //初始版本,初始文件自然不变版本
            String oldVersion = localStorage.getVersion();
            String prefix = oldVersion.substring(0, oldVersion.lastIndexOf("/") + 1);
            String suf = oldVersion.substring(oldVersion.lastIndexOf("/") + 1);
            int a = 0;
            try {
                a = Integer.valueOf(suf) + 1;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            String name = FileUtil.getFileNameNoEx(multipartFile.getOriginalFilename());
            localStorage.setRealName(file.getName());
            localStorage.setPath(file.getPath());
            localStorage.setSuffix(suffix);
            String version = prefix + String.valueOf(a);
            localStorage.setVersion(version);
            localStorage.setSize(FileUtil.getSize(multipartFile.getSize()));
            localStorage.setType(type);
            localStorage.setIsRevision(false);
            localStorage.setFileStatus("approved");
            localStorage.setName(name);
            localStorage.setRealName(file.getName());
            LocalStorage storage = localStorageRepository.save(localStorage);
            // 变更日志记录
            ToolsLog log = new ToolsLog();
            log.setLogType(CommonConstants.LOG_TYPE_FILE);
            log.setUsername(getUserName());
            log.setBindingId(localStorage.getId());
            log.setDescription("文件改版");
            log.setDescriptionDetail("文件升版，新文件替代原文件");
            toolsLogRepository.save(log);
            return storage;
        } catch (Exception e) {
            FileUtil.del(file);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void undo(Long id) {
        LocalStorage localStorage = localStorageRepository.findById(id).orElseGet(LocalStorage::new);
        // 删除待审批项---某个版本(待审批)的待审批项
        String version = getNextVersion(localStorage);
        //删除指定版本的任务
        preTrailRepository.delByStorageIdAndVersionAndChangeType(id, version, CommonConstants.UPGRADE_VERSION);
        //删除指定版本的审批进度任务
        fileApprovalProcessRepository.deleteAllByBindingIdAndVersionAndChangeType(id, version, CommonConstants.UPGRADE_VERSION);
    }

    private String getNextVersion(LocalStorage localStorage) {
        String oldVersion = localStorage.getVersion();
        String prefix = oldVersion.substring(0, oldVersion.lastIndexOf("/") + 1);
        String suf = oldVersion.substring(oldVersion.lastIndexOf("/") + 1);
        int a = 0;
        try {
            a = Integer.valueOf(suf) + 1;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return prefix + String.valueOf(a);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollBackCover(RollbackDto dto) {

        Long storageId = dto.getStorageId();
        LocalStorage localStorage = localStorageRepository.findById(storageId).orElseGet(LocalStorage::new);
        ValidationUtil.isNull(localStorage.getId(), "LocalStorage", "id", storageId);
        //初始版本,初始文件自然不变版本
        String nextVersion = getNextVersion(localStorage);

        // 回滚操作--简单地说：删除编辑后的一切操作，回滚编辑前的关联项
        // 修改之前状态时待审批
        String approvalStatus = dto.getApprovalStatus();
        String fileStatus = dto.getFileStatus();

        //删除指定时间之后的版本的任务 ---物理删除
        //非主键物理删除--低版本mysql存在不兼容情况
        // preTrailRepository.delByStorageIdAndVersionAndModifiedTime(storageId, nextVersion,dto.getLastModifiedDate());

        List<PreTrail> newTrailList = preTrailRepository.findAllByStorageIdAndVersionAndModifiedTime(storageId, nextVersion, dto.getLastModifiedDate());
        preTrailRepository.deleteAll(newTrailList);

        //删除指定时间之后指定版本的审批进度任务 ---物理删除
        //fileApprovalProcessRepository.deleteAllByBindingIdAndVersionAndModifiedTime(storageId, nextVersion, dto.getLastModifiedDate());
        List<FileApprovalProcess> newProcessList = fileApprovalProcessRepository.findByBindingIdAndVersionAndModifiedTime(storageId, nextVersion, dto.getLastModifiedDate());
        fileApprovalProcessRepository.deleteAll(newProcessList);

        //其他文件状态下可以切换版本引发文件的版本变更上传操作
        List<FileApprovalProcess> processList = fileApprovalProcessRepository.findMinByModifiedTime(storageId, nextVersion, dto.getLastModifiedDate());
        List<PreTrail> preTrailList = preTrailRepository.findMinByModifiedTime(storageId, nextVersion, dto.getLastModifiedDate());

        if (ValidationUtil.isNotEmpty(processList)) {
            processList.forEach(process -> {
                process.setIsDel(CommonConstants.NOT_DEL);
            });
            fileApprovalProcessRepository.saveAll(processList);
        }

        if (ValidationUtil.isNotEmpty(preTrailList)) {
            preTrailList.forEach(preTrail -> {
                preTrail.setIsDel(CommonConstants.NOT_DEL);
            });
            fileApprovalProcessRepository.saveAll(processList);
        }
    }

    @Override
    public Map<String, Object> getPreTrailByFileId(Long fileId, Boolean latestVersion) {

        LocalStorage localStorage = localStorageRepository.findById(fileId).orElseGet(LocalStorage::new);
        ValidationUtil.isNull(localStorage.getId(), "LocalStorage", "id", fileId);

        /**
         * latestVersion = true 获取最新一批的任务
         * 1.最新创建时间-查询任务编号
         * 2.根据任务编号查询任务列表
         */
        List<PreTrail> list = new ArrayList<>();
        if (latestVersion) {
            // 1.查询有效记录中最新一条记录的创建时间，根据最新时间获取该记录的编号，而后根据编号查询同组数据
            list = preTrailRepository.findFileLatestRecords(fileId, CommonConstants.NOT_DEL);
        } else {
            // id递增方式获取相关的全部任务
            list = preTrailRepository.findAllByStorageIdSortByVersion(fileId, CommonConstants.NOT_DEL);
        }

        Map<String, Object> map = new HashMap<>();
        List<PreTrailDto> dtoList = new ArrayList<>();

        setApproverNameAndComment(list, map);
        map.put("content", list);
        return map;
    }

    @Override
    public Map<String, Object> getPreTrailByFileTempId(Long fileTempId, Boolean latestVersion) {

        LocalStorageTempSmall localStorage = localStorageTempSmallRepository.findById(fileTempId).orElseGet(LocalStorageTempSmall::new);
        ValidationUtil.isNull(localStorage.getId(), "LocalStorageTempSmall", "id", fileTempId);

        /**
         * latestVersion = true 获取最新一批的任务
         * 1.最新创建时间-查询任务编号
         * 2.根据任务编号查询任务列表
         */
        List<PreTrail> list = new ArrayList<>();
        if (latestVersion) {
            /*
            // 根据版本查询文件关联的任务
            String version = localStorage.getVersion();
            // 非草稿状态，说明是版本覆盖操作，因此需要取下个版本（预发布版本）
            if (!localStorage.getFileStatus().equals(CommonConstants.DRAFT_STATUS) && !localStorage.getApprovalStatus().equals(CommonConstants.APPROVED_STATUS)) {
                version = getNextVersion(localStorage);
            }

            // 1.获取版本 2.根据版本获取其对应的任务
            list = preTrailRepository.findAllByStorageIdAndVersion(fileId, version, CommonConstants.NOT_DEL);
            */

            // 1.查询有效记录中最新一条记录的创建时间，根据最新时间获取该记录的编号，而后根据编号查询同组数据
            list = preTrailRepository.findFileLatestRecords(fileTempId, CommonConstants.NOT_DEL);
        } else {
            // id递增方式获取相关的全部任务
            list = preTrailRepository.findAllByStorageIdSortByVersion(fileTempId, CommonConstants.NOT_DEL);
        }

        Map<String, Object> map = new HashMap<>();
        List<PreTrailDto> dtoList = new ArrayList<>();

        setApproverNameAndComment(list, map);
        map.put("content", list);
        return map;
    }

    private void setApproverNameAndComment(List<PreTrail> list, Map<String, Object> map) {
        List<PreTrailDto> dtoList;
        if (ValidationUtil.isNotEmpty(list)) {
            dtoList = preTrailMapper.toDto(list);
            dtoList.forEach(dto -> {
                Approver approver = approverRepository.findById(dto.getApprovedBy()).orElseGet(Approver::new);
                ValidationUtil.isNull(approver.getId(), "Approver", "id", dto.getApprovedBy());
                dto.setApprover(approver.getUsername());
            });
            // 提取驳回理由
            if (list.get(list.size() - 1).getApproveResult() != null && !list.get(list.size() - 1).getApproveResult()) {
                String comment = list.get(list.size() - 1).getComment();
                map.put("comment", comment);
            }
            // 提取当前审批人
            // String currApprover = dtoList.get(list.size() - 1).getApprover();
            Approver cur = approverRepository.findById(dtoList.get(list.size() - 1).getApprovedBy()).orElseGet(Approver::new);
            map.put("currApprover", cur);
        }
    }

    @Override
    public Map<String, Object> getApprovalProcessByFileId(Long fileId) {
        Map<String, Object> map = new HashMap<>();
        // id递增方式获取任务--默认获取最新的审批进度信息数据
        List<FileApprovalProcess> list = new ArrayList<>();
//        List<FileApprovalProcess> list = fileApprovalProcessRepository.findAllByLastCreateTime(fileId, CommonConstants.NOT_DEL);
        // 获取最新的审批流程，然后根据他去查找同组数据 --- 优化sql语句
        FileApprovalProcess process = fileApprovalProcessRepository.findByLastCreateTime(fileId, CommonConstants.NOT_DEL, CommonConstants.FILE_TYPE_LIST);
        if (process != null) {
            // 获取同组数据
            String processNo = process.getProcessNo().substring(0, process.getProcessNo().length() - 1) + "%";
            list = fileApprovalProcessRepository.findTeamProcessList(fileId, CommonConstants.NOT_DEL, processNo);
        }
        List<ApprovalFileProcessDto> dtoList = new ArrayList<>();
        String waitDuration = null;
        if (ValidationUtil.isNotEmpty(list)) {
            // 创建时间一致
            waitDuration = StringUtils.getWaitingTime(list.get(0).getCreateTime());
            dtoList = fileApprovalProcessMapper.toDto(list);
            // 获取到审批者姓名
            dtoList.forEach(dto -> {
                Approver approver = approverRepository.findById(dto.getApprovedBy()).orElseGet(Approver::new);
                ValidationUtil.isNull(approver.getId(), "Approver", "id", dto.getApprovedBy());
                dto.setApprover(approver.getUsername());
                if (dto.getApprovedResult() == null) {
                    dto.setApprovedResult("待审批");
                }
            });
        }
        map.put("waitDuration", waitDuration);
        map.put("content", dtoList);
        return map;
    }

    @Override
    public List<LocalStorageSmall> findByExample(FileQueryDto queryDto) {
        return localStorageSmallRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, queryDto, criteriaBuilder));
    }

    @Override
    public Map<String, Object> getApprovalProcessByFileIdV2(Long fileId, Boolean isTemp) {
        Map<String, Object> map = new HashMap<>();
        // id递增方式获取任务--默认获取最新的审批进度信息数据
        List<FileApprovalProcess> list = new ArrayList<>();
//        List<FileApprovalProcess> list = fileApprovalProcessRepository.findAllByLastCreateTime(fileId, CommonConstants.NOT_DEL);
        // 获取最新的审批流程，然后根据他去查找同组数据 --- 优化sql语句
        FileApprovalProcess process = new FileApprovalProcess();
        if (isTemp) {
            // 临时
            process = fileApprovalProcessRepository.findByLastCreateTime(fileId, CommonConstants.NOT_DEL, CommonConstants.FILE_TYPE_LIST);
        } else {
            // 正式
            process = fileApprovalProcessRepository.findByLastCreateTime(fileId, CommonConstants.NOT_DEL, CommonConstants.FILE_TYPE_LIST);
        }

        if (process.getId() != null) {
            // 获取同组数据
            String processNo = process.getProcessNo().substring(0, process.getProcessNo().length() - 1) + "%";
            list = fileApprovalProcessRepository.findTeamProcessList(fileId, CommonConstants.NOT_DEL, processNo);
        }
        List<ApprovalFileProcessDto> dtoList = new ArrayList<>();
        String waitDuration = null;
        if (ValidationUtil.isNotEmpty(list)) {
            // 创建时间一致
            waitDuration = StringUtils.getWaitingTime(list.get(0).getCreateTime());
            dtoList = fileApprovalProcessMapper.toDto(list);
            // 获取到审批者姓名
            dtoList.forEach(dto -> {
                Approver approver = approverRepository.findById(dto.getApprovedBy()).orElseGet(Approver::new);
                ValidationUtil.isNull(approver.getId(), "Approver", "id", dto.getApprovedBy());
                dto.setApprover(approver.getUsername());
                if (dto.getApprovedResult() == null) {
                    dto.setApprovedResult("待审批");
                }
            });
        }
        map.put("waitDuration", waitDuration);
        map.put("content", dtoList);
        return map;
    }

    @Override
    public Map<String, Object> getApprovalProcessListByFileId(Long fileId) {
        Map<String, Object> map = new HashMap<>();
        List<FileApprovalProcess> list = fileApprovalProcessRepository.findByBindingId(fileId, CommonConstants.NOT_DEL, CommonConstants.FILE_TYPE_LIST);
        map.put("count", list.size());
        map.put("content", list);
        return map;
    }

    @Override
    public List<LocalStorageDto> queryAll(LocalStorageQueryCriteria criteria) {
        return localStorageMapper.toDto(localStorageRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder)));
    }

    @Override
//    @Cacheable(key = "'id:' + #p0")
    public LocalStorageDto findById(Long id) {
        LocalStorage localStorage = localStorageRepository.findById(id).orElseGet(LocalStorage::new);
        ValidationUtil.isNull(localStorage.getId(), "LocalStorage", "id", id);
        LocalStorageDto dto = localStorageMapper.toDto(localStorage);
        // 判定是否有下载查看权
        Boolean isAdmin = SecurityUtils.getIsAdmin();
        List<Long> scopes = SecurityUtils.getCurrentUserDataScope();
        // 初始化默认设置为全员可查看下载 [hasDownloadAuthority:true]

        List<Long> deptIdList = new ArrayList<>();
        if (localStorage.getSecurityLevel().equals(CommonConstants.SECURITY_EXTERNAL) &&
                ValidationUtil.isNotEmpty(Collections.singletonList(localStorage.getBindDepts()))) {

            // 非作废文件，判断登陆人是否有权查看该文件
            // scopes 是当前登陆人员的权限部门
            // 存在指定部门，则需要对比当前登陆人所在部门是否在指定部门之下
            localStorage.getBindDepts().forEach(dept -> {
                deptIdList.add(dept.getDeptId());
            });

            // 字符化指定开放的部门-list
            dto.setBindDeptStr(fileDeptRepository.findByIdIn(deptIdList));

        }
        if (!isAdmin) {
            if (localStorage.getFileStatus().equals(CommonConstants.OBSOLETE_STATUS)) {
                // 作废文件非管理员无权下载查看
                dto.setHasDownloadAuthority(false);
            } else {
                List<Long> bindDeptIds = new ArrayList<>();
                // 非作废文件
                if (localStorage.getSecurityLevel().equals(CommonConstants.SECURITY_EXTERNAL)) {
                    if (ValidationUtil.isNotEmpty(Collections.singletonList(localStorage.getBindDepts()))) {

                        bindDeptIds = fileDeptService.getChildrenIds(deptIdList);
                        // 需要添加文件自身所在部门
                        bindDeptIds.add(localStorage.getFileDept().getId());
                        if (Collections.disjoint(bindDeptIds, scopes)) {
                            // disjoint返回true说明无交集
                            dto.setHasDownloadAuthority(false);
                        }
                    }
                } else if (localStorage.getSecurityLevel().equals(CommonConstants.SECURITY_INTERNAL)) {
                    bindDeptIds.add(localStorage.getFileDept().getId());
                    if (Collections.disjoint(bindDeptIds, scopes)) {
                        // disjoint返回true说明无交集
                        dto.setHasDownloadAuthority(false);
                    }
                }
            }
        }
        // 管理员无视限制,都可查看和下载
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LocalStorage create(String name, String version, Long fileLevelId, Long fileCategoryId, Long deptId, String fileStatus,
                               String fileType, String securityLevel, Timestamp expirationTime, String fileDesc,
                               MultipartFile multipartFile, List<Long> bindFiles, Set<Long> bindDepts, Boolean isTempPicture) {
        String initName = multipartFile.getOriginalFilename();
        //重名校验
        if (StringUtils.isBlank(name)) {
            // 同部门、同分类不允许同名
            LocalStorage old = localStorageRepository.findByNameAndDeptIdAndCategoryId(name, fileCategoryId, deptId);
            if (old != null) {
                throw new EntityExistException(LocalStorage.class, "name", name);
            }
        }
        //initName组成：名称+“.”+“type”，例如：WI30903【各中心厂设变案前置作业规定】.doc
        String keyword = null;
        String initType = null;
        if (!isTempPicture && initName != null) {
            //截取最初文件名称
            keyword = initName.substring(0, initName.lastIndexOf("."));
            //截取文件自身类型,例如：doc、xlsx等
            initType = initName.substring(initName.lastIndexOf(".") + 1);
            List<LocalStorage> list = localStorageRepository.findRealNameByDeptIdAndCategoryIdAndRealName(deptId, fileCategoryId, keyword, initType);
            if (ValidationUtil.isNotEmpty(list)) {
                throw new BadRequestException("It is not allowed to upload files with the same name under the same department and category,You can try modifying and trying again" +
                        " 同部门、同分类下不允许上传同名同类型文件,可适当修改后再试");
            }
        }
        //判断同部门同分类下是否存在重名文件
        //第一步：查询同部门、同分类下同名称文件
        //模糊匹配是否存在相同项目，如果存在则提示不允许保存
        FileUtil.checkSize(properties.getMaxSize(), multipartFile.getSize());
        String suffix = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
        String type = FileUtil.getFileType(suffix);
        File file = FileUtil.upload(multipartFile, properties.getPath().getPath() + type + File.separator);
        if (ObjectUtil.isNull(file)) {
            throw new BadRequestException("Upload Error! 上传失败");
        }
        //初始版本,初始文件自然不变版本
//        String version = "A/0";
        try {
            name = StringUtils.isBlank(name) ? FileUtil.getFileNameNoEx(multipartFile.getOriginalFilename()) : name;
            //赋值文件所在级别
            FileLevel level = new FileLevel();
            level.setId(fileLevelId);
            //赋值文件所属分类
            FileCategory category = new FileCategory();
            category.setId(fileCategoryId);
            //赋值文件所属分类
            FileDept fileDept = new FileDept();
            fileDept.setId(deptId);

            LocalStorage localStorage = new LocalStorage(
                    file.getName(),
                    name,
                    suffix,
                    file.getPath(),
                    type,
                    FileUtil.getSize(multipartFile.getSize()),
                    version,
                    false,
                    fileStatus,
                    "waitingfor",
                    fileType,
                    securityLevel,
                    expirationTime,
                    "创建" + name,
                    fileDesc,
                    level,
                    category,
                    null,
                    null,
                    fileDept
            );
            LocalStorage storage = null;
            if (isTempPicture) {
                localStorage.setIsDel(1L);
                storage = localStorageRepository.save(localStorage);
            } else {
                // 邮件发图片不设置任务和审批进度
                storage = localStorageRepository.save(localStorage);
                // 添加指定开放的部门
                // 添加参考文献 bindFiles
                if (ValidationUtil.isNotEmpty(Collections.singletonList(bindDepts))) {
                    List<BindingDept> bindingDeptList = new ArrayList<>();
                    LocalStorage finalStorage = storage;
                    bindDepts.forEach(bindDeptId -> {
                        BindingDept bind = new BindingDept();
                        bind.setStorageId(finalStorage.getId());
                        bind.setDeptId(bindDeptId);
                        bindingDeptList.add(bind);
                    });
                    bindingDeptRepository.saveAll(bindingDeptList);
                }
                // 添加参考文献 bindFiles
                if (ValidationUtil.isNotEmpty(bindFiles)) {
                    List<BindingLocalStorage> bindingLocalStorageList = new ArrayList<>();
                    LocalStorage finalStorage = storage;
                    bindFiles.forEach(bindId -> {
                        BindingLocalStorage bind = new BindingLocalStorage();
                        bind.setStorageId(finalStorage.getId());
                        bind.setBindingStorageId(bindId);
                        bindingLocalStorageList.add(bind);
                    });
                    bindingLocalStorageRepository.saveAll(bindingLocalStorageList);
                }

                // 创建编号：任务编号和待审批进度信息编号
                String processNoFormat = createNoFormat();

                //创建待审批任务
                PreTrail preTrail = new PreTrail();
                preTrail.setPreTrailNo(processNoFormat);
                preTrail.setStorageId(localStorage.getId());
                preTrail.setSrcPath(null);
                preTrail.setTarPath(file.getPath());
                preTrail.setSuffix(suffix);
                preTrail.setVersion(version);
                preTrail.setSize(FileUtil.getSize(multipartFile.getSize()));
                preTrail.setFileType(type);
                preTrail.setType(CommonConstants.TRAIL_TYPE_FILE);
                preTrail.setRealName(file.getName());
                preTrail.setChangeType(CommonConstants.NEW);
                preTrail.setChangeDesc("上传新文件：[" + name + "]");
                preTrail.setIsDel(0L);
                // 根据当前登录人查找其上级领导
                // todo 允许选择临时审批者
                Long superior = SecurityUtils.getCurrentUserSuperior();
                Long currentDeptId = SecurityUtils.getCurrentDeptId();
                if (superior != null) {
                    preTrail.setApprovedBy(superior);
                } else {
                    // 所在部门就是质量部
                    if (currentDeptId.equals(CommonConstants.ZL_DEPART)) {
                        //质量部的master自批就可以了
                        preTrail.setApprovedBy(SecurityUtils.getCurrentUserId());
                    } else {
                        // 循环到一级部门（仅在顶级部门之下）
                        // 当前创建者是部门master,需要向上级部门或质量部master发起审批任务
                        //当前所在部门
                        FileDept dept = fileDeptRepository.findById(currentDeptId).orElseGet(FileDept::new);

                        //获取上级部门
                        if (dept.getPid() != null) {
                            //非顶级部门
                            // 上级部门
                            FileDept pDept = fileDeptRepository.findById(dept.getPid()).orElseGet(FileDept::new);

                            if (pDept.getPid() != null) {
                                // 非一级部门
                                List<Approver> list = approverRepository.findByDeptIdAndIsMaster(pDept.getId(), true);
                                if (ValidationUtil.isNotEmpty(list)) {
                                    preTrail.setApprovedBy(list.get(0).getId());
                                } else {
                                    throw new BadRequestException("上级部门[" + pDept.getName() + "]未设置部门管理者，请联系管理员！");
                                }
                            } else {
                                // 一级部门则直接提交质量部master审批
                                preTrail.setApprovedBy(commonUtils.getZlbMaster());
                            }
                        } else {
                            // 当前创建者所在部门是顶级部门，向本部门msater发起任务审批（允许自批），向质量部门master发起审批任务请求
                            // 无上级，则自批，例如：总经理等人
                            preTrail.setApprovedBy(SecurityUtils.getCurrentUserId());
                        }
                    }
                }
                preTrailRepository.save(preTrail);
                // create审批进度表
                // 初始化整个审批流程进度 例：研发->研发经理->研发总监->质量部经理(所有审批最终审批者：质量部经理)
                FileApprovalProcess process = new FileApprovalProcess();
                // 设置审批流程进度编号
                process.setProcessNo(processNoFormat);
                process.setBindingId(localStorage.getId());
                process.setSrcPath(null);
                process.setTarPath(file.getPath());
                process.setSuffix(suffix);
                process.setVersion(version);
                process.setSize(FileUtil.getSize(multipartFile.getSize()));
                process.setType(type);
                process.setRealName(file.getName());
                process.setChangeType(CommonConstants.NEW);
                process.setChangeDesc("上传新文件：[" + name + "]");
                process.setIsDel(0L);

                List<FileApprovalProcess> processList = new ArrayList<>();
                // 循环到上级的supperiorId = NULL 部门循环结束->质量部门->结束
                createProcessList(processNoFormat, superior, currentDeptId, process, processList);

                //创建日志记录
                ToolsLog log = new ToolsLog();
                log.setBindingId(storage.getId());
                log.setLogType(CommonConstants.LOG_TYPE_FILE);
                log.setUsername(getUserName());
                log.setDescription("上传文件");
                log.setDescriptionDetail("创建：【" + name + "】文件");
                toolsLogRepository.save(log);
            }
            return storage;
        } catch (
                Exception e) {
            FileUtil.del(file);
            throw e;
        }

    }

    private void createProcessList(String processNoFormat, Long superior, Long currentDeptId, FileApprovalProcess process, List<FileApprovalProcess> processList) {
        if (!currentDeptId.equals(CommonConstants.ZL_DEPART)) {

            if (superior != null) {
                String sb = "STEP";
                int step = 0;
                // 判断本部门此上级是否是部门master,不是的话还要继续循环找下去
                Approver approver = approverRepository.findById(superior).orElseGet(Approver::new);
                Long lastSuperiorId = superior;
                if (approver.getIsDepartMaster()) {
                    step++;
                    FileApprovalProcess process1 = new FileApprovalProcess();
                    process1.copy(process);
                    process1.setProcessNo(processNoFormat + "_" + sb + step);
                    process1.setApprovedBy(superior);
                    processList.add(process1);
                } else {
                    Boolean isDepartMaster = approver.getIsDepartMaster();
                    Long userId = superior;
                    do {
                        step++;
                        Approver approver1 = approverRepository.findById(userId).orElseGet(Approver::new);
                        FileApprovalProcess process1 = new FileApprovalProcess();
                        process1.copy(process);
                        process1.setProcessNo(processNoFormat + "_" + sb + step);
                        process1.setApprovedBy(userId);
                        processList.add(process1);
                        userId = approver1.getSuperiorId();
                        isDepartMaster = approver1.getIsDepartMaster();
                        lastSuperiorId = approver1.getId();
                    } while (!isDepartMaster);
                }
                //存在直系上级
                // FileDept dept = fileDeptRepository.findById(currentDeptId).orElseGet(FileDept::new);
                // todo 直接上级的上级的部门管理者所在部门的上级部门
                FileDept dept = fileDeptRepository.findBySuperiorId(lastSuperiorId);
                //获取上级部门
                if (dept.getPid() != null) {
                    //非顶级部门
                    // 上级部门
                    FileDept pDept = fileDeptRepository.findById(dept.getPid()).orElseGet(FileDept::new);

                    if (pDept.getPid() != null) {
                        // 上级非一级部门
                        Long lastPid = pDept.getPid();
                        Long id = pDept.getId();
                        String deptName = pDept.getName();
                        // 部门循环结束->质量部门->结束
                        do {
                            step++;
                            FileApprovalProcess process2 = new FileApprovalProcess();
                            process2.copy(process);
                            FileDept fDept = fileDeptRepository.findById(lastPid).orElseGet(FileDept::new);
                            List<Approver> list = approverRepository.findByDeptIdAndIsMaster(id, true);
                            if (ValidationUtil.isNotEmpty(list)) {
                                process2.setApprovedBy(list.get(0).getId());
                                process2.setProcessNo(processNoFormat + "_" + sb + step);
                                processList.add(process2);
                            } else {
                                throw new BadRequestException("上级部门[" + deptName + "]未设置部门管理者，请联系管理员！");
                            }
                            lastPid = fDept.getPid();
                            id = fDept.getId();
                            deptName = fDept.getName();
                        } while (lastPid != null);
                        FileApprovalProcess process2 = new FileApprovalProcess();
                        process2.copy(process);
                        process2.setApprovedBy(commonUtils.getZlbMaster());
                        process2.setProcessNo(processNoFormat + "_" + sb + (step + 1));
                        processList.add(process2);
                        fileApprovalProcessRepository.saveAll(processList);
                    } else {
                        // 上级是一级部门，说明上级与质量部齐平，则直接提交质量部master审批 - 产生一条审批进度记录
                        if (!superior.equals(commonUtils.getZlbMaster())) {
                            // 如果直系上司就是质量部管理者则不需要再多走这一步审批了
                            step++;
                            FileApprovalProcess process3 = new FileApprovalProcess();
                            process3.copy(process);
                            process3.setApprovedBy(commonUtils.getZlbMaster());
                            process3.setProcessNo(processNoFormat + "_" + sb + step);
                            processList.add(process3);
                        }
                    }
                } else {
                    // 当前创建者所在部门是顶级部门，向本部门master发起任务审批（允许自批），向质量部门master发起审批任务请求
                    // 无上级，则自批，产生一条审批进度记录
                    step++;
                    FileApprovalProcess process4 = new FileApprovalProcess();
                    process4.copy(process);
                    process4.setApprovedBy(commonUtils.getZlbMaster());
                    process4.setProcessNo(processNoFormat + "_" + sb + step);
                    processList.add(process4);
                }
                fileApprovalProcessRepository.saveAll(processList);
            } else {
                // 循环到一级部门（仅在顶级部门之下）
                // 当前创建者是部门master,需要向上级部门或质量部master发起审批任务
                //当前所在部门
                FileDept dept = fileDeptRepository.findById(currentDeptId).orElseGet(FileDept::new);
                //获取上级部门
                if (dept.getPid() != null) {
                    //非顶级部门
                    // 上级部门
                    FileDept pDept = fileDeptRepository.findById(dept.getPid()).orElseGet(FileDept::new);

                    if (pDept.getPid() != null) {
                        // 上级非一级部门
                        String sb = "STEP";
                        int step = 0;
                        Long lastPid = pDept.getPid();
                        Long id = pDept.getId();
                        String deptName = pDept.getName();
                        // 部门循环结束->质量部门->结束
                        do {
                            step++;
                            FileApprovalProcess process1 = new FileApprovalProcess();
                            process1.copy(process);
                            FileDept fDept = fileDeptRepository.findById(lastPid).orElseGet(FileDept::new);
                            List<Approver> list = approverRepository.findByDeptIdAndIsMaster(id, true);
                            if (ValidationUtil.isNotEmpty(list)) {
                                process1.setApprovedBy(list.get(0).getId());
                                process1.setProcessNo(processNoFormat + "_" + sb + step);
                                processList.add(process1);
                            } else {
                                throw new BadRequestException("上级部门[" + deptName + "]未设置部门管理者，请联系管理员！");
                            }
                            lastPid = fDept.getPid();
                            id = fDept.getId();
                            deptName = fDept.getName();
                        } while (lastPid != null);
                        FileApprovalProcess process2 = new FileApprovalProcess();
                        process2.copy(process);
                        process2.setApprovedBy(commonUtils.getZlbMaster());
                        process2.setProcessNo(processNoFormat + "_" + sb + (step + 1));
                        processList.add(process2);
                        fileApprovalProcessRepository.saveAll(processList);
                    } else {
                        // 上级是一级部门，说明上级与质量部齐平，则直接提交质量部master审批 - 产生一条审批进度记录
                        process.setApprovedBy(commonUtils.getZlbMaster());
                        fileApprovalProcessRepository.save(process);
                    }
                } else {
                    // 当前创建者所在部门是顶级部门，向本部门master发起任务审批（允许自批），向质量部门master发起审批任务请求
                    // 无上级，则自批，产生一条审批进度记录
                    process.setApprovedBy(SecurityUtils.getCurrentUserId());
                    fileApprovalProcessRepository.save(process);
                }
            }

        } else {
            // 质量部本部审批
            String sb = "STEP";
            int step = 0;
            if (superior != null) {

                // 判断本部门此上级是否是部门master,不是的话还要继续循环找下去
                Approver approver = approverRepository.findById(superior).orElseGet(Approver::new);
                if (approver.getIsDepartMaster()) {
                    FileApprovalProcess process1 = new FileApprovalProcess();
                    process1.copy(process);
                    process1.setProcessNo(processNoFormat + "_" + sb + 1);
                    process1.setApprovedBy(superior);
                    processList.add(process1);
                } else {
                    Boolean isDepartMaster = approver.getIsDepartMaster();
                    Long userId = superior;
                    do {
                        step++;
                        Approver approver1 = approverRepository.findById(userId).orElseGet(Approver::new);
                        FileApprovalProcess process1 = new FileApprovalProcess();
                        process1.copy(process);
                        process1.setProcessNo(processNoFormat + "_" + sb + step);
                        process1.setApprovedBy(userId);
                        processList.add(process1);
                        userId = approver1.getSuperiorId();
                        isDepartMaster = approver1.getIsDepartMaster();
                    } while (!isDepartMaster);
                }
                fileApprovalProcessRepository.saveAll(processList);
            } else {
                // 质量部master自批
                FileApprovalProcess process1 = new FileApprovalProcess();
                process1.copy(process);
                process1.setProcessNo(processNoFormat + "_" + sb + 1);
                process1.setApprovedBy(SecurityUtils.getCurrentUserId());
                fileApprovalProcessRepository.save(process1);
            }
        }
    }


    public String getUserName() {
        try {
            return SecurityUtils.getCurrentUsername();
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(LocalStorage resources) {

        // 初始化待修改项
        LocalStorageTemp temp = new LocalStorageTemp();

        LocalStorage localStorage = localStorageRepository.findById(resources.getId()).orElseGet(LocalStorage::new);
        /**
         *  判断是否还处于中间待审批状态，如果是，则提示尚在流程中不允许二次修改；若审批结束了则不需要拦截
         * 拦截条件一：1.版本变更
         * 拦截条件二：2.修改操作
         * 额外放行条件：版本升级且未进入流程
         */
        String oldChangeType = "";
        String oldChangeDesc = "";
        String newRealName = "";
        String newTarPath = "";
        String newSize = "";
        String newType = "";
        String newSuffix = "";
        String newVersion = "";
        List<FileApprovalProcess> waitList = new ArrayList<>();
        FileApprovalProcess ps = new FileApprovalProcess();
        if (resources.getApprovalStatus().equals(CommonConstants.WAITING_FOR_STATUS)) {
            ps = fileApprovalProcessRepository.findByLastCreateTime(resources.getId(), CommonConstants.NOT_DEL, CommonConstants.FILE_TYPE_LIST);
            if (ps != null) {
                // 获取同组数据
                String processNo = ps.getProcessNo().substring(0, ps.getProcessNo().length() - 1) + "%";
                waitList = fileApprovalProcessRepository.findTeamProcessList(resources.getId(), CommonConstants.NOT_DEL, processNo);
            }
        } else if (localStorage.getLocalStorageTemp() != null) {
            ps = fileApprovalProcessRepository.findByLastCreateTime(localStorage.getLocalStorageTemp().getId(), CommonConstants.NOT_DEL, CommonConstants.FILE_TYPE_LIST);
            if (ps != null) {
                // 获取同组数据
                String processNo = ps.getProcessNo().substring(0, ps.getProcessNo().length() - 1) + "%";
                waitList = fileApprovalProcessRepository.findTeamProcessList(localStorage.getLocalStorageTemp().getId(), CommonConstants.NOT_DEL, processNo);
            }
        }

        if (ValidationUtil.isNotEmpty(waitList)) {
            List<String> results = new ArrayList<>();
            waitList.forEach(p -> {
                if (p.getApprovedResult() != null) {
                    results.add(p.getApprovedResult());
                }
            });
                /*if (results.size() != 0 && results.size() < processList.size()) {
                    throw new BadRequestException("当前修改的文件已进入审批流程，请流程结束后再试！");
                } else if (results.size() == 0) {
                   // todo 尚未审批暂时不处理
                }*/
            if (results.size() != 0 && results.size() < waitList.size()) {
                throw new BadRequestException("当前修改的文件已进入审批流程，请流程结束后再试！");
            } else if (results.size() == 0) {
                // 尚未审批
                // todo 删除任务+审批流程
                if (ps != null) {
                    newTarPath = ps.getTarPath();
                    newVersion = ps.getVersion();
                    newSize = ps.getSize();
                    newSuffix = ps.getSuffix();
                    newType = ps.getType();
                    newRealName = ps.getRealName();
                    switch (ps.getChangeType()) {
                        case CommonConstants.UPGRADE_VERSION:
                            oldChangeType = CommonConstants.UPGRADE_VERSION;
                            oldChangeDesc = ps.getChangeDesc() + "；";
                            // 若为升级版本则删除
                            preTrailRepository.delByStorageIdAndVersionAndChangeType(localStorage.getId(), ps.getVersion(), CommonConstants.UPGRADE_VERSION);
                            fileApprovalProcessRepository.deleteAllByBindingIdAndVersionAndChangeType(localStorage.getId(), ps.getVersion(), CommonConstants.UPGRADE_VERSION);
                            break;
                        case CommonConstants.NEW:
                            oldChangeDesc = ps.getChangeDesc() + "；";
                            preTrailRepository.delByStorageIdAndVersionAndChangeType(localStorage.getId(), localStorage.getVersion(), CommonConstants.NEW);
                            fileApprovalProcessRepository.deleteAllByBindingIdAndVersionAndChangeType(localStorage.getId(), localStorage.getVersion(), CommonConstants.NEW);
                            break;
                        case CommonConstants.REVISE:
                            preTrailRepository.delByStorageIdAndVersionAndChangeType(ps.getBindingId(), localStorage.getVersion(), CommonConstants.REVISE);
                            fileApprovalProcessRepository.deleteAllByBindingIdAndVersionAndChangeType(ps.getBindingId(), localStorage.getVersion(), CommonConstants.REVISE);
                            fileApprovalProcessRepository.deleteAllByBindingIdAndVersionAndChangeType(ps.getBindingId(), ps.getVersion(), CommonConstants.REVISE);
                            fileApprovalProcessRepository.deleteAllByBindingIdAndVersionAndChangeType(localStorage.getId(), ps.getVersion(), CommonConstants.REVISE);
                            fileApprovalProcessRepository.deleteAllByBindingIdAndVersionAndChangeType(localStorage.getId(), ps.getCreateTime(), localStorage.getVersion(), CommonConstants.REVISE);
                            // fileApprovalProcessRepository.deleteAllByBindingIdAndVersionAndChangeType(localStorage.getId(), localStorage.getVersion(), CommonConstants.REVISE);
                            break;
                    }
                }
            } else {
                // 已经审批完了
            }
        }

        //清理缓存
        delCaches(resources.getId());

        // 重名校验
        FileCategory fileCategory = resources.getFileCategory();
        FileDept fileDept = resources.getFileDept();

        LocalStorage old = localStorageRepository.findByNameAndDeptIdAndCategoryId(resources.getName(), fileDept.getId(), fileCategory.getId());
        if (old != null && !old.getId().equals(resources.getId())) {
            throw new EntityExistException(LocalStorage.class, "name", resources.getName());
        }

        ValidationUtil.isNull(localStorage.getId(), "LocalStorage", "id", resources.getId());

        // 修改temp数据

        if (localStorage.getLocalStorageTemp() != null) {
            // temp.setId(localStorage.getLocalStorageTemp().getId());
            localStorageTempRepository.deleteByStorageId(localStorage.getId());
            preTrailRepository.deleteByStorageId(localStorage.getLocalStorageTemp().getId());
        }
        // 依据localStorage重构temp属性
        temp.setStorageId(localStorage.getId());
        temp.setRealName(localStorage.getRealName());
        temp.setVersion(localStorage.getVersion());
        temp.setName(resources.getName());
        temp.setPath(localStorage.getPath());
        temp.setSuffix(localStorage.getSuffix());
        temp.setType(localStorage.getType());
        temp.setSize(localStorage.getSize());
        // 重置文件自身属性
        if (oldChangeType.equals(CommonConstants.UPGRADE_VERSION)) {
            temp.setRealName(newRealName);
            temp.setVersion(newVersion);
            temp.setPath(newTarPath);
            temp.setSuffix(newSuffix);
            temp.setType(newType);
            temp.setSize(newSize);
        }
        temp.setIsRevision(localStorage.getIsRevision());
        temp.setFileStatus(resources.getFileStatus());
        temp.setApprovalStatus(CommonConstants.WAITING_FOR_STATUS);
        temp.setFileType(resources.getFileType());
        temp.setSecurityLevel(resources.getSecurityLevel());
        temp.setExpirationTime(resources.getExpirationTime());
        temp.setChangeDesc(oldChangeDesc + resources.getChangeDesc());
        temp.setFileLevel(resources.getFileLevel());
        temp.setFileCategory(resources.getFileCategory());
        temp.setFileDept(resources.getFileDept());
        temp.setFileDesc(resources.getFileDesc());
        // temp.setBindDepts(localStorage.getBindDepts());
        // temp.setBindFiles(localStorage.getBindFiles());
        LocalStorageTemp newTemp = localStorageTempRepository.save(temp);

        //绑定文件--需要先清空之前先装载最新数据到临时文件信息中
        /*//删除旧数据，只保存现有数据
        if (temp.getId() != null) {
            bindingLocalStorageTempRepository.deleteByStorageTempId(temp.getId());
            bindingDeptTempRepository.deleteByStorageTempId(temp.getId());
        }*/
        Set<BindingLocalStorageTemp> bindTempFiles = new HashSet<>();
        List<BindingLocalStorage> bindFiles = resources.getBindFiles();
        if (ValidationUtil.isNotEmpty(bindFiles)) {
            bindFiles.forEach(bind -> {
                BindingLocalStorageTemp bindStorageTemp = new BindingLocalStorageTemp();
                bindStorageTemp.setStorageTempId(newTemp.getId());
                bindStorageTemp.setBindingStorageId(bind.getBindingStorageId());
                bindTempFiles.add(bindStorageTemp);
            });
            bindingLocalStorageTempRepository.saveAll(bindTempFiles);
            // newTemp.getBindTempFiles().clear();
            newTemp.getBindTempFiles().addAll(bindTempFiles);
        }

        //绑定开放部门--需要先清空之前先装载最新数据
        Set<BindingDeptTemp> bindDeptTemps = new HashSet<>();
        Set<BindingDept> bindDepts = resources.getBindDepts();
        if (resources.getSecurityLevel().equals(CommonConstants.SECURITY_EXTERNAL) && ValidationUtil.isNotEmpty(Collections.singletonList(bindDepts))) {
            bindDepts.forEach(bind -> {
                BindingDeptTemp bindDeptTemp = new BindingDeptTemp();
                bindDeptTemp.setStorageTempId(newTemp.getId());
                bindDeptTemp.setDeptId(bind.getDeptId());
                bindDeptTemps.add(bindDeptTemp);
            });
            bindingDeptTempRepository.saveAll(bindDeptTemps);
            // newTemp.getBindTempDepts().clear();
            newTemp.getBindTempDepts().addAll(bindDeptTemps);
        }
        // temp.setIsDel(CommonConstants.IS_DEL);
        localStorageTempRepository.save(newTemp);

        // 源文件信息与temp建立关联
        localStorage.setLocalStorageTemp(newTemp);
        localStorage.setChangeDesc(resources.getChangeDesc());
        localStorageRepository.save(localStorage);

        // todo 根据temp去创建审批流程---采用异步流程
        // 创建编号：任务编号和待审批进度信息编号
        String processNoFormat = createNoFormat();
        String processTempNoFormat = createTempNoFormat();

        //创建待审批任务
        PreTrail preTrail = new PreTrail();
        preTrail.setPreTrailNo(processNoFormat);
        preTrail.setStorageId(newTemp.getId());
        preTrail.setTarPath(newTemp.getPath());
        preTrail.setSrcPath(localStorage.getPath());
        preTrail.setSuffix(newTemp.getSuffix());
        preTrail.setSize(newTemp.getSize());
        preTrail.setFileType(newTemp.getType());
        preTrail.setType(CommonConstants.TRAIL_TYPE_FILE);
        preTrail.setRealName(newTemp.getRealName());
        preTrail.setChangeType(CommonConstants.REVISE);
        preTrail.setBindingType(true);
        preTrail.setBindingId(localStorage.getId());
        preTrail.setVersion(newTemp.getVersion());
        preTrail.setChangeDesc(oldChangeDesc + "修改文件：[" + localStorage.getName() + "],修改内容：" + resources.getChangeDesc());
        preTrail.setIsDel(0L);
        // 根据当前登录人查找其上级领导
        // todo 允许选择临时审批者
        Long superior = SecurityUtils.getCurrentUserSuperior();
        Long currentDeptId = SecurityUtils.getCurrentDeptId();
        if (superior != null) {
            preTrail.setApprovedBy(superior);
        } else {
            // 所在部门就是质量部
            if (currentDeptId.equals(CommonConstants.ZL_DEPART)) {
                //质量部的master自批就可以了
                preTrail.setApprovedBy(SecurityUtils.getCurrentUserId());
            } else {
                // 循环到一级部门（仅在顶级部门之下）
                // 当前创建者是部门master,需要向上级部门或质量部master发起审批任务
                //当前所在部门
                FileDept dept = fileDeptRepository.findById(currentDeptId).orElseGet(FileDept::new);

                //获取上级部门
                if (dept.getPid() != null) {
                    //非顶级部门
                    // 上级部门
                    FileDept pDept = fileDeptRepository.findById(dept.getPid()).orElseGet(FileDept::new);

                    if (pDept.getPid() != null) {
                        // 非一级部门
                        List<Approver> list = approverRepository.findByDeptIdAndIsMaster(pDept.getId(), true);
                        if (ValidationUtil.isNotEmpty(list)) {
                            preTrail.setApprovedBy(list.get(0).getId());
                        } else {
                            throw new BadRequestException("上级部门[" + pDept.getName() + "]未设置部门管理者，请联系管理员！");
                        }
                    } else {
                        // 一级部门则直接提交质量部master审批
                        preTrail.setApprovedBy(commonUtils.getZlbMaster());
                    }
                } else {
                    // 当前创建者所在部门是顶级部门，向本部门msater发起任务审批（允许自批），向质量部门master发起审批任务请求
                    // 无上级，则自批，例如：总经理等人
                    preTrail.setApprovedBy(SecurityUtils.getCurrentUserId());
                }
            }
        }
        preTrailRepository.save(preTrail);
        // create审批进度表
        // 初始化整个审批流程进度 例：研发->研发经理->研发总监->质量部经理(所有审批最终审批者：质量部经理)
        FileApprovalProcess process = new FileApprovalProcess();
        // 设置审批流程进度编号
        process.setProcessNo(processTempNoFormat);
        process.setBindingId(newTemp.getId());
        process.setSrcPath(localStorage.getPath());
        process.setSuffix(newTemp.getSuffix());
        process.setVersion(newTemp.getVersion());
        process.setSize(newTemp.getSize());
        process.setType(newTemp.getType());
        process.setRealName(newTemp.getRealName());
        process.setChangeType(CommonConstants.REVISE);
        process.setTarPath(newTemp.getPath());
        process.setBindingType(true);
        process.setChangeDesc(oldChangeDesc + "修改文件：[" + localStorage.getName() + "],修改内容：" + resources.getChangeDesc());
        process.setIsDel(0L);

        List<FileApprovalProcess> processList = new ArrayList<>();
        // 循环到上级的supperiorId = NULL 部门循环结束->质量部门->结束
        createProcessList(processTempNoFormat, superior, currentDeptId, process, processList);

        // todo 正式文件上应当也存在审批流程记录
        FileApprovalProcess formalProcess = new FileApprovalProcess();
        // 设置审批流程进度编号
        formalProcess.setProcessNo(processNoFormat);
        formalProcess.setSrcPath(localStorage.getPath());
        formalProcess.setTarPath(newTemp.getPath());
        formalProcess.setSuffix(newTemp.getSuffix());
        formalProcess.setVersion(newTemp.getVersion());
        formalProcess.setSize(newTemp.getSize());
        formalProcess.setType(newTemp.getType());
        formalProcess.setRealName(newTemp.getRealName());
        formalProcess.setChangeType(CommonConstants.REVISE);
        formalProcess.setBindingId(localStorage.getId());
        formalProcess.setChangeDesc(process.getChangeDesc());
        formalProcess.setBindingType(null);
        List<FileApprovalProcess> formalProcessList = new ArrayList<>();
        // 循环到上级的supperiorId = NULL 部门循环结束->质量部门->结束
        createProcessList(processNoFormat, superior, currentDeptId, formalProcess, formalProcessList);

        // localStorageRepository.save(localStorage);

        // 变更日志记录
        ToolsLog log = new ToolsLog();
        log.setLogType(CommonConstants.LOG_TYPE_FILE);
        log.setUsername(getUserName());
        log.setBindingId(localStorage.getId());
        log.setDescription("修改文件");
        log.setDescriptionDetail(resources.getChangeDesc());
        toolsLogRepository.save(log);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAll(Long[] ids) {
        for (Long id : ids) {
            // 校验是否有文件被引用
            List<IssueFile> list = issueFileRepository.findByStorageId(id);
            if (ValidationUtil.isNotEmpty(list)) {
                throw new BadRequestException("所选文件作为附件被引用，请解除后再试！");
            }
            //清理缓存
            delCaches(id);
            LocalStorage storage = localStorageRepository.findById(id).orElseGet(LocalStorage::new);
            FileUtil.del(storage.getPath());
            // 删除文件临时信息
            if (storage.getLocalStorageTemp() != null) {
                // todo 删除其他临时信息绑定项
                // 删除文件对应的审批任务
                preTrailRepository.deleteAllByStorageId(storage.getLocalStorageTemp().getId());
                // 删除文件对应的审批进度
                fileApprovalProcessRepository.deleteAllByBindingId(storage.getLocalStorageTemp().getId());
                localStorageTempRepository.deleteByStorageId(id);
            }

            //删除该文件已绑定的数据
            bindingLocalStorageRepository.deleteByStorageId(id);
            //解绑数据
            bindingLocalStorageRepository.updateAllByStorageId(id);
            //删除文件信息
            //localStorageRepository.delete(storage);
            localStorageRepository.delById(id);
            // 删除文件对应的审批任务
            preTrailRepository.deleteAllByStorageId(id);
            // 删除文件对应的审批进度
            fileApprovalProcessRepository.deleteAllByBindingId(id);
            //删除该文件对应的工具日志记录
            // 变更日志记录
            ToolsLog log = new ToolsLog();
            log.setLogType(CommonConstants.LOG_TYPE_FILE);
            log.setUsername(getUserName());
            log.setBindingId(id);
            log.setDescription("删除文件");
            log.setDescriptionDetail("删除【" + storage.getName() + "】文件");
            toolsLogRepository.save(log);
            //toolsLogRepository.deleteAllByBindingId(id);
        }
    }

    @Override
    public void download(List<LocalStorageDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (LocalStorageDto localStorageDTO : queryAll) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("文件名", localStorageDTO.getRealName());
            map.put("备注名", localStorageDTO.getName());
            map.put("文件类型", localStorageDTO.getType());
            map.put("文件大小", localStorageDTO.getSize());
            if (localStorageDTO.getFileLevel() != null) {
                map.put("文件等级", localStorageDTO.getFileLevel().getName());
            }
            if (localStorageDTO.getFileCategory() != null) {
                map.put("文件分类", localStorageDTO.getFileCategory().getName());
            }
            if (localStorageDTO.getFileDept() != null) {
                map.put("所属部门", localStorageDTO.getFileDept().getName());
            }
            map.put("文件版本", localStorageDTO.getVersion());
            map.put("审批状态", localStorageDTO.getApprovalStatus());
            map.put("文件状态", localStorageDTO.getFileStatus());
            map.put("创建者", localStorageDTO.getCreateBy());
            map.put("创建日期", localStorageDTO.getCreateTime());
            map.put("版本号", localStorageDTO.getVersion());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PreTrail uploadPreTrailV2(Long id, String version, String approvalStatus, MultipartFile multipartFile) {

        LocalStorage old = localStorageRepository.findById(id).orElseGet(LocalStorage::new);

        // 前置判断是否允许上传
        List<FileApprovalProcess> waitList = new ArrayList<>();
        if (approvalStatus.equals(CommonConstants.WAITING_FOR_STATUS)) {
            FileApprovalProcess ps = fileApprovalProcessRepository.findByLastCreateTime(id, CommonConstants.NOT_DEL, CommonConstants.FILE_TYPE_LIST);
            if (ps != null) {
                // 获取同组数据
                String processNo = ps.getProcessNo().substring(0, ps.getProcessNo().length() - 1) + "%";
                waitList = fileApprovalProcessRepository.findTeamProcessList(id, CommonConstants.NOT_DEL, processNo);
            }
        } else if (old.getLocalStorageTemp() != null) {
            FileApprovalProcess ps = fileApprovalProcessRepository.findByLastCreateTime(old.getLocalStorageTemp().getId(), CommonConstants.NOT_DEL, CommonConstants.FILE_TYPE_LIST);
            if (ps != null) {
                // 获取同组数据
                String processNo = ps.getProcessNo().substring(0, ps.getProcessNo().length() - 1) + "%";
                waitList = fileApprovalProcessRepository.findTeamProcessList(old.getLocalStorageTemp().getId(), CommonConstants.NOT_DEL, processNo);
            }
        }
        /*switch (ps.getChangeType()) {
            case CommonConstants.UPGRADE_VERSION:
                // 若为升级版本则删除
                preTrailRepository.delByStorageIdAndVersionAndChangeType(old.getId(), ps.getVersion(), CommonConstants.UPGRADE_VERSION);
                fileApprovalProcessRepository.deleteAllByBindingIdAndVersionAndChangeType(old.getId(), ps.getVersion(), CommonConstants.UPGRADE_VERSION);
                break;
            case CommonConstants.NEW:
                preTrailRepository.delByStorageIdAndVersionAndChangeType(old.getId(), old.getVersion(), CommonConstants.NEW);
                fileApprovalProcessRepository.deleteAllByBindingIdAndVersionAndChangeType(old.getId(), old.getVersion(), CommonConstants.NEW);
                break;
            case CommonConstants.REVISE:
                preTrailRepository.delByStorageIdAndVersionAndChangeType(ps.getBindingId(), old.getVersion(), CommonConstants.REVISE);
                fileApprovalProcessRepository.deleteAllByBindingIdAndVersionAndChangeType(ps.getBindingId(), old.getVersion(), CommonConstants.REVISE);
                fileApprovalProcessRepository.deleteAllByBindingIdAndVersionAndChangeType(old.getId(), ps.getCreateTime(), old.getVersion(), CommonConstants.REVISE);
                // fileApprovalProcessRepository.deleteAllByBindingIdAndVersionAndChangeType(localStorage.getId(), localStorage.getVersion(), CommonConstants.REVISE);
                break;
            default:
                break;
        }*/

        if (ValidationUtil.isNotEmpty(waitList)) {
            List<String> results = new ArrayList<>();
            waitList.forEach(p -> {
                if (p.getApprovedResult() != null) {
                    results.add(p.getApprovedResult());
                }
            });
                /*if (results.size() != 0 && results.size() < processList.size()) {
                    throw new BadRequestException("当前修改的文件已进入审批流程，请流程结束后再试！");
                } else if (results.size() == 0) {
                   // todo 尚未审批暂时不处理
                }*/
            if (results.size() < waitList.size()) {
                throw new BadRequestException("当前修改的文件正被审批，请流程结束后再改版！");
            }
        }

        FileUtil.checkSize(properties.getMaxSize(), multipartFile.getSize());
        String suffix = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
        String type = FileUtil.getFileType(suffix);
        File file = FileUtil.upload(multipartFile, properties.getPath().getPath() + type + File.separator);
        if (ObjectUtil.isNull(file)) {
            throw new BadRequestException("Upload Error! 上传失败");
        }
        try {
            LocalStorage localStorage = localStorageRepository.findById(id).orElseGet(LocalStorage::new);
            ValidationUtil.isNull(localStorage.getId(), "LocalStorage", "id", id);
            // 创建编号
            String processNoFormat = createNoFormat();

            //初始版本,初始文件自然不变版本
            // todo 支持自定义版本号
            /*String version = getNextVersion(localStorage);
            if (approvalStatus.equals(CommonConstants.OBSOLETED_STATUS)) {
                // 获取之前的任务版本号重新发布
                List<PreTrail> list = preTrailRepository.findAllByStorageId(id, CommonConstants.NOT_DEL);
                if (ValidationUtil.isNotEmpty(list)) {
                    version = list.get(0).getVersion();
                }
            }*/
            //创建待审批任务
            PreTrail preTrail = new PreTrail();
            preTrail.setPreTrailNo(processNoFormat);
            preTrail.setStorageId(localStorage.getId());
            preTrail.setSrcPath(localStorage.getPath());
            preTrail.setTarPath(file.getPath());
            preTrail.setSuffix(suffix);
            preTrail.setVersion(version);
            preTrail.setSize(FileUtil.getSize(multipartFile.getSize()));
            preTrail.setFileType(type);
            preTrail.setType(CommonConstants.TRAIL_TYPE_FILE);
            preTrail.setRealName(file.getName());
            preTrail.setChangeType(CommonConstants.UPGRADE_VERSION);
            preTrail.setChangeDesc("新文件[" + FileUtil.getFileNameNoEx(multipartFile.getOriginalFilename()) + "]替代原文件[" + localStorage.getRealName() + "],文件版本为" + version);
            preTrail.setIsDel(0L);
            // 根据当前登录人查找其上级领导
            // todo 允许选择临时审批者
            Long superior = SecurityUtils.getCurrentUserSuperior();
            Long currentDeptId = SecurityUtils.getCurrentDeptId();
            if (superior != null) {
                preTrail.setApprovedBy(superior);
            } else {
                // todo 循环到一级部门（仅在顶级部门之下）
                // 当前创建者是部门master,需要向上级部门或质量部master发起审批任务
                //当前所在部门
                FileDept dept = fileDeptRepository.findById(currentDeptId).orElseGet(FileDept::new);
                //获取上级部门
                if (dept.getPid() != null) {
                    //非顶级部门
                    // 上级部门
                    FileDept pDept = fileDeptRepository.findById(dept.getPid()).orElseGet(FileDept::new);

                    if (pDept.getPid() != null) {
                        // 非一级部门
                        List<Approver> list = approverRepository.findByDeptIdAndIsMaster(pDept.getId(), true);
                        if (ValidationUtil.isNotEmpty(list)) {
                            preTrail.setApprovedBy(list.get(0).getId());
                        } else {
                            throw new BadRequestException("上级部门[" + pDept.getName() + "]未设置部门管理者，请联系管理员！");
                        }
                    } else {
                        // 一级部门则直接提交质量部master审批
                        preTrail.setApprovedBy(commonUtils.getZlbMaster());
                    }
                } else {
                    // 当前创建者所在部门是顶级部门，向本部门msater发起任务审批（允许自批），向质量部门master发起审批任务请求
                    //无上级，则自批
                    preTrail.setApprovedBy(SecurityUtils.getCurrentUserId());
                }
            }

            // create审批进度表
            // todo 初始化整个审批流程进度 例：研发->研发经理->研发总监->质量部经理(所有审批最终审批者：质量部经理)
            FileApprovalProcess process = new FileApprovalProcess();
            process.setProcessNo(processNoFormat);
            process.setBindingId(localStorage.getId());
            process.setSrcPath(localStorage.getPath());
            process.setTarPath(file.getPath());
            process.setSuffix(suffix);
            process.setVersion(version);
            process.setSize(FileUtil.getSize(multipartFile.getSize()));
            process.setType(type);
            process.setRealName(file.getName());
            process.setChangeType(CommonConstants.UPGRADE_VERSION);
            process.setChangeDesc("新文件[" + FileUtil.getFileNameNoEx(multipartFile.getOriginalFilename()) + "]替代原文件[" + localStorage.getRealName() + "],文件版本为" + version);
            process.setIsDel(0L);

            List<FileApprovalProcess> processList = new ArrayList<>();
            //todo 循环到上级的supperiorId = NULL 部门循环结束->质量部门->结束
            createProcessList(processNoFormat, superior, currentDeptId, process, processList);

            return preTrailRepository.save(preTrail);

        } catch (Exception e) {
            FileUtil.del(file);
            throw e;
        }
    }

    /**
     * 清理缓存
     *
     * @param id /
     */
    public void delCaches(Long id) {
        // 删除文件 等同于：@CacheEvict(key = "'id:' + #p0.id")
        redisUtils.del(CacheKey.FILE_ID + id);
    }
}
