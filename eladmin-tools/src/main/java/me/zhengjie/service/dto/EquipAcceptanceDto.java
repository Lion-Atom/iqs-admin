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
 * @date 2022-03-29
 */
@Getter
@Setter
public class EquipAcceptanceDto extends BaseDTO implements Serializable {

    private Long id;

    private Long equipmentId;

    private String equipName;

    private String acceptStatus;

    private String acceptParticipant;

    private String[] participantTags;

    private Long acceptDepart;

    private String acceptDepartName;

    private String acceptBy;

    private String submitBy;

    private Timestamp submitTime;

    private Long approveDepart;

    private String approveDepartName;

    private String approveBy;

    private Timestamp approveTime;

    private String approveResult;

    private String refuseReason;
}