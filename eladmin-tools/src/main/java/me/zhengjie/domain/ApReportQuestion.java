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
 * @date 2019-09-09
 */
@Getter
@Setter
@Entity
@Table(name = "plan_report_question")
@NoArgsConstructor
public class ApReportQuestion extends BaseEntity implements Serializable {

    @Id
    @Column(name = "report_question_id")
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ApiModelProperty(value = "审核计划报告ID")
    private Long planReportId;

    @NotBlank
    @ApiModelProperty(value = "问题点名称")
    private String name;

    @ApiModelProperty(value = "是否重复")
    private Boolean isRepeat = false;

    @ApiModelProperty(value = "是否改善完成")
    private Boolean isCompleted = false;

}