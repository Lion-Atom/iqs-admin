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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import me.zhengjie.base.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Tong Minjie
 * @date 2022-01-18
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "change_management")
public class ChangeManagement extends BaseEntity implements Serializable {

    @Id
    @Column(name = "management_id")
    @NotNull(groups = Update.class)
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ApiModelProperty(value = "变更信息ID")
    private Long changeId;

    @NotBlank
    @ApiModelProperty(value = "零件图纸是否变更")
    private String partDrawing;

    @NotBlank
    @ApiModelProperty(value = "文档/作业程序/指导书是否变更")
    private String document;

    @NotBlank
    @ApiModelProperty(value = "评估报告是否变更")
    private String evaluationReport;

    @NotBlank
    @ApiModelProperty(value = "生产件批准程序是否变更")
    private String ppap;

    @NotBlank
    @ApiModelProperty(value = "管制计划是否变更")
    private String controlPlan;

    @NotBlank
    @ApiModelProperty(value = "产品质量先期策划是否变更")
    private String apqp;

    @NotBlank
    @ApiModelProperty(value = "样品零件是否变更")
    private String samplePart;

    @NotBlank
    @ApiModelProperty(value = "工具是否变更")
    private String tools;

    @NotBlank
    @ApiModelProperty(value = "其它是否变更")
    private String other;

    @NotNull
    @ApiModelProperty(value = "已完成步骤")
    private Integer finishedStep;

}