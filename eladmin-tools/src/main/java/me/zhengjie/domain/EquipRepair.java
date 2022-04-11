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
@Table(name = "equip_repair")
public class EquipRepair extends BaseEntity implements Serializable {

    @Id
    @Column(name = "repair_id")
    @NotNull(groups = Update.class)
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ApiModelProperty(value = "设备ID")
    private Long equipmentId;

    @ApiModelProperty(value = "维修单号")
    private String repairNum;

    @NotNull
    @ApiModelProperty(value = "停机时间")
    private Timestamp shutTime;

    @ApiModelProperty(value = "停机人员")
    private String shutBy;

    @NotBlank
    @ApiModelProperty(value = "维修负责人")
    private String repairBy;

    @ApiModelProperty(value = "维修费用")
    private String repairCost;

    @NotNull
    @ApiModelProperty(value = "开始维修时间")
    private Timestamp repairTime;

    @NotNull
    @ApiModelProperty(value = "结束维修时间")
    private Timestamp resolveTime;

    @NotNull
    @ApiModelProperty(value = "是否是故障")
    private Boolean isFault;

    @ApiModelProperty(value = "非故障说明")
    private String judgeReason;

    @ApiModelProperty(value = "维修步骤和过程")
    private String repairDesc;

    @ApiModelProperty(value = "确认人")
    private String confirmBy;

    @ApiModelProperty(value = "确认时间")
    private Timestamp confirmTime;

    @NotNull
    @ApiModelProperty(value = "是否确认完成")
    private Boolean isFinished;

}