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
import me.zhengjie.domain.BindingDept;
import me.zhengjie.domain.BindingLocalStorage;
import me.zhengjie.domain.FileDept;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author TongMinjie
 * @date 2021-04-28
 */
@Getter
@Setter
public class LocalStorageDto extends BaseDTO implements Serializable {

    private Long id;

    private String realName;

    private String name;

    private String suffix;

    private String type;

    private String size;

    private String version;

    private Boolean isRevision;

    private Long fileLevelId;

    private Long fileCategoryId;

    private Long deptId;

    private String fileStatus;

    private String approvalStatus;

    private String securityLevel;

    private String fileType;

    private Timestamp expirationTime;

    private String changeDesc;

    private String fileDesc;

    private FileLevelDto fileLevel;

    private FileCategoryDto fileCategory;

    private FileDept fileDept;

    private Set<BindingDept> bindDepts;

    private List<String> bindDeptStr = new ArrayList<>();

    private List<BindingLocalStorageDto> bindFiles;

    private LocalStorageTempDto localStorageTemp;

    private Boolean hasDownloadAuthority = true;
}