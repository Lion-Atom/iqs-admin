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

/**
 * @author Tong Minjie
 * @date 2021-07-30
 */

@Getter
@Setter
public class IssueActionDto extends BaseDTO implements Serializable {

    private Long id;

    private Long issueId;

    private String issueTitle;

    private String hasReport;

    private String name;

    private String status;

    private String description;

    private Long responsibleId;

    private String responsibleName;

    private Double efficiency;

    private String partIdentification;

    private Timestamp plannedTime;

    private Timestamp completeTime;

    private String comment;

    private Boolean isCon;

    private String type;

    private Integer systemNum;

    private Long causeId;

    private String validationMethod;

    private String validationResult;

    private Timestamp plannedCompleteTime;

    private String identification;

    private String correctiveMeasurementMethod;

    private String correctiveEfficiencyResult;

    private Timestamp evaluationTime;

    private String conclusion;

    private Timestamp removeTime;

    /**
     * D4-根本原因名称
     */
    private String causeName;

    /**
     * D4-根本原因发生/检测
     */
    private String judgeResult;

    /**
     * 阻断D3和D7的关联
     */
    private String stepName;

    /**
     * 分析ID
     */
    private Long analysisId;

}
