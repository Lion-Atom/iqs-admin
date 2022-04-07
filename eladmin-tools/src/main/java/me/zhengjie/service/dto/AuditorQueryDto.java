package me.zhengjie.service.dto;

import lombok.Data;
import me.zhengjie.annotation.Query;

import javax.validation.constraints.NotBlank;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/28 9:26
 */
@Data
public class AuditorQueryDto {

    @Query
    private String status;

    @Query
    @NotBlank
    private String approvalStatus;
}
