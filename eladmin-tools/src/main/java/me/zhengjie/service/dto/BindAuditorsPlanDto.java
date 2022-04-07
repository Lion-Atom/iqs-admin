package me.zhengjie.service.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/9/18 13:47
 */
@Getter
@Setter
public class BindAuditorsPlanDto {
    private Long id;

    private Long planId;

    private Long auditorId;

    private String username;
}
