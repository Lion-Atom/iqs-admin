package me.zhengjie.domain;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
@Entity
@Table(name = "template_score")
@NoArgsConstructor
public class TemplateScore extends BaseEntity implements Serializable {

    @Id
    @Column(name = "template_score_id")
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ApiModelProperty(value = "模板ID")
    private Long templateId;

    @ApiModelProperty(value = "项目名称")
    private String itemName;

    @ApiModelProperty(value = "项目类型")
    private String itemType;

    @ApiModelProperty(value = "项目内容")
    private String content;

    @ApiModelProperty(value = "特殊类型")
    private Boolean isSpecial = false;

    @ApiModelProperty(value = "分数")
    private Double score;

    @ApiModelProperty(value = "父项目ID")
    private Long pid;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "是否需要打分")
    private Boolean isNeed = true;

    @ApiModelProperty(value = "是否激活")
    private Boolean isActive = false;

    public void copy(TemplateScore source) {
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
    }

}
