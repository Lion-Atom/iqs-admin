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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import me.zhengjie.base.BaseEntity;
import me.zhengjie.service.dto.TimeManagementDto;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author Tong Minjie
 * @Timestamp 2021-07-23
 */
@Entity
@Getter
@Setter
@Table(name = "tool_time_management")
public class TimeManagement extends BaseEntity implements Serializable {

    @Id
    @Column(name = "management_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "ID", hidden = true)
    private Long id;

    @NotNull
    @ApiModelProperty(value = "问题标识")
    private Long issueId;

    @ApiModelProperty(value = "D1状态")
    @Column(name="d1_status")
    private Boolean d1Status = false;

    @ApiModelProperty(value = "D1时间")
    @Column(name="d1_time")
    private Timestamp d1Time;

    @ApiModelProperty(value = "D2状态")
    @Column(name="d2_status")
    private Boolean d2Status = false;

    @ApiModelProperty(value = "D2时间")
    @Column(name="d2_time")
    private Timestamp d2Time;

    @ApiModelProperty(value = "D3状态")
    @Column(name="d3_status")
    private Boolean d3Status = false;

    @ApiModelProperty(value = "D3时间")
    @Column(name="d3_time")
    private Timestamp d3Time;

    @ApiModelProperty(value = "D4状态")
    @Column(name="d4_status")
    private Boolean d4Status = false;

    @ApiModelProperty(value = "D4时间")
    @Column(name="d4_time")
    private Timestamp d4Time;

    @ApiModelProperty(value = "D5状态")
    @Column(name="d5_status")
    private Boolean d5Status = false;

    @ApiModelProperty(value = "D5时间")
    @Column(name="d5_time")
    private Timestamp d5Time;

    @ApiModelProperty(value = "D46状态")
    @Column(name="d6_status")
    private Boolean d6Status = false;

    @ApiModelProperty(value = "D6时间")
    @Column(name="d6_time")
    private Timestamp d6Time;

    @ApiModelProperty(value = "D7状态")
    @Column(name="d7_status")
    private Boolean d7Status = false;

    @ApiModelProperty(value = "D7时间")
    @Column(name="d7_time")
    private Timestamp d7Time;

    @ApiModelProperty(value = "D8状态")
    @Column(name="d8_status")
    private Boolean d8Status = false;

    @ApiModelProperty(value = "D8时间")
    @Column(name="d8_time")
    private Timestamp d8Time;

    @ApiModelProperty(value = "计划步骤1")
    @Column(name="plan_step1")
    private String planStep1;

    @ApiModelProperty(value = "计划时间1")
    @Column(name="plan_time1")
    private Timestamp planTime1;

    @ApiModelProperty(value = "计划步骤2")
    @Column(name="plan_step2")
    private String planStep2;

    @ApiModelProperty(value = "计划时间2")
    @Column(name="plan_time2")
    private Timestamp planTime2;

    @ApiModelProperty(value = "计划步骤3")
    @Column(name="plan_step3")
    private String planStep3;

    @ApiModelProperty(value = "计划时间3")
    @Column(name="plan_time3")
    private Timestamp planTime3;

    public void copy(TimeManagementDto source) {
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
    }

}
