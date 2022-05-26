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

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.zhengjie.base.BaseDTO;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author Tong Minjie
 * @date 2022-03-30
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class EquipmentDto extends BaseDTO implements Serializable {

    private Long id;

    private String equipName;

    private String equipProvider;

    private String factoryNum;

    private String equipNum;

    private String equipStatus;

    private String equipModel;

    private String equipSpec;

    private String equipWeight;

    private String equipSize;

    private String assetNum;

    private Timestamp saleDate;

    private Timestamp receiveDate;

    private Timestamp purDate;

    private Timestamp useDate;

    private Boolean havAcceptReport;

    private Long useDepart;

    private String useDepartName;

    private String useArea;

    private String useBy;

    private String equipType;

    private String equipLevel;

    private String acceptStatus;

    private String maintainStatus;

    private String equipOltage;

    private String equipAir;

    private String equipWater;

    private String equipOther;

    private String maintainLevel;

    private Integer maintainPeriod;

    private String maintainPeriodUnit;

    private Timestamp lastMaintainDate;

    private Timestamp maintainDueDate;

    private Boolean isRemind;

    private Integer remindDays;

    private String acceptBy;

    private String[] acceptByList;

    private String netValue;

    private String roi;

}