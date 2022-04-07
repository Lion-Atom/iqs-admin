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
package me.zhengjie.service;

import me.zhengjie.domain.*;
import me.zhengjie.service.dto.*;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Zheng Jie
 * @date 2019-09-05
 */
public interface LocalStorageService {

    /**
     * 分页查询
     *
     * @param criteria 条件
     * @param pageable 分页参数
     * @return /
     */
    Object queryAll(LocalStorageQueryCriteria criteria, Pageable pageable);

    /**
     * 查询全部数据
     *
     * @param criteria 条件
     * @return /
     */
    List<LocalStorageDto> queryAll(LocalStorageQueryCriteria criteria);

    /**
     * 根据ID查询
     *
     * @param id /
     * @return /
     */
    LocalStorageDto findById(Long id);

    /**
     * 上传
     *
     * @param name 文件名称
     * @param file 文件
     * @return 文件信息
     */
    LocalStorage create(String name, String version, Long fileLevelId, Long fileCategoryId, Long deptId, String fileStatus,
                        String fileType, String securityLevel, Timestamp expirationTime, String desc,
                        MultipartFile file, List<Long> bindFiles, Set<Long> bindDepts, Boolean isTempPicture);

    /**
     * 编辑
     *
     * @param resources 文件信息
     */
    void update(LocalStorage resources);

    /**
     * 多选删除
     *
     * @param ids /
     */
    void deleteAll(Long[] ids);

    /**
     * 导出数据
     *
     * @param localStorageDtos 待导出的数据
     * @param response         /
     * @throws IOException /
     */
    void download(List<LocalStorageDto> localStorageDtos, HttpServletResponse response) throws IOException;

    /**
     * 根据文件标识查询文件信息
     *
     * @param ids 文件标识集合
     * @return 文件信息
     */
    List<LocalStorageSmall> queryAllByIds(Long[] ids);

    /**
     * 根据文件临时标识查询文件临时信息
     *
     * @param ids 文件临时标识集合
     * @return 文件信息
     */
    List<LocalStorageTempSmall> queryByTempIds(Long[] ids);

    /**
     * 提交文件内容变更
     *
     * @param id             文件标识
     * @param approvalStatus 文件状态
     * @param file           文件内容
     */
    PreTrail uploadPreTrail(Long id, String approvalStatus, MultipartFile file);

    /**
     * 提交文件内容变更
     *
     * @param id             文件标识
     * @param version        文件版本
     * @param approvalStatus 文件状态
     * @param file           文件内容
     */
    PreTrail uploadPreTrailV2(Long id, String version, String approvalStatus, MultipartFile file);

    /**
     * 文件内容变更
     *
     * @param id   文件标识
     * @param file 文件内容
     * @return 文件更新后内容
     */
    LocalStorage cover(Long id, MultipartFile file);

    /**
     * 撤销变更
     *
     * @param id 文件标识
     */
    void undo(Long id);

    /**
     * @param dto 上次修改时间+编辑前的审批状态
     */
    void rollBackCover(RollbackDto dto);

    /**
     * @param fileId 文件标识
     * @return 文件待审批项目
     */
    Map<String, Object> getPreTrailByFileId(Long fileId, Boolean latestVersion);

    /**
     * @param fileId 文件标识
     * @return 文件待审批项目
     */
    Map<String, Object> getPreTrailByFileTempId(Long fileId, Boolean latestVersion);

    /**
     * @param fileId 文件标识
     * @return 文件待审批项目
     */
    Map<String, Object> getApprovalProcessByFileId(Long fileId);


    /**
     * 条件查询文件信息
     *
     * @param queryDto 查询条件
     * @return 文件信息
     */
    List<LocalStorageSmall> findByExample(FileQueryDto queryDto);

    /**
     * @param fileId 文件标识
     * @param isTemp 是否是临时标识
     * @return 文件待审批项目
     */
    Map<String, Object> getApprovalProcessByFileIdV2(Long fileId, Boolean isTemp);

    /**
     * @param fileId 文件标识
     * @return 文件审批项目集合
     */
    Map<String, Object> getApprovalProcessListByFileId(Long fileId);
}