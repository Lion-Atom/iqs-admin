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
 * @author Tong Minjie
 * @date 2021-06-24
 */
@Getter
@Setter
@Entity
@Table(name = "sys_approval_process")
@NoArgsConstructor
public class FileApprovalProcess extends BaseEntity implements Serializable {

    @Id
    @Column(name = "approval_process_id")
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ApiModelProperty(value = "目标标识")
    private Long bindingId;

    @NotBlank
    @ApiModelProperty(value = "审批单号")
    private String processNo;

    @ApiModelProperty(value = "真实文件名")
    private String realName;

    @ApiModelProperty(value = "后缀")
    private String suffix;

    @ApiModelProperty(value = "原路径")
    private String srcPath;

    @ApiModelProperty(value = "目标路径")
    private String tarPath;

    @ApiModelProperty(value = "文件类型")
    private String type;

    @ApiModelProperty(value = "大小")
    private String size;

    @ApiModelProperty(value = "版本号")
    private String version;

    @ApiModelProperty(value = "变更类型")
    private String changeType;

    @ApiModelProperty(value = "绑定类型")
    private Boolean bindingType;

    @ApiModelProperty(value = "变更描述")
    private String changeDesc;

    @ApiModelProperty(value = "是否已删除",hidden = true)
    private Long isDel=0L;

    @NotNull
    @ApiModelProperty(value = "审批者")
    @Column(name = "approved_by")
    private Long approvedBy;

    @ApiModelProperty(value = "审批结果")
    private String approvedResult;

    @ApiModelProperty(value = "审批意见")
    private String approvedComment;

    @ApiModelProperty(value = "处理时长")
    private String duration;

    public void copy(FileApprovalProcess source) {
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
    }
}