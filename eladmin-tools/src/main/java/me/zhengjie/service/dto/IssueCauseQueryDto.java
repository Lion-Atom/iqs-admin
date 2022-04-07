package me.zhengjie.service.dto;

import lombok.Data;
import me.zhengjie.annotation.Query;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/6/1 9:52
 */
@Data
public class IssueCauseQueryDto {

    /**
     * 记录目标标识
     */
    @Query
    private Long issueId;

    @Query
    private Boolean isExact;

}
