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
import lombok.NoArgsConstructor;
import me.zhengjie.annotation.Query;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
* @author Tong Minjie
* @date 2022/05/07
*/
@Data
@NoArgsConstructor
public class TrainCertificationQueryCriteria {

    @Query(blurry = "staffName")
    private String blurry;

    private Long departId;

    @Query(propName = "departId", type = Query.Type.IN)
    private Set<Long> departIds = new HashSet<>();

    @Query
    private String certificationType;

    @Query
    private String jobType;

    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> dueDate;
}