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

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import me.zhengjie.base.BaseDTO;
import me.zhengjie.domain.FileDept;
import me.zhengjie.domain.ToolsJob;
import me.zhengjie.domain.ToolsUser;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;

/**
 * @author Zheng Jie
 * @date 2018-11-23
 */
@Getter
@Setter
public class ToolsUserDto extends BaseDTO implements Serializable {

    private Long id;

    private Set<ToolsJob> jobs;

    private String jobName;

    private FileDept dept;

    private Long deptId;

    private String deptName;

    private String username;

    private String nickName;

    private String staffType;

    private String jobType;

    private Timestamp hireDate;

    private String workshop;

    private String team;

    private String jobNum;

    private String email;

    private String phone;

    private String gender;

    private String avatarName;

    private String avatarPath;

    @JSONField(serialize = false)
    private String password;

    private Boolean enabled;

    //@JSONField(serialize = false)
    //放开限制：使得可以用于判断是否是管理员
    private Boolean isAdmin = false;

    private Date pwdResetTime;

    private Boolean isDepartMaster;

    private Long superiorId;

    private String superiorName;
}
