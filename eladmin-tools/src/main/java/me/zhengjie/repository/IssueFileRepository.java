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
package me.zhengjie.repository;

import me.zhengjie.domain.IssueFile;
import me.zhengjie.service.dto.IssueFileQueryDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;


/**
 * @author Tong Minjie
 * @date 2020-07-28
 */
@Repository
public interface IssueFileRepository extends JpaRepository<IssueFile, Long>, JpaSpecificationExecutor<IssueFile> {


    /**
     * 根据问题id删除附件信息
     *
     * @param issueId 问题标识
     */
    @Modifying
    @Query(value = " delete  from issue_file where issue_id = ?1 ", nativeQuery = true)
    void deleteByIssueId(Long issueId);

    /**
     * 根据问题id删除指定附件信息
     *
     * @param issueId   问题标识
     * @param stepNames 步骤集合
     */
    @Modifying
    @Query(value = " delete  from issue_file where issue_id = ?1 and step_name in ?2", nativeQuery = true)
    void deleteByIssueIdAndInStepNames(Long issueId, List<String> stepNames);

    /**
     * 根据问题id删除指定步骤的附件信息
     *
     * @param issueId  问题标识
     * @param stepName 所属步骤
     */
    @Modifying
    @Query(value = " delete  from issue_file where issue_id = ?1 " +
            " and step_name = ?2 " +
            " and storage_id is not null", nativeQuery = true)
    void deleteTempByIssueIdAndStepName(Long issueId, String stepName);

    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * 反查问题标识
     *
     * @param id id
     * @return 问题标识
     */
    @Query(value = " select issue_id  from issue_file where file_id = ?1 ", nativeQuery = true)
    Long findIssueIdById(Long id);

    /**
     * 查询文控绑定的指定步骤下的文件
     *
     * @param issueId  问题标识
     * @param stepName 步骤名称
     * @return 文件信息
     */
    @Query(value = " select *  from issue_file where issue_id = ?1 " +
            " and step_name = ?2 " +
            " and storage_id is not null", nativeQuery = true)
    List<IssueFile> findTempFileByStepNameAndIssueId(Long issueId, String stepName);

    /**
     * 查询非文控绑定的指定步骤下的文件
     *
     * @param issueId  问题标识
     * @param stepName 步骤名称
     * @return 文件信息
     */
    @Query(value = " select *  from issue_file where issue_id = ?1 " +
            " and step_name = ?2 " +
            " and storage_id is null", nativeQuery = true)
    List<IssueFile> findComFileByStepNameAndIssueId(Long issueId, String stepName);

    /**
     * 查询绑定文控文件的数据
     *
     * @param storageId 文件标识
     * @return 文件信息
     */
    @Query(value = " select *  from issue_file where storage_id = ?1 ", nativeQuery = true)
    List<IssueFile> findByStorageId(Long storageId);
}
