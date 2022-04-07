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
 * @date 2021-07-30
 */
@Entity
@Getter
@Setter
@Table(name = "issue_action")
public class IssueAction extends BaseEntity implements Serializable {

    @Id
    @Column(name = "action_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "ID", hidden = true)
    private Long id;

    @NotNull
    @ApiModelProperty(value = "问题标识")
    private Long issueId;

    @NotNull
    @ApiModelProperty(value = "措施名称")
    private String name;

    @ApiModelProperty(value = "措施状态",name="新建、进行中、完成、已移除")
    private String status;

    @ApiModelProperty(value = "具体描述")
    private String description;

    @ApiModelProperty(value = "负责人标识")
    private Long responsibleId;

    @ApiModelProperty(value = "有效性")
    private Double efficiency;

    @ApiModelProperty(value = "计划执行时间")
    private Timestamp plannedTime;

    @ApiModelProperty(value = "实际完成时间")
    private Timestamp completeTime;

    @ApiModelProperty(value = "其他")
    private String comment;

    @ApiModelProperty(value = "是否是围堵措施")
    private Boolean isCon;

    @NotBlank
    @ApiModelProperty(value = "所属步骤：D3/D5/D7")
    private String type;

    @ApiModelProperty(value = "系统编号")
    private Integer systemNum;

    @ApiModelProperty(value = "原因标识")
    private Long causeId;

    @ApiModelProperty(value = "确认方法")
    private String validationMethod;

    @ApiModelProperty(value = "确认结果")
    private String validationResult;

    @ApiModelProperty(value = "计划完成时间")
    private Timestamp plannedCompleteTime;

    @ApiModelProperty(value = "标识")
    private String identification;

    @ApiModelProperty(value = "测评方法")
    private String correctiveMeasurementMethod;

    @ApiModelProperty(value = "测评结果")
    private String correctiveEfficiencyResult;

    @ApiModelProperty(value = "评估日期")
    private Timestamp evaluationTime;

    @ApiModelProperty(value = "结论")
    private String conclusion;

    @ApiModelProperty(value = "移出时间")
    private Timestamp removeTime;

}
