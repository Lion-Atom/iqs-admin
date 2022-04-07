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

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import me.zhengjie.base.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2021-07-23
 */
@Entity
@Getter
@Setter
@Table(name = "supplier_contact")
public class SupplierContact extends BaseEntity implements Serializable {

    @Id
    @Column(name = "supplier_contact_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "ID", hidden = true)
    private Long id;

    @NotNull
    @ApiModelProperty(value = "供应商ID")
    private Long supplierId;

    @NotBlank
    @ApiModelProperty(value = "姓名")
    private String name;

    @NotBlank
    @ApiModelProperty(value = "性别")
    private String gender;

    @NotBlank
    @ApiModelProperty(value = "重要程度")
    private String importantLevel;

    @ApiModelProperty(value = "单位")
    private String unit;

    @ApiModelProperty(value = "称谓")
    private String title;

    @ApiModelProperty(value = "在职状态")
    private String jobStatus;

    @ApiModelProperty(value = "部门")
    private String department;

    @ApiModelProperty(value = "职务")
    private String post;

    @ApiModelProperty(value = "负责业务")
    private String business;

    @ApiModelProperty(value = "工作电话")
    private String workTel;

    @ApiModelProperty(value = "传真")
    private String fax;

    @ApiModelProperty(value = "移动电话")
    private String phone;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "家庭电话")
    private String homeTel;

    @ApiModelProperty(value = "MSN")
    private String msn;

    @ApiModelProperty(value = "QQ号码")
    private String qq;

    @ApiModelProperty(value = "邮编")
    private String zip;

    @ApiModelProperty(value = "联系人类型")
    private String type;

    @ApiModelProperty(value = "年龄")
    private Integer age;

    @ApiModelProperty(value = "证件类型")
    private String idType;

    @ApiModelProperty(value = "证件号")
    private String idNum;

    @ApiModelProperty(value = "住址")
    private String address;

    @ApiModelProperty(value = "住址")
    private Timestamp birthday;

    @ApiModelProperty(value = "兴趣爱好")
    private String hobby;

    @ApiModelProperty(value = "头像地址")
    private String avatarName;

    @ApiModelProperty(value = "头像真实地址")
    private String avatarPath;

    @ApiModelProperty(value = "备注")
    private String remark;
}
