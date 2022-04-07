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
package me.zhengjie.modules.system.service.dto;

import lombok.Data;
import me.zhengjie.annotation.Query;

import java.sql.Timestamp;
import java.util.List;

/**
 * 审批进度查询类
 * @author Tong Minjie
 * @date 2019-06-24 09:23:07
 */
@Data
public class ApprovalProcessQueryCriteria {

    @Query(blurry = "realName,createBy,changeDesc,processNo,updateBy")
    private String blurry;

    @Query(propName = "bindingId", type = Query.Type.EQUAL)
    private Long bindingId;

    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;

    @Query
    private Integer isDel=0;

    @Query
    private String version;

    @Query
    private Long approvedBy;

}
