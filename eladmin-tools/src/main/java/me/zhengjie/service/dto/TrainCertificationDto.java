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
import me.zhengjie.domain.TrCertificationFile;
import me.zhengjie.domain.TrNewStaffFile;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tong Minjie
 * @date 2022-05-07
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TrainCertificationDto extends BaseDTO implements Serializable {

    private Long id;

    private String staffName;

    private Timestamp hireDate;

    private Long departId;

    private String departName;

    private String superior;

    private String jobNum;

    private String jobName;

    private String certificationType;

    private Long trScheduleId;

    private String trainTitle;

    private String scheduleStatus;

    private String jobType;

    private String orgName;

    private Timestamp dueDate;

    private Boolean isRemind;

    private Integer remindDays;

    private Timestamp trainDate;

    private String trainContent;

    private Timestamp issueDate;

    private String trainResult;

    private String certificationStatus;

    private List<TrCertificationFile> fileList = new ArrayList<>();
}