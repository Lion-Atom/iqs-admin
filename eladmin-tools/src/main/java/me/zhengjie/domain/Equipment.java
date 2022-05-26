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
@Table(name = "tools_equipment")
public class Equipment extends BaseEntity implements Serializable {

    @Id
    @Column(name = "equipment_id")
    @NotNull(groups = Update.class)
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @ApiModelProperty(value = "设备名称")
    private String equipName;

    @ApiModelProperty(value = "设备厂家")
    private String equipProvider;

    @NotNull
    @ApiModelProperty(value = "设备编号")
    private String equipNum;

    @ApiModelProperty(value = "出厂编号")
    private String factoryNum;

    @ApiModelProperty(value = "设备型号")
    private String equipModel;

    @ApiModelProperty(value = "设备规格")
    private String equipSpec;

    @ApiModelProperty(value = "设备重量")
    private String equipWeight;

    @ApiModelProperty(value = "设备尺寸")
    private String equipSize;

//    @NotBlank
    @ApiModelProperty(value = "资产号")
    private String assetNum;

    @NotBlank
    @ApiModelProperty(value = "设备状态")
    private String equipStatus;

    @ApiModelProperty(value = "出厂日期")
    private Timestamp saleDate;

    @ApiModelProperty(value = "收到日期")
    private Timestamp receiveDate;

    @ApiModelProperty(value = "购置日期")
    private Timestamp purDate;

    @ApiModelProperty(value = "投用日期")
    private Timestamp useDate;

    @NotNull
    @ApiModelProperty(value = "有无验收报告")
    private Boolean havAcceptReport;

    @NotNull
    @ApiModelProperty(value = "使用部门")
    private Long useDepart;

    @NotBlank
    @ApiModelProperty(value = "所在位置")
    private String useArea;

    @ApiModelProperty(value = "使用人")
    private String useBy;

    @ApiModelProperty(value = "设备类别")
    private String equipType;

    @ApiModelProperty(value = "设备级别")
    private String equipLevel;

    @ApiModelProperty(value = "验收状态")
    private String acceptStatus;

    @ApiModelProperty(value = "保养状态")
    private String maintainStatus;

    @ApiModelProperty(value = "设备-电压")
    private String equipOltage;

    @ApiModelProperty(value = "设备-压缩空气")
    private String equipAir;

    @ApiModelProperty(value = "设备-水")
    private String equipWater;

    @ApiModelProperty(value = "设备-其它")
    private String equipOther;

    @ApiModelProperty(value = "保养级别")
    private String maintainLevel;

    @ApiModelProperty(value = "保养周期")
    private Integer maintainPeriod;

    @ApiModelProperty(value = "保养周期时间单位")
    private String maintainPeriodUnit;

    @ApiModelProperty(value = "上次保养日期")
    private Timestamp lastMaintainDate;

    @ApiModelProperty(value = "保养到期日期")
    private Timestamp maintainDueDate;

    @ApiModelProperty(value = "是否需要下次校准前提醒")
    private Boolean isRemind;

    @ApiModelProperty(value = "提前提醒天数（需要提醒则须填写提前几天提醒）")
    private Integer remindDays;

    @ApiModelProperty(value = "参加验收的部门人员")
    private String acceptBy;

    @ApiModelProperty(value = "设备净值")
    private String netValue;

    @ApiModelProperty(value = "投资回报率")
    private String roi;

}