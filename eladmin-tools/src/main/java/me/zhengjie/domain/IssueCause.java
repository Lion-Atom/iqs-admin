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
import java.util.Objects;

/**
 * @author Tong Minjie
 * @date 2021-07-28
 */
@Entity
@Getter
@Setter
@Table(name = "issue_cause")
public class IssueCause extends BaseEntity implements Serializable {

    @Id
    @Column(name = "cause_id")
    @NotNull(groups = Update.class)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "ID", hidden = true)
    private Long id;

    @NotNull
    @ApiModelProperty(value = "问题标识")
    private Long issueId;

    @ApiModelProperty(value = "原因名称")
    private String name;

    @ApiModelProperty(value = "是否是发生",name = "true:发生，false:检测")
    private String judgeResult;

    @ApiModelProperty(value = "确认方法")
    private String method;

    @ApiModelProperty(value = "确认结果")
    private String result;

    @NotNull(groups = Update.class)
    @ApiModelProperty(value = "原因占比")
    private Double contribution;

    @ApiModelProperty(value = "评论")
    private String comment;

    @ApiModelProperty(value = "是否是根本原因")
    private Boolean isExact = false;

    @ApiModelProperty(value = "上级节点")
    private Long pid;

    @ApiModelProperty(value = "子节点数目", hidden = true)
    private Integer subCount = 0;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IssueCause cause = (IssueCause) o;
        return Objects.equals(id, cause.id) &&
                Objects.equals(name, cause.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
