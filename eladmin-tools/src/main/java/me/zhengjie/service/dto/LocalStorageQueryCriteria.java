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
package me.zhengjie.service.dto;

import lombok.Data;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.zhengjie.annotation.DataPermission;
import me.zhengjie.annotation.Query;

/**
* @author Zheng Jie
* @date 2019-09-05
*/
@Data
//@DataPermission(joinName = "fileDept.id")
public class LocalStorageQueryCriteria{

    @Query(blurry = "name,suffix,type,createBy,size")
    private String blurry;

    @Query(propName = "id", type = Query.Type.EQUAL)
    // 用于查询自身，查询可绑定项
    private Long fileId;

    @Query(propName = "id", type = Query.Type.NOT_EQUAL)
    // 用于排除自身，查询可绑定项
    private Long id;

    @Query(propName = "id", type = Query.Type.EQUAL, joinName = "fileLevel")
    private Long fileLevelId;

    private Long deptId;

    @Query(propName = "id", type = Query.Type.IN, joinName = "fileDept")
    private Set<Long> deptIds = new HashSet<>();

    @Query(propName = "id", type = Query.Type.IN, joinName = "fileCategory")
    private Set<Long> fileCategoryIds = new HashSet<>();

    private Long fileCategoryId;

    @Query
    private String fileStatus;

    @Query(propName = "fileStatus", type = Query.Type.NOT_EQUAL)
    private String fileStatusExternal;

    @Query
    private String approvalStatus;

    @Query
    private String fileType;

    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;

    @Query
    private Long isDel = 0L;

    private Boolean anonymousAccess = false;

    private Boolean isReference = false;
}