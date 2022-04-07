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
 * @date 2021/11/17 13:35
 */

@Getter
@Setter
@Entity
@Table(name = "template_certificate")
@NoArgsConstructor
public class ApTemplateCertificate extends BaseEntity implements Serializable {

    @Id
    @Column(name = "template_certificate_id")
    @NotNull(groups = Update.class)
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty(value = "模板ID" )
    private Long templateId;

    @ApiModelProperty(value = "认证体系名称")
    private String name;

    @ApiModelProperty(value = "认证机构")
    private String sgs;

    @ApiModelProperty(value = "认证编号")
    private String cerNum;

    @ApiModelProperty(value = "有效期限")
    private Timestamp validDate;

    @ApiModelProperty(value = "是否已过期")
    private Boolean isOverdue = false;

    public void copy(ApTemplateCertificate source) {
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
    }

}
