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
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author Tong Minjie
 * @date 2021-07-23
 */
@Entity
@Getter
@Setter
@Table(name = "supplier_customer_audit")
public class SupplierCusAudit extends BaseEntity implements Serializable {

    @Id
    @Column(name = "customer_audit_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "ID", hidden = true)
    private Long id;

    @NotNull
    @ApiModelProperty(value = "供应商ID")
    private Long supplierId;

    @NotBlank
    @ApiModelProperty(value = "客户名称")
    private String customerName;

    @NotBlank
    @ApiModelProperty(value = "审核方法")
    private String auditMethod;

    @NotNull
    @ApiModelProperty(value = "审核日期")
    private Timestamp auditDate;

    @NotBlank
    @ApiModelProperty(value = "审核结果")
    private String auditResult;
}
