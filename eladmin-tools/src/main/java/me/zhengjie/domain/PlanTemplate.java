package me.zhengjie.domain;

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
@Table(name = "plan_template")
@NoArgsConstructor
public class PlanTemplate extends BaseEntity implements Serializable {

    @Id
    @Column(name = "template_id")
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ApiModelProperty(value = "审核计划ID")
    private Long planId;

    @ApiModelProperty(value = "模板名称")
    private String name;

    @ApiModelProperty(value = "使用模板类型")
    private String templateType;

    @ApiModelProperty(value = "启用状态")
    private Boolean enabled;

}
