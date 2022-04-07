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
 * @date 2022-03-29
 */
@Entity
@Getter
@Setter
@Table(name = "equip_acceptance")
public class EquipAcceptance extends BaseEntity implements Serializable {

    @Id
    @Column(name = "acceptance_id")
    @NotNull(groups = Update.class)
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ApiModelProperty(value = "设备ID")
    private Long equipmentId;

    @NotBlank
    @ApiModelProperty(value = "验收状态")
    private String acceptStatus;

    @NotBlank
    @ApiModelProperty(value = "验收参与部门人员")
    private String acceptParticipant;

    @ApiModelProperty(value = "验收部门")
    private Long acceptDepart;

    @ApiModelProperty(value = "验收人")
    private String acceptBy;

    @ApiModelProperty(value = "提交人")
    private String submitBy;

    @ApiModelProperty(value = "提交时间")
    private Timestamp submitTime;

    @ApiModelProperty(value = "批准部门")
    private Long approveDepart;

    @ApiModelProperty(value = "批准人")
    private String approveBy;

    @ApiModelProperty(value = "批准时间")
    private Timestamp approveTime;

    @ApiModelProperty(value = "批准结果")
    private String approveResult;

    @ApiModelProperty(value = "不批准原因")
    private String refuseReason;
}