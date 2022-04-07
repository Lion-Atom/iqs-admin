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
import java.sql.Timestamp;

/**
 * @author Tong Minjie
 * @date 2021-07-23
 */
@Entity
@Getter
@Setter
@Table(name = "tool_issue_number")
public class IssueNum extends BaseEntity implements Serializable {

    @Id
    @Column(name = "number_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "ID", hidden = true)
    private Long id;

    @NotNull
    @ApiModelProperty(value = "问题标识")
    private Long issueId;

    @ApiModelProperty(value = "产品料号")
    private String caPartNum;

    @ApiModelProperty(value = "产品生产日期")
    private Timestamp componentDateCode;

    @ApiModelProperty(value = "产品批号")
    private String componentLotNum;

    @ApiModelProperty(value = "不良数量")
    private String defectQuantity;

    @ApiModelProperty(value = "客户影响")
    private String customerImpact;

}
