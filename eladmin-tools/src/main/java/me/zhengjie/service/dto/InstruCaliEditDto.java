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
package me.zhengjie.service.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import me.zhengjie.base.BaseDTO;
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
@Data
public class InstruCaliEditDto extends BaseDTO implements Serializable {

    private Long id;

    private String instruName;

    private String instruNum;

    private String assetNum;

    private Timestamp purDate;

    private String innerId;

    private Integer caliPeriod;

    private String periodUnit;

    private Timestamp lastCaliDate;

    private Timestamp nextCaliDate;

    private Boolean innerChecked;

    private Boolean isDoor;

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