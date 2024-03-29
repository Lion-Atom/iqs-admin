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
import me.zhengjie.annotation.DataPermission;
import me.zhengjie.annotation.Query;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
* @author Zheng Jie
* @date 2019-03-25
*/
@Data
public class DeptQueryCriteriaV2 {

    @Query(type = Query.Type.INNER_LIKE)
    private String name;

    @Query
    private Boolean enabled;

    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;

    @Query(propName = "id", type = Query.Type.IN)
    private Set<Long> deptIds = new HashSet<>();

}