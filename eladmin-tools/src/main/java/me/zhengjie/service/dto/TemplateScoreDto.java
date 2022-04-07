package me.zhengjie.service.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.zhengjie.base.BaseDTO;
import me.zhengjie.base.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/9/28 13:35
 */
@Getter
@Setter
public class TemplateScoreDto extends BaseDTO implements Serializable {

    private Long id;

    private Long templateId;

    private String itemName;

    private String itemType;

    private String content;

    private Boolean isSpecial = false;

    private Double score;

    private Long pid;

    private String pItemName;

    private String remark;

    private Boolean isNeed;

    private Boolean isActive;

}
