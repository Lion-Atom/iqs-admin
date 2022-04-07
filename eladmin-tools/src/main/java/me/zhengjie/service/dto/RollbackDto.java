package me.zhengjie.service.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/6/8 10:35
 */
@Data
public class RollbackDto {

    @NotNull
    private Long storageId;

    /**
     * 开始编辑的时间
     */
    @NotNull
    private Timestamp lastModifiedDate;

    /**
     * 编辑前的审批状态
     */
    @NotBlank
    private String approvalStatus;

    /**
     * 编辑前的文件状态
     */
    @NotBlank
    private String fileStatus;
}
