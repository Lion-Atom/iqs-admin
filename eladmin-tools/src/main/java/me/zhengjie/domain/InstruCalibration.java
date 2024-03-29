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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.zhengjie.base.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author TongMinjie
 * @date 2022-05-30
 */
@Getter
@Setter
@Entity
@Table(name = "instru_calibration")
@NoArgsConstructor
public class InstruCalibration extends BaseEntity implements Serializable {

    @Id
    @Column(name = "instru_calibration_id")
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ApiModelProperty(value = "仪器仪表ID")
    private Long instruId;

    @NotNull
    @ApiModelProperty(value = "校准日期")
    private Timestamp caliDate;

    @NotNull
    @ApiModelProperty(value = "是否是内部校准")
    private Boolean innerChecked;

    @ApiModelProperty(value = "校准机构ID")
    private Long caliOrgId;

    @ApiModelProperty(value = "是否是上门校准")
    private Boolean isDoor;

    @NotBlank
    @ApiModelProperty(value = "校准结果是否合格")
    private String caliResult;

    @ApiModelProperty(value = "不合格原因描述")
    private String failDesc;

}