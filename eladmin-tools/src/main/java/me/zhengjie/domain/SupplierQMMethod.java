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
import java.sql.Timestamp;

/**
 * @author Tong Minjie
 * @date 2021-12-2302
 */
@Entity
@Getter
@Setter
@Table(name = "supplier_qm_method")
public class SupplierQMMethod extends BaseEntity implements Serializable {

    @Id
    @Column(name = "method_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "ID", hidden = true)
    private Long id;

    @NotNull
    @ApiModelProperty(value = "供应商ID")
    private Long supplierId;

    @NotBlank
    @ApiModelProperty(value = "质量管理方法名称")
    private String methodName;

    @ApiModelProperty(value = "是否被使用")
    private Boolean isUsed = false;

    @ApiModelProperty(value = "是否没被使用")
    private Boolean notUsed = false;

}
