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

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.zhengjie.base.BaseDTO;
import me.zhengjie.domain.ChangeApprove;
import me.zhengjie.domain.ChangeManagement;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tong Minjie
 * @date 2022-01-17
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChangeDto extends BaseDTO implements Serializable {


    private Long id;

    private Integer finishedStep;

    private String changeNum;

    private String reason;

    private String source;

    private String initiator;

    private String department;

    private Timestamp initTime;

    private String area;

    private String[] areaTags;

    private String depart;

    private String[] departTags;

    private String project;

    private String[] projectTags;

    private String production;

    private String[] prodTags;

    private String cost;

    // -----分类-----
    // 人机料法环等因素

    private Boolean isCustomer;

    private String remark;

    private String email;

    // -----分析-----
    // 变更范围: 文档，过程，工具和其他

    private String scope;

    private List<String> scopeTags = new ArrayList<>();

    private Boolean havAgreement;

    // -----确认/创建-----
    private String approveDepart;

    private String approveBy;

    private Boolean havReport;

    private Boolean isAccepted;

    private String status;

    private ChangeManagement management = new ChangeManagement();

    private ChangeApprove approve = new ChangeApprove();

    private List<Integer> unlockSteps = new ArrayList<>();

}