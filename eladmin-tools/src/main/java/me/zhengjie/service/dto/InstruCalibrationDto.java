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
import me.zhengjie.domain.InstruCaliFileV2;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @author TongMinjie
 * @date 2022-05-30
 */
@Getter
@Setter
public class InstruCalibrationDto extends BaseDTO implements Serializable {

    private Long id;

    private Long instruId;

    private String instruName;

    private String innerId;

    private Timestamp caliDate;

    private Boolean innerChecked;

    private Long caliOrgId;

    private String caliOrgName;

    private Boolean isDoor;

    private String caliResult;

    private String failDesc;

    private List<InstruCaliFileV2> fileList = new ArrayList<>();
}