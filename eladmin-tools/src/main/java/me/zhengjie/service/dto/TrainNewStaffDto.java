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
import lombok.EqualsAndHashCode;
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
 * @date 2022-05-06
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TrainNewStaffDto extends BaseDTO implements Serializable {

    private Long id;

    private String staffType;

    private String staffName;

    private Timestamp hireDate;

    private Long departId;

    private String departName;

    private String workshop;

    private String team;

    private String superior;

    private String jobNum;

    private String jobName;

    private String trainContent;

    private Boolean isFinished;

    private String reason;

}