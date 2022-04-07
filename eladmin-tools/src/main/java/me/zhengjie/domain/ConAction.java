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
@Table(name = "issue_containment_action")
public class ConAction extends BaseEntity implements Serializable {

    @Id
    @Column(name = "containment_action_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "ID", hidden = true)
    private Long id;

    @NotNull
    @ApiModelProperty(value = "问题标识")
    private Long issueId;

    @NotNull
    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "良品数量")
    private String qtyOk;

    @ApiModelProperty(value = "不良品数量")
    private String qtyNo;

    @ApiModelProperty(value = "措施标识")
    private Long actionId;

    @ApiModelProperty(value = "措施名称")
    private String actionName;

    @ApiModelProperty(value = "负责人标识")
    private Long responsibleId;

    @ApiModelProperty(value = "有效性(%)")
    private Double efficiency;

    @ApiModelProperty(value = "产品标识")
    private String partIdentification;

    @ApiModelProperty(value = "计划执行时间")
    private Timestamp plannedTime;

    @ApiModelProperty(value = "实际执行时间")
    private Timestamp actualTime;

}
