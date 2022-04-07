package me.zhengjie.service.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/6/1 9:52
 */
@Data
public class ToolsLogDelCond {

    /**
     * 记录目标标识
     */
    @NotNull
    private Long bindingId;
}
