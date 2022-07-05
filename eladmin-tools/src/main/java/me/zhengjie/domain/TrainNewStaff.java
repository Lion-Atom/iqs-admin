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
 * @date 2022-05-06
 */
@Entity
@Getter
@Setter
@Table(name = "train_new_staff")
public class TrainNewStaff extends BaseEntity implements Serializable {

    @Id
    @Column(name = "new_staff_train_id")
    @NotNull(groups = Update.class)
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ApiModelProperty(value = "员工ID")
    private Long userId;

    @NotBlank
    @ApiModelProperty(value = "员工分类")
    private String staffType;

    @ApiModelProperty(value = "工种")
    private String jobType;

    @NotBlank
    @ApiModelProperty(value = "员工姓名")
    private String staffName;

    @NotNull
    @ApiModelProperty(value = "入职日期")
    private Timestamp hireDate;

    @NotNull
    @ApiModelProperty(value = "所属部门ID")
    private Long departId;

    @ApiModelProperty(value = "车间")
    private String workshop;

    @ApiModelProperty(value = "班组")
    private String team;

    @ApiModelProperty(value = "上级主管")
    private String superior;

    @ApiModelProperty(value = "工号")
    private String jobNum;

    //    @NotBlank
    @ApiModelProperty(value = "岗位")
    private String jobName;

    @NotNull
    @Column(name = "train_schedule_id")
    @ApiModelProperty(value = "培训计划ID")
    private Long trScheduleId;

//    @NotBlank
//    @ApiModelProperty(value = "培训内容")
//    private String trainContent;

    @NotNull
    @ApiModelProperty(value = "是否完成入职培训")
    private Boolean isFinished;

    @ApiModelProperty(value = "未完成培训原因")
    private String reason;

    @NotNull
    @ApiModelProperty(value = "允许操作")
    private Boolean isAuthorize;
}