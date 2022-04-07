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

import me.zhengjie.domain.FileCategory;
import me.zhengjie.service.dto.FileCategoryDto;
import me.zhengjie.service.dto.FileCategoryQueryCriteria;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
* @author Tong Minjie
* @date 2021-04-28
*/
public interface FileCategoryService {

    /**
     * 查询所有数据
     * @param criteria 条件
     * @param isQuery /
     * @throws Exception /
     * @return /
     */
    List<FileCategoryDto> queryAll(FileCategoryQueryCriteria criteria, Boolean isQuery) throws Exception;

    /**
     * 根据ID查询
     * @param id /
     * @return /
     */
    FileCategoryDto findById(Long id);

    /**
     * 创建
     * @param resources /
     */
    void create(FileCategory resources);

    /**
     * 编辑
     * @param resources /
     */
    void update(FileCategory resources);

    /**
     * 删除
     * @param fileCategoryDtos /
     *
     */
    void delete(Set<FileCategoryDto> fileCategoryDtos);

    /**
     * 根据PID查询
     * @param pid /
     * @return /
     */
    List<FileCategory> findByPid(long pid);

    /**
     * 根据文件ID查询
     * @param id /
     * @return /
     */
//    Set<FileCategory> findByFileId(Long id);

    /**
     * 导出数据
     * @param queryAll 待导出的数据
     * @param response /
     * @throws IOException /
     */
    void download(List<FileCategoryDto> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 获取待删除的文件等级
     * @param fileCategoryList /
     * @param fileCategoryDtos /
     * @return /
     */
    Set<FileCategoryDto> getDeleteFileCategorys(List<FileCategory> fileCategoryList, Set<FileCategoryDto> fileCategoryDtos);

    /**
     * 根据ID获取同级与上级数据
     * @param fileCategoryDto /
     * @param fileCategorys /
     * @return /
     */
    List<FileCategoryDto> getSuperior(FileCategoryDto fileCategoryDto, List<FileCategory> fileCategorys);

    /**
     * 构建树形数据
     * @param fileCategoryDtos /
     * @return /
     */
    Object buildTree(List<FileCategoryDto> fileCategoryDtos);

    /**
     * 获取
     * @param fileCategoryList 文件分类集合
     * @return 所有分类及其子孙集合
     */
    List<Long> getFileCategoryChildren(List<FileCategory> fileCategoryList);

    /**
     * 验证是否被文件关联
     * @param fileCategoryDtos /
     */
    void verification(Set<FileCategoryDto> fileCategoryDtos);
}