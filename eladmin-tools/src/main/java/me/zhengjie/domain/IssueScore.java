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
@Table(name = "issue_score")
public class IssueScore extends BaseEntity implements Serializable {

    @Id
    @Column(name = "score_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "ID", hidden = true)
    private Long id;

    @NotNull
    @ApiModelProperty(value = "问题标识")
    private Long issueId;

    @NotNull
    @ApiModelProperty(value = "D#")
    private String name;

    @NotNull
    @ApiModelProperty(value = "步骤")
    private String content;

    @NotNull
    @ApiModelProperty(value = "分数类型")
    private String scoreType;

    @ApiModelProperty(value = "不合格")
    private Boolean unqualified = false;

    @ApiModelProperty(value = "合格（最低要求）")
    private Boolean qualified = true;

    @ApiModelProperty(value = "额外加分")
    private Boolean additional = false;

    @ApiModelProperty(value = "项目得分")
    private int score;
}
