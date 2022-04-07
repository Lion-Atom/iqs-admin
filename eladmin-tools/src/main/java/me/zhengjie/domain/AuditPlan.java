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
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2021-09-06
 */
@Entity
@Getter
@Setter
@Table(name = "audit_plan")
public class AuditPlan extends BaseEntity implements Serializable {

    @Id
    @Column(name = "plan_id")
    @NotNull(groups = Update.class)
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty(value = "审核状态")
    private String status;

    @ApiModelProperty(value = "审核编号 /n 自动生成")
    private String auditNo;

    @NotBlank
    @ApiModelProperty(value = "审核计划种类")
    private String type;

    @NotBlank
    @ApiModelProperty(value = "审核内容")
    private String content;

/*    @NotBlank
    @ApiModelProperty(value = "审核计划名称")
    private String name;*/

    @ApiModelProperty(value = "系统名称")
    private String systemName;

    @ApiModelProperty(value = "审核计划名称")
    private String realName;

    @ApiModelProperty(value = "审核计划时间")
    private Timestamp planTime;

    @NotBlank
    @ApiModelProperty(value = "模板类型")
    private String templateType;

    @ApiModelProperty(value = "模板ID")
    private Long templateId;

    @NotBlank
    @ApiModelProperty(value = "审核范围")
    private String scope;

    @NotBlank
    @ApiModelProperty(value = "审核周期")
    private String period;

    @NotBlank
    @ApiModelProperty(value = "审核原因")
    private String reason;

    @NotBlank
    @ApiModelProperty(value = "审核产品")
    private String product;

    @NotBlank
    @ApiModelProperty(value = "产品技术")
    private String technology;

    @NotBlank
    @ApiModelProperty(value = "审核地点")
    private String address;

    @NotBlank
    @ApiModelProperty(value = "审核产线")
    private String line;

    @NotNull
    @ApiModelProperty(value = "审核负责人ID")
    private Long chargeBy;

    @ApiModelProperty(value = "计划描述")
    private String description;

    @ApiModelProperty(value = "审核计划批准人")
    private Long approvedBy;

    @ApiModelProperty(value = "审核计划批准时间")
    private Timestamp approvedTime;

    @ApiModelProperty(value = "审核计划审批状态")
    private String approvalStatus;

    @ApiModelProperty(value = "驳回建议")
    private String rejectComment;

    @ApiModelProperty(value = "审核计划修改原因描述")
    private String changeDesc;

    @ApiModelProperty(value = "审核计划修改批准时间")
    private Timestamp changeApprovedTime;

    @ApiModelProperty(value = "关闭时间")
    private Timestamp closeTime;

    @ApiModelProperty(value = "是否过期")
    private Boolean isOverdue;

    @ManyToMany
    @ApiModelProperty(value = "计划审核人员")
    @JoinTable(name = "tools_auditors_plan",
            joinColumns = {@JoinColumn(name = "plan_id", referencedColumnName = "plan_id")},
            inverseJoinColumns = {@JoinColumn(name = "auditor_id", referencedColumnName = "auditor_id")})
    private Set<Auditor> auditors;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuditPlan plan = (AuditPlan) o;
        return Objects.equals(id, plan.id) &&
                Objects.equals(realName, plan.realName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, realName);
    }
}