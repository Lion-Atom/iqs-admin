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
import java.sql.Timestamp;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/9/28 13:35
 */
@Getter
@Setter
@Entity
@Table(name = "template_content")
@NoArgsConstructor
public class ApTemplateContent extends BaseEntity implements Serializable {

    @Id
    @Column(name = "template_id")
    @ApiModelProperty(value = "ID", hidden = true)
    private Long id;

    @ApiModelProperty(value = "模板内容名称")
    private String name;

    @ApiModelProperty(value = "审核日期")
    private Timestamp auditTime;

    @ApiModelProperty(value = "DUNS编码")
    private String dunsNum;

    @ApiModelProperty(value = "详细地址")
    private String address;

    @ApiModelProperty(value = "被审核组织负责人用户名")
    private String chargeName;

    @ApiModelProperty(value = "被审核组织负责人手机号码")
    private String chargePhone;

    @ApiModelProperty(value = "被审核组织负责人邮箱")
    private String chargeEmail;

    @ApiModelProperty(value = "被审核组织质量负责人用户名")
    private String qualityName;

    @ApiModelProperty(value = "被审核组织质量负责人手机号码")
    private String qualityPhone;

    @ApiModelProperty(value = "被审核组织质量负责人邮箱")
    private String qualityEmail;

    @ApiModelProperty(value = "被审核组织销售负责人用户名")
    private String salesName;

    @ApiModelProperty(value = "被审核组织销售负责人手机号码")
    private String salesPhone;

    @ApiModelProperty(value = "被审核组织生产负责人邮箱")
    private String salesEmail;

    @ApiModelProperty(value = "被审核组织生产负责人用户名")
    private String productName;

    @ApiModelProperty(value = "被审核组织生产负责人手机号码")
    private String productPhone;

    @ApiModelProperty(value = "被审核组织生产负责人邮箱")
    private String productEmail;

    @ApiModelProperty(value = "被审核组织研发负责人用户名")
    private String techName;

    @ApiModelProperty(value = "被审核组织研发负责人手机号码")
    private String techPhone;

    @ApiModelProperty(value = "被审核组织研发负责人邮箱")
    private String techEmail;

    @ApiModelProperty(value = "产品类别")
    private String productType;

    @ApiModelProperty(value = "执行过程")
    private String processType;

    @ApiModelProperty(value = "其他")
    private String other;

    public void copy(ApTemplateContent source) {
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
    }

}
