package me.zhengjie.service.dto;

import lombok.Data;
import me.zhengjie.annotation.Query;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/6/1 9:52
 */
@Data
public class AuditPlanQueryDto {

    @Query
    private Long id;

    /**
     * 计划状态
     */
    @Query
    private String status;

    /**
     * 审批状态
     */
    @Query
    private String approvalStatus;

}
