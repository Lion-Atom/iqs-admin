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
 * @date 2022-03-10
 */
@Getter
@Setter
@Entity
@Table(name = "calibration_file")
@NoArgsConstructor
public class CalibrationFile extends BaseEntity implements Serializable {

    @Id
    @Column(name = "calibration_file_id")
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ApiModelProperty(value = "仪器校验ID")
    private Long calibrationId;

    @ApiModelProperty(value = "文件所属")
    private String fileType;

    @NotNull
    @ApiModelProperty(value = "是否是最新校准报告")
    private Boolean isLatest;

    @NotBlank
    @ApiModelProperty(value = "校准结果是否合格")
    private String caliResult;

    @ApiModelProperty(value = "不合格原因描述")
    private String failDesc;

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

    public CalibrationFile(Long calibrationId, String fileType, Boolean isLatest, String caliResult, String failDesc, String realName,
                           String name, String suffix, String path, String type, String size) {
        this.calibrationId = calibrationId;
        this.fileType = fileType;
        this.isLatest = isLatest;
        this.caliResult = caliResult;
        this.failDesc = failDesc;
        this.realName = realName;
        this.name = name;
        this.suffix = suffix;
        this.path = path;
        this.type = type;
        this.size = size;
    }

    public void copy(CalibrationFile source) {
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
    }
}