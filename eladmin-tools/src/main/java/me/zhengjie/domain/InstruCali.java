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
 * @date 2021-09-06
 */
@Entity
@Getter
@Setter
@Table(name = "tools_calibration")
public class InstruCali extends BaseEntity implements Serializable {

    @Id
    @Column(name = "calibration_id")
    @NotNull(groups = Update.class)
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @ApiModelProperty(value = "仪器名称")
    private String instruName;

    @ApiModelProperty(value = "出厂型号")
    private String instruNum;

    @ApiModelProperty(value = "资产号")
    private String assetNum;

    @ApiModelProperty(value = "出厂日期")
    private Timestamp purDate;

    @NotBlank
    @ApiModelProperty(value = "内部ID")
    private String innerId;

    @NotNull
    @ApiModelProperty(value = "校准周期")
    private Integer caliPeriod;

    @NotBlank
    @ApiModelProperty(value = "校准周期时间单位")
    private String periodUnit;

    @NotNull
    @ApiModelProperty(value = "上次校准日期")
    private Timestamp lastCaliDate;

    @NotNull
    @ApiModelProperty(value = "下次校准日期")
    private Timestamp nextCaliDate;

    @NotNull
    @ApiModelProperty(value = "是否是内部校准")
    private Boolean innerChecked;

    @ApiModelProperty(value = "校准机构ID")
    private Long caliOrgId;

    @ApiModelProperty(value = "是否是上门校准")
    private Boolean isDoor;

    @NotBlank
    @ApiModelProperty(value = "测量范围")
    private String caliScope;

    @NotBlank
    @ApiModelProperty(value = "精度要求")
    private String precise;

    @NotBlank
    @ApiModelProperty(value = "允许误差")
    private String errorRange;

    @NotBlank
    @ApiModelProperty(value = "使用区域")
    private String useArea;

    @NotBlank
    @ApiModelProperty(value = "使用人")
    private String useBy;

    @NotBlank
    @ApiModelProperty(value = "存放位置")
    private String position;

    @NotBlank
    @ApiModelProperty(value = "保管人")
    private String keeper;

    @NotNull
    @ApiModelProperty(value = "是否作废")
    private Boolean isDroped;

    @ApiModelProperty(value = "作废说明")
    private String dropRemark;

    @NotNull
    @ApiModelProperty(value = "是否需要下次校准前提醒")
    private Boolean isRemind;

    @ApiModelProperty(value = "提前提醒天数（需要提醒则须填写提前几天提醒）")
    private Integer remindDays;

//    @NotBlank
    @ApiModelProperty(value = "仪器校准状态")
    private String status;

}