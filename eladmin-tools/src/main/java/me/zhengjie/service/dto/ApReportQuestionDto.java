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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.zhengjie.base.BaseDTO;
import me.zhengjie.base.BaseEntity;
import me.zhengjie.domain.ApQuestionAction;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author TongMinjie
 * @date 2019-09-09
 */
@Getter
@Setter
public class ApReportQuestionDto extends BaseDTO implements Serializable {

    private Long id;

    private Long planId;

    private String planName;

    private Long planReportId;

    private String name;

    private Boolean isRepeat;

    private Boolean isCompleted;

    private List<String> actionNames;

}