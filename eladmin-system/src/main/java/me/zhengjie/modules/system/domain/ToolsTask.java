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
package me.zhengjie.modules.system.domain;

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
 * @author Zheng Jie
 * @date 2019-09-05
 */
@Getter
@Setter
@Entity
@Table(name = "tool_pre_trail")
@NoArgsConstructor
public class ToolsTask extends BaseEntity implements Serializable {

    @Id
    @Column(name = "pre_trail_id")
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty(value = "任务编号")
    private String preTrailNo;

    @NotNull
    @ApiModelProperty(value = "文件标识")
    private Long storageId;

    @ApiModelProperty(value = "真实文件名")
    private String realName;

    @ApiModelProperty(value = "后缀")
    private String suffix;

    @ApiModelProperty(value = "原路径")
    private String srcPath;

    @ApiModelProperty(value = "目标路径")
    private String tarPath;

    @ApiModelProperty(value = "文件类型")
    private String fileType;

    @ApiModelProperty(value = "类型")
    private String type;

    @ApiModelProperty(value = "大小")
    private String size;

    @NotBlank
    @ApiModelProperty(value = "版本号")
    private String version;

    @ApiModelProperty(value = "变更描述")
    private String changeDesc;

    @ApiModelProperty(value = "变更类型")
    private String changeType;

    @ApiModelProperty(value = "绑定类型")
    private Boolean bindingType;

    @ApiModelProperty(value = "绑定的目标标识")
    private Long bindingId;

    @ApiModelProperty(value = "是否已删除",hidden = true)
    private Long isDel=0L;

    @NotNull
    @ApiModelProperty(value = "审批者")
    @Column(name = "approved_by")
    private Long approvedBy;

    @ApiModelProperty(value = "是否已完成")
    private Boolean isDone;

    @ApiModelProperty(value = "审批结论")
    private Boolean approveResult;

    @ApiModelProperty(value = "建议")
    private String comment;

    public void copy(ToolsTask source) {
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
    }
}