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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.zhengjie.base.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author TongMinjie
 * @date 2022-05-07
 */
@Getter
@Setter
@Entity
@Table(name = "train_participant")
@NoArgsConstructor
public class TrainParticipant extends BaseEntity implements Serializable {

    @Id
    @Column(name = "train_participant_id")
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "train_schedule_id")
    @ApiModelProperty(value = "培训日程安排ID")
    private Long trScheduleId;

    @NotBlank
    @Column(name = "participant_depart")
    @ApiModelProperty(value = "所属部门")
    private String participantDepart;

    @NotBlank
    @Column(name = "participant_name")
    @ApiModelProperty(value = "参与者姓名")
    private String participantName;

    @NotNull
    @Column(name = "is_valid")
    @ApiModelProperty(value = "是否有效")
    private Boolean isValid;

}