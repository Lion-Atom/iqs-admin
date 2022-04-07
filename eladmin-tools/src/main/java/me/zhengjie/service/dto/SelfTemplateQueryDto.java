package me.zhengjie.service.dto;

import lombok.Data;
import me.zhengjie.annotation.Query;

import javax.validation.constraints.NotNull;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/6/1 9:52
 */
@Data
public class SelfTemplateQueryDto {

    @NotNull
    private Long id;

    @NotNull
    private Long templateId;
}
