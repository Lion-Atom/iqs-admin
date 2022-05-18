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
 * @author TongMin Jie
 * @date 2022-5-16
 */
@Data
@NoArgsConstructor
public class TrainExamStaffQueryCriteria {

    @Query(blurry = "staffName,jobName,jobType,workshop")
    private String blurry;

    @Query(propName = "departId", type = Query.Type.EQUAL)
    private Long departId;

    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;
}