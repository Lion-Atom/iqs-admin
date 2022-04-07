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
 * @date 2021-07-23
 */
@Getter
@Setter
public class SupplierContactDto extends BaseDTO implements Serializable {

    private Long id;

    private Long supplierId;

    private String supplierName;

    private String name;

    private String gender;

    private String importantLevel;

    private String unit;

    private String title;

    private String jobStatus;

    private String department;

    private String post;

    private String business;

    private String workTel;

    private String fax;

    private String phone;

    private String email;

    private String homeTel;

    private String msn;

    private String qq;

    private String zip;

    private String type;

    private Integer age;

    private String idType;

    private String idNum;

    private String address;

    private Timestamp birthday;

    private String hobby;

    private String[] hobbyTags;

    private String avatarName;

    private String avatarPath;

    private String remark;
}
