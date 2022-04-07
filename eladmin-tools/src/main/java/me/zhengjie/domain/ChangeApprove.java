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
 * @date 2022-01-18
 */
@Entity
@Getter
@Setter
@Table(name = "change_approve")
public class ChangeApprove extends BaseEntity implements Serializable {

    @Id
    @Column(name = "approve_id")
    @NotNull(groups = Update.class)
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ApiModelProperty(value = "变更信息ID")
    private Long changeId;

    @ApiModelProperty(value = "变更实现时间")
    private Timestamp implementedTime;

    @ApiModelProperty(value = "发给客户审批时间")
    private Timestamp sendToCusTime;

    @ApiModelProperty(value = "客户验收时间")
    private Timestamp acceptedByCusTime;

    @ApiModelProperty(value = "内部团队同意变更的时间")
    private Timestamp acceptedByInternalTime;

    @NotNull
    @ApiModelProperty(value = "已完成步骤")
    private Integer finishedStep;

}