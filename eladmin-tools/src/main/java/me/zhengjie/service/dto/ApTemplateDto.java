package me.zhengjie.service.dto;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.zhengjie.base.BaseDTO;
import me.zhengjie.base.BaseEntity;
import me.zhengjie.domain.ApTemplateContent;

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
public class ApTemplateDto extends BaseDTO implements Serializable {

    private Long id;

    private Long planId;

    private String name;

    private String templateType;

    private Boolean enabled;

    // 模板内容
    private ApTemplateContent content;

    public void copy(ApTemplateDto source) {
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
    }

}
