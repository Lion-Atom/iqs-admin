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
@Table(name = "tools_auditor")
public class Auditor extends BaseEntity implements Serializable {

    @Id
    @Column(name = "auditor_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "ID", hidden = true)
    private Long id;

    @NotNull
    @ApiModelProperty(value = "用户标识")
    private Long userId;

    @NotBlank
    @ApiModelProperty(value = "体系")
    private String system;

    @ApiModelProperty(value = "状态")
    private String status;

    @ApiModelProperty(value = "认证时间")
    private Timestamp certificationTime;

    @ApiModelProperty(value = "有效期限")
    private Integer validity;

    @ApiModelProperty(value = "下一次认证时间")
    private Timestamp nextCertificationTime;

    @ApiModelProperty(value = "认证单位")
    private String certificationUnit;

    @ApiModelProperty(value = "批准人")
    private Long approvedBy;

    @ApiModelProperty(value = "审核状态")
    private String approvalStatus;

    @ApiModelProperty(value = "驳回建议")
    private String rejectComment;

    @ApiModelProperty(value = "批准时间")
    private Timestamp approvedTime;

    @JSONField(serialize = false)
    @ManyToMany(mappedBy = "auditors")
    @ApiModelProperty(value = "审核计划")
    private Set<AuditPlan> plans;
}
