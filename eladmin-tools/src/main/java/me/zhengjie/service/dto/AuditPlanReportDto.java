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

import lombok.Getter;
import lombok.Setter;
import me.zhengjie.base.BaseDTO;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author TongMinjie
 * @date 2019-09-09
 */
@Getter
@Setter
public class AuditPlanReportDto extends BaseDTO implements Serializable {

    private Long id;

    private Long planId;

    // 审核计划
    private String planName;

    private String product;

    private String reason;

    private String scope;

    // 模板内容信息
    private Timestamp auditTime;

    private String address;

    // 报告信息
    private String result;

    private Double score;

    private String trailRequirement;

    private Timestamp reportDeadline;

    private Timestamp finalDeadline;

}