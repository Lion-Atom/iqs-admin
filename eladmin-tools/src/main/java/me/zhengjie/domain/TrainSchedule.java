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
package me.zhengjie.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import me.zhengjie.base.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author Tong Minjie
 * @date 2022-05-18
 */
@Entity
@Getter
@Setter
@Table(name = "train_schedule")
public class TrainSchedule extends BaseEntity implements Serializable {

    @Id
    @Column(name = "train_schedule_id")
    @NotNull(groups = Update.class)
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @ApiModelProperty(value = "培训标题")
    private String trainTitle;

    @NotNull
    @ApiModelProperty(value = "培训时间")
    private Timestamp trainTime;

    @NotNull
    @ApiModelProperty(value = "培训内容")
    private String trainContent;

    @NotNull
    @ApiModelProperty(value = "报名截止时间")
    private Timestamp regDeadline;

    @ApiModelProperty(value = "培训地点")
    private String trainLocation;

    @ApiModelProperty(value = "培训费用")
    private Integer cost;

    @NotBlank
    @ApiModelProperty(value = "培训员")
    private String trainer;

    @ApiModelProperty(value = "培训机构")
    private String trainIns;

    @NotBlank
    @ApiModelProperty(value = "涉及部门")
    private String department;

    @NotBlank
    @ApiModelProperty(value = "培训类型")
    private String trainType;

    @NotNull
    @ApiModelProperty(value = "人数限制")
    private Integer totalNum;

    @NotNull
    @ApiModelProperty(value = "当前与会人数")
    private Integer curNum;

    @NotNull
    @ApiModelProperty(value = "是否需要提前提醒")
    private Boolean isRemind;

    @ApiModelProperty(value = "提前提醒天数")
    private String remindDays;

    @NotNull
    @ApiModelProperty(value = "是否延期")
    private Boolean isDelay;

    @ApiModelProperty(value = "新的培训时间")
    private Timestamp newTrainTime;

    @ApiModelProperty(value = "延期原因描述")
    private String delayDesc;

}