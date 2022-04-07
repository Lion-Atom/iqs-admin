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

import me.zhengjie.domain.FileLevel;
import me.zhengjie.service.dto.FileLevelDto;
import me.zhengjie.service.dto.FileLevelQueryCriteria;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
* @author Zheng Jie
* @date 2019-03-25
*/
public interface FileLevelService {

    /**
     * 查询所有数据
     * @param criteria 条件
     * @param isQuery /
     * @throws Exception /
     * @return /
     */
    List<FileLevelDto> queryAll(FileLevelQueryCriteria criteria, Boolean isQuery) throws Exception;

    /**
     * 根据ID查询
     * @param id /
     * @return /
     */
    FileLevelDto findById(Long id);

    /**
     * 创建
     * @param resources /
     */
    void create(FileLevel resources);

    /**
     * 编辑
     * @param resources /
     */
    void update(FileLevel resources);

    /**
     * 删除
     * @param FileLevelDtos /
     *
     */
    void delete(Set<FileLevelDto> FileLevelDtos);

    /**
     * 根据PID查询
     * @param pid /
     * @return /
     */
    List<FileLevel> findByPid(long pid);

    /**
     * 根据文件ID查询
     * @param id /
     * @return /
     */
//    Set<FileLevel> findByFileId(Long id);

    /**
     * 导出数据
     * @param queryAll 待导出的数据
     * @param response /
     * @throws IOException /
     */
    void download(List<FileLevelDto> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 获取待删除的文件等级
     * @param FileLevelList /
     * @param FileLevelDtos /
     * @return /
     */
    Set<FileLevelDto> getDeleteFileLevels(List<FileLevel> FileLevelList, Set<FileLevelDto> FileLevelDtos);

    /**
     * 根据ID获取同级与上级数据
     * @param FileLevelDto /
     * @param FileLevels /
     * @return /
     */
    List<FileLevelDto> getSuperior(FileLevelDto FileLevelDto, List<FileLevel> FileLevels);

    /**
     * 构建树形数据
     * @param FileLevelDtos /
     * @return /
     */
    Object buildTree(List<FileLevelDto> FileLevelDtos);

    /**
     * 获取
     * @param FileLevelList
     * @return
     */
    List<Long> getFileLevelChildren(List<FileLevel> FileLevelList);

    /**
     * 验证是否被文件关联
     * @param FileLevelDtos /
     */
    void verification(Set<FileLevelDto> FileLevelDtos);
}