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
import java.util.Objects;
import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2021-09-06
 */
@Entity
@Getter
@Setter
@Table(name = "tools_change")
public class Change extends BaseEntity implements Serializable {

    @Id
    @Column(name = "change_id")
    @NotNull(groups = Update.class)
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty(value = "已完成步骤")
    private Integer finishedStep;

    @ApiModelProperty(value = "变更编码 /n 自动生成")
    private String changeNum;

    @NotBlank
    @ApiModelProperty(value = "变更原因")
    private String reason;

    @NotBlank
    @ApiModelProperty(value = "变更来源")
    private String source;

    @NotBlank
    @ApiModelProperty(value = "发起人")
    private String initiator;

    @NotBlank
    @ApiModelProperty(value = "发起部门")
    private String department;

    @ApiModelProperty(value = "发起时间")
    private Timestamp initTime;

    @NotBlank
    @ApiModelProperty(value = "受影响地区")
    private String area;

    @NotBlank
    @ApiModelProperty(value = "涉及部门")
    private String depart;

    @NotBlank
    @ApiModelProperty(value = "受影响项目")
    private String project;

    @NotBlank
    @ApiModelProperty(value = "受影响产品")
    private String production;

    @NotBlank
    @ApiModelProperty(value = "费用评估")
    private String cost;

    // -----分类-----
    // 子表-人机料法环等因素

    @ApiModelProperty(value = "是否是客户要求")
    private Boolean isCustomer;

    @ApiModelProperty(value = "非客户要求的备注")
    private String remark;

    @ApiModelProperty(value = "客户邮箱")
    private String email;

    // -----分析-----
    // 子附件表-变更范围: 文档，过程，工具和其他
    @ApiModelProperty(value = "变更范围")
    private String scope;

    @ApiModelProperty(value = "有无客户协议")
    private Boolean havAgreement;

    // -----确认/创建-----
    @ApiModelProperty(value = "审批部门")
    private String approveDepart;

    @ApiModelProperty(value = "审批人")
    private String approveBy;

    @ApiModelProperty(value = "有无评估报告")
    private Boolean havReport;

    @ApiModelProperty(value = "是否接受变更")
    private Boolean isAccepted;

    @ApiModelProperty(value = "变更状态")
    private String status;

}