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
import lombok.*;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import me.zhengjie.base.BaseEntity;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Zheng Jie
 * @date 2019-09-05
 */
@Getter
@Setter
@Entity
@Table(name = "tool_local_storage")
@NoArgsConstructor
public class LocalStorage extends BaseEntity implements Serializable {

    @Id
    @Column(name = "storage_id")
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @ApiModelProperty(value = "版本号")
    private String version;

    @ApiModelProperty(value = "是否变版")
    private Boolean isRevision;

    @ApiModelProperty(value = "文件状态")
    private String fileStatus;

    @ApiModelProperty(value = "审批状态")
    private String approvalStatus;

    @ApiModelProperty(value = "文件类型")
    private String fileType;

    @ApiModelProperty(value = "文件保密等级")
    private String securityLevel;

    @Column(name = "expiration_time")
    @ApiModelProperty(value = "过期时间")
    private Timestamp expirationTime;

    @ApiModelProperty(value = "变更描述")
    private String changeDesc;

    @ApiModelProperty(value = "是否已删除")
    private Long isDel = 0L;

    @ApiModelProperty(value = "文件描述")
    private String fileDesc;

    @OneToOne
    @JoinColumn(name = "file_level_id")
    @ApiModelProperty(value = "文件等级")
    private FileLevel fileLevel;

    @OneToOne
    @JoinColumn(name = "file_category_id")
    @NotFound(action = NotFoundAction.IGNORE)
    @ApiModelProperty(value = "文件分类")
    private FileCategory fileCategory;

    @OneToOne
    @JoinColumn(name = "dept_id")
    @ApiModelProperty(value = "文件所在部门")
    private FileDept fileDept;

    @JoinColumn(name = "storage_id")
    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.REMOVE})
    private Set<BindingDept>  bindDepts;

    @JoinColumn(name = "storage_id")
    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.REMOVE})
    private List<BindingLocalStorage> bindFiles;

    @OneToOne
    @JoinColumn(name = "storage_temp_id")
    @NotFound(action = NotFoundAction.IGNORE)
    @ApiModelProperty(value = "文件等级")
    private LocalStorageTemp localStorageTemp;

    public LocalStorage(String realName, String name, String suffix, String path, String type,
                        String size, String version, Boolean isRevision, String fileStatus,
                        String approvalStatus, String fileType, String securityLevel, Timestamp expirationTime,
                        String changeDesc, String fileDesc, FileLevel fileLevel, FileCategory fileCategory,
                        List<BindingLocalStorage> bindFiles, Set<BindingDept> bindDepts, FileDept fileDept) {
        this.realName = realName;
        this.name = name;
        this.suffix = suffix;
        this.path = path;
        this.type = type;
        this.size = size;
        this.version = version;
        this.isRevision = isRevision;
        this.fileStatus = fileStatus;
        this.approvalStatus = approvalStatus;
        this.fileType = fileType;
        this.securityLevel = securityLevel;
        this.expirationTime = expirationTime;
        this.changeDesc = changeDesc;
        this.fileDesc = fileDesc;
        this.fileLevel = fileLevel;
        this.fileCategory = fileCategory;
        this.bindFiles = bindFiles;
        this.bindDepts = bindDepts;
        this.fileDept = fileDept;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LocalStorage storage = (LocalStorage) o;
        return Objects.equals(id, storage.id) &&
                Objects.equals(name, storage.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    public void copy(LocalStorage source) {
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
    }
}