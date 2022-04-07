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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author Zheng Jie
 * @date 2018-11-24
 */
@Data
public class ToolsLogDto implements Serializable {

    private Long id;

    /**
     * 关联标识，desc:目标对象
     */
    private Long bindingId;

    /**
     * 日志类型：文件、分类等，便于后续拓展
     */
    private String logType;

    /** 操作用户 */
    private String username;

    /** 描述 */
    private String description;

    /** 描述明细 */
    private String descriptionDetail;

    /** 创建日期 */
    private Timestamp createTime;

}
