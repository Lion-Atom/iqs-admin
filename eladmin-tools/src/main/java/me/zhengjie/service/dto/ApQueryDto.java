package me.zhengjie.service.dto;

import lombok.Data;
import me.zhengjie.annotation.Query;

import javax.validation.constraints.NotBlank;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/6/1 9:52
 */
@Data
public class ApQueryDto {

    @NotBlank
    private String yearType;

    @NotBlank
    private String monthType;

}
