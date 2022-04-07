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
import me.zhengjie.domain.CalibrationFile;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tong Minjie
 * @date 2022-03-14
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InstruCaliDto extends BaseDTO implements Serializable {

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

    private String precise;

    private String errorRange;

    private String useArea;

    private String useBy;

    private Boolean isDroped;

    private String dropRemark;

    private Boolean isRemind;

    private Integer remindDays;

    private String status;

    private Long uid;

    private List<CalibrationFile> fileList = new ArrayList<>();

}