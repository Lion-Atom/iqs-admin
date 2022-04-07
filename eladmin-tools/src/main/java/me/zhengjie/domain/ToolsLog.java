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

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
/**
 *
 * @author Tong Minjie
 * @date 2021-06-23
 */
@Entity
@Getter
@Setter
@Table(name = "tools_log")
public class ToolsLog implements Serializable {

    @Id
    @Column(name = "tools_log_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "ID", hidden = true)
    private Long id;

    @ApiModelProperty(value = "绑定的（文件等）标识")
    private Long bindingId;

    /**
     * 日志类型：文件、分类等，便于后续拓展
     */
    @ApiModelProperty(value = "日志类型", hidden = true)
    private String logType;

    /** 操作用户 */
    @ApiModelProperty(value = "操作人名称", hidden = true)
    private String username;

    /** 描述 */
    @ApiModelProperty(value = "描述", hidden = true)
    private String description;

    /** 描述明细 */
    @ApiModelProperty(value = "操作描述明细", hidden = true)
    private String descriptionDetail;

    /** 创建日期 */
    @CreationTimestamp
    @ApiModelProperty(value = "创建时间", hidden = true)
    private Timestamp createTime;

}
