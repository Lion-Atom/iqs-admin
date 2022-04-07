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
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author Tong Minjie
 * @date 2021-07-27
 */
@Entity
@Getter
@Setter
@Table(name = "question_action")
public class ApQuestionAction extends BaseEntity implements Serializable {

    @Id
    @Column(name = "question_action_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "ID", hidden = true)
    private Long id;

    @NotNull
    @ApiModelProperty(value = "审核计划ID")
    private Long planId;

    @NotNull
    @ApiModelProperty(value = "报告下问题ID")
    private Long reportQuestionId;

    @NotNull
    @ApiModelProperty(value = "改善对策标题")
    private String title;

    @ApiModelProperty(value = "对策描述")
    private String description;

    @ApiModelProperty(value = "负责人")
    private String responsibleBy;

    @ApiModelProperty(value = "有效性")
    private Integer efficiency;

    @ApiModelProperty(value = "计划执行时间")
    private Timestamp planTime;

    @ApiModelProperty(value = "实际落实时间")
    private Timestamp completeTime;

    @ApiModelProperty(value = "确认人")
    private String validateBy;

    @ApiModelProperty(value = "确认时间")
    private Timestamp validateTime;

    @ApiModelProperty(value = "结案状态")
    private String status;

    @ApiModelProperty(value = "其他备注")
    private String other;

}
