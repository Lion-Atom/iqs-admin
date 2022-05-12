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
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author TongMinjie
 * @date 2022-04-11
 */
@Getter
@Setter
@Entity
@Table(name = "train_exam_staff_file")
@NoArgsConstructor
public class TrExamStaffFile extends BaseEntity implements Serializable {

    @Id
    @Column(name = "tr_exam_staff_file_id")
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "train_exam_staff_id")
    @ApiModelProperty(value = "培训考试员工ID")
    private Long trExamStaffId;

    @ApiModelProperty(value = "文件类型")
    private String fileType;

    @ApiModelProperty(value = "真实文件名")
    private String realName;

    @ApiModelProperty(value = "文件名")
    private String name;

    @ApiModelProperty(value = "后缀")
    private String suffix;

    @ApiModelProperty(value = "路径")
    private String path;

    @ApiModelProperty(value = "类型")
    private String type;

    @ApiModelProperty(value = "大小")
    private String size;

    public TrExamStaffFile(Long trExamStaffId, String fileType, String realName, String name, String suffix, String path, String type, String size) {
        this.trExamStaffId = trExamStaffId;
        this.fileType = fileType;
        this.realName = realName;
        this.name = name;
        this.suffix = suffix;
        this.path = path;
        this.type = type;
        this.size = size;
    }

    public void copy(TrExamStaffFile source) {
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
    }
}