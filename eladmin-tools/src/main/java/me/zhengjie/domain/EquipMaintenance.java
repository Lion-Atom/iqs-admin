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
@Table(name = "equip_maintenance")
public class EquipMaintenance extends BaseEntity implements Serializable {

    @Id
    @Column(name = "maintenance_id")
    @NotNull(groups = Update.class)
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ApiModelProperty(value = "设备ID")
    private Long equipmentId;

    @NotNull
    private Timestamp maintainDate;

    @NotBlank
    @ApiModelProperty(value = "保养人")
    private String maintainBy;

    @ApiModelProperty(value = "保养时长")
    private String maintainDuration;

    @ApiModelProperty(value = "确认人")
    private String confirmBy;

    @ApiModelProperty(value = "保养结果")
    private String maintainResult;

    @ApiModelProperty(value = "保养描述")
    private String maintainDesc;
}