package me.zhengjie.service.dto;

import lombok.Data;
import me.zhengjie.annotation.Query;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/6/1 9:52
 */
@Data
public class IssueActionQueryDto {

    /**
     * 记录目标标识
     */
    @Query
    private Long issueId;

    @Query
    private Boolean isCon;

    @Query
    private String type;

    @Query
    private String status;

    private Boolean isImCorrectAct = false;

}
