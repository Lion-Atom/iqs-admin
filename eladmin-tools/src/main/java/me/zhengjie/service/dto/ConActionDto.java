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
public class ConActionDto extends BaseDTO implements Serializable {

    private Long id;

    private Long issueId;

    private String title;

    private String qtyOk;

    private String qtyNo;

    private Long actionId;

    private String actionName;

    private Long responsibleId;

    private String responsibleName;

    private Double efficiency;

    private String partIdentification;

    private Timestamp plannedTime;

    private Timestamp actualTime;

}
