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
import lombok.EqualsAndHashCode;
import me.zhengjie.base.BaseDTO;
import me.zhengjie.domain.TrScheduleFile;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tong Minjie
 * @date 2022-05-18
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TrainScheduleDto extends BaseDTO implements Serializable {

    private Long id;

    private String trainTitle;

    private Timestamp trainTime;

    private String trainContent;

    private Timestamp regDeadline;

    private String trainLocation;

    private Integer cost;

    private String trainer;

    private String trainIns;

    private String department;

    private String trainType;

    private Integer totalNum;

    private Integer curNum;

    private Boolean isRemind;

    private String remindDays;

    private Boolean isDelay;

    private Timestamp newTrainTime;

    private String delayDesc;

    private List<TrScheduleFile> fileList = new ArrayList<>();

}