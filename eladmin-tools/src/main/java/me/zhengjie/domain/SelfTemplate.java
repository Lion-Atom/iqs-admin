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
@Table(name = "self_template")
@NoArgsConstructor
public class SelfTemplate extends BaseEntity implements Serializable {

    @Id
    @Column(name = "self_template_id")
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ApiModelProperty(value = "模板ID")
    private Long templateId;

    @ApiModelProperty(value = "项目名称")
    private String itemName;

    @ApiModelProperty(value = "项目内容")
    private String itemContent;

    @ApiModelProperty(value = "分数")
    private Double itemScore;

    @ApiModelProperty(value = "父项目ID")
    private Long pid;

    public void copy(SelfTemplate source) {
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
    }

}
