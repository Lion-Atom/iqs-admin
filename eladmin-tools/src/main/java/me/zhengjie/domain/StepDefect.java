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
 * @author Tong Minjie
 * @date 2021-07-27
 */
@Entity
@Getter
@Setter
@Table(name = "process_step_defect")
public class StepDefect extends BaseEntity implements Serializable {

    @Id
    @Column(name = "defect_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "ID", hidden = true)
    private Long id;

    @NotNull
    @ApiModelProperty(value = "问题标识")
    private Long issueId;

    @NotNull
    @ApiModelProperty(value = "用户标识")
    private String processStep;

    @ApiModelProperty(value = "是否被创建")
    private Boolean created = false;

    @ApiModelProperty(value = "是否被检测")
    private Boolean detected = false;

    @ApiModelProperty(value = "应当被检测")
    private Boolean shouldDetected = false;
}
