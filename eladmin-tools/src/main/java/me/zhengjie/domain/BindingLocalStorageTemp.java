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
import lombok.Setter;
import me.zhengjie.base.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Zheng Jie
 * @date 2019-09-05
 */
@Getter
@Setter
@Entity
@Table(name = "tool_binding_local_storage_temp")
public class BindingLocalStorageTemp extends BaseEntity implements Serializable {

    @Id
    @Column(name = "binding_temp_id")
    @NotNull(groups = Update.class)
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty(value = "绑定的文件标识")
    @Column(name = "binding_storage_id")
    private Long bindingStorageId;


    @ApiModelProperty(value = "被绑定的文件标识")
    @Column(name = "storage_temp_id")
    private Long storageTempId;

   /* @JoinColumn(name = "storage_id")
    @ManyToOne(fetch=FetchType.EAGER)
    @ApiModelProperty(value = "存储文件", hidden = true)
    private LocalStorage localStorage;*/

}