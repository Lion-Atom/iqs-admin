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

/**
 * 5W2H
 *
 * @author Tong Minjie
 * @date 2021-07-27
 */
@Entity
@Getter
@Setter
@Table(name = "issue_question")
public class IssueQuestion extends BaseEntity implements Serializable {

    @Id
    @Column(name = "question_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "ID", hidden = true)
    private Long id;

    @NotNull
    @ApiModelProperty(value = "问题标识")
    private Long issueId;

    @ApiModelProperty(value = "5W2H描述名称")
    private String name;

    @ApiModelProperty(value = "描述信息")
    private String description;

    @NotBlank
    @ApiModelProperty(value = "类型")
    private String type;

    @ApiModelProperty(value = "是-内容")
    private String isContent;

    @ApiModelProperty(value = "否-内容")
    private String notContent;

}
