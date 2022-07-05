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
 * @date 2022-05-07
 */
@Entity
@Getter
@Setter
@Table(name = "train_certification")
public class TrainCertification extends BaseEntity implements Serializable {

    @Id
    @Column(name = "train_certification_id")
    @NotNull(groups = Update.class)
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ApiModelProperty(value = "员工ID")
    private Long userId;

//    @NotBlank
    @ApiModelProperty(value = "员工姓名")
    private String staffName;

//    @NotNull
    @ApiModelProperty(value = "入职日期")
    private Timestamp hireDate;

//    @NotNull
    @ApiModelProperty(value = "所属部门ID")
    private Long departId;

//    @NotBlank
    @ApiModelProperty(value = "上级主管")
    private String superior;

    @ApiModelProperty(value = "工号")
    private String jobNum;

    @ApiModelProperty(value = "岗位级别")
    private String jobName;

    @NotBlank
    @ApiModelProperty(value = "认证证件种类")
    private String certificationType;

    @Column(name = "train_schedule_id")
    @ApiModelProperty(value = "培训计划ID")
    private Long trScheduleId;

//    @NotBlank
    @ApiModelProperty(value = "工种类型")
    private String jobType;

    @ApiModelProperty(value = "签发机构")
    private String orgName;

    @ApiModelProperty(value = "到期日期")
    private Timestamp dueDate;

    @NotNull
    @ApiModelProperty(value = "是否设置到期提醒")
    private Boolean isRemind;

    @ApiModelProperty(value = "提前提醒天数")
    private Integer remindDays;

    @ApiModelProperty(value = "培训日期")
    private Timestamp trainDate;

    @ApiModelProperty(value = "培训内容")
    private String trainContent;

    @ApiModelProperty(value = "发证日期")
    private Timestamp issueDate;

    @ApiModelProperty(value = "培训结果")
    private String trainResult;

    @ApiModelProperty(value = "认证状态")
    private String certificationStatus;

}