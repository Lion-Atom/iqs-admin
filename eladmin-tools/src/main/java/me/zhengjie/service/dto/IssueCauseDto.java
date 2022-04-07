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

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import me.zhengjie.base.BaseDTO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
* @author TongMinjie
* @date 2021-07-28
*/
@Getter
@Setter
public class IssueCauseDto extends BaseDTO implements Serializable {

    private Long id;

    private Long issueId;

    private String name;

    private String judgeResult;

    private String method;

    private String result;

    private Double contribution;

    private String comment;

    private Boolean isExact = false;

    private Long pid;

    private Integer subCount;

//    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<IssueCauseDto> children = new ArrayList<>();

    public Boolean getHasChildren() {
        return subCount > 0;
    }

    public Boolean getLeaf() {
        return subCount <= 0;
    }

    public String getLabel() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IssueCauseDto fileLevelDto = (IssueCauseDto) o;
        return Objects.equals(id, fileLevelDto.id) &&
                Objects.equals(name, fileLevelDto.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}