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
import java.util.List;

/**
* @author TongMin Jie
* @date 2022-05-27 14:11:34
*/
@Data
@NoArgsConstructor
public class InstrumentQueryCriteria {

    @Query(blurry = "instruName,instruNum")
    private String blurry;

    @Query(blurry = "useArea,useBy")
    private String useBlurry;

    @Query
    private Boolean innerChecked;

    @Query
    private Boolean isDoor;

    @Query
    private String status;

    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> lastCaliDate;

    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> nextCaliDate;
}