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
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author TongMin jie
 * @date 2022-07-14
 */
@Entity
@Getter
@Setter
@Table(name = "cs_feedback")
public class CsFeedback extends BaseEntity implements Serializable {

    @Id
    @Column(name = "cs_feedback_id")
    @NotNull(groups = Update.class)
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @ApiModelProperty(value = "客户企业名称")
    private String companyName;

    @NotBlank
    @ApiModelProperty(value = "问题分类")
    private String type;

    @NotBlank
    @Column(name = "feedback_desc")
    @ApiModelProperty(value = "问题描述")
    private String desc;

    @ApiModelProperty(value = "QQ号码")
    private String qq;

    @ApiModelProperty(value = "手机号码")
    private String phone;

    @Email
    @ApiModelProperty(value = "电子邮箱")
    private String email;

    @ApiModelProperty(value = "反馈状态")
    private String status;

}