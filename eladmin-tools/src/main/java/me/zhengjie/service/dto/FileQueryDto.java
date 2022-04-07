package me.zhengjie.service.dto;

import lombok.Data;
import me.zhengjie.annotation.Query;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/6/1 9:52
 */
@Data
public class FileQueryDto {

    @Query
    private Long storageId;

    /**
     * 文件状态
     */
    @Query
    private String fileStatus;

    /**
     * 审批状态
     */
    @Query
    private String approvalStatus;

    @Query
    private Long isDel = 0L;

}
