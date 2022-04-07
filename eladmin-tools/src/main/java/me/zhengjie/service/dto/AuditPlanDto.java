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

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import me.zhengjie.base.BaseDTO;
import me.zhengjie.domain.AuditPlanReport;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author TongMinjie
 * @date 2021-09-06
 */
@Getter
@Setter
public class AuditPlanDto extends BaseDTO implements Serializable {

    private Long id;

    private String status;

    private String auditNo;

    private String type;

    private String content;

    private String name;

    private String systemName;

    private String realName;

    private Timestamp planTime;

    private String templateType;

    private Long templateId;

    private Long templateName;

    private String scope;

    private String period;

    private String reason;

    private String product;

    private String technology;

    private String address;

    private String line;

    private Long chargeBy;

    private String chargeman;

    private String description;

    private Long approvedBy;

    private String approver;

    private String approvalStatus;

    private String rejectComment;

    private Timestamp approvedTime;

    private String changeDesc;

    private Timestamp changeApprovedTime;

    private Timestamp finalDeadline;

    private Timestamp closeTime;

    private Boolean isOverdue;

    private AuditPlanReport report;

    private Set<AuditorDto> auditors;

}