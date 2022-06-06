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
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author TongMin Jie
 * @date 2022-06-06
 */
@Getter
@Setter
@Entity
@Table(name = "tool_schedule_dept")
public class ScheduleBindingDept extends BaseEntity implements Serializable {

    @Id
    @Column(name = "tool_schedule_dept_id")
    @NotNull(groups = Update.class)
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty(value = "绑定开放的部门标识")
    @Column(name = "dept_id")
    private Long deptId;


    @ApiModelProperty(value = "被绑定的培训计划标识")
    @Column(name = "train_schedule_id")
    private Long trScheduleId;

}