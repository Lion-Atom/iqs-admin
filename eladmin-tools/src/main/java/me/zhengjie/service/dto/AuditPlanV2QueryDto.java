package me.zhengjie.service.dto;

import lombok.Data;
import me.zhengjie.annotation.Query;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/6/1 9:52
 */
@Data
public class AuditPlanV2QueryDto {

    @Query
    private Long id;

    /**
     * 计划状态
     */
    @Query(propName = "status", type = Query.Type.IN)
    private Set<String> statusList = new HashSet<>();

    /**
     * 审批状态
     */
    @Query
    private String approvalStatus;

}
