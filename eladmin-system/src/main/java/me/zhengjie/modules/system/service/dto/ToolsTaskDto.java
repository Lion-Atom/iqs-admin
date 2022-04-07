package me.zhengjie.modules.system.service.dto;

import lombok.Getter;
import lombok.Setter;
import me.zhengjie.base.BaseDTO;

import java.io.Serializable;


/**
 * @author tmj
 * @version 1.0
 * @date 2021/6/23 9:37
 */
@Getter
@Setter
public class ToolsTaskDto extends BaseDTO implements Serializable {

    private Long id;

    /**
     * 任务编号
     */
    private String preTrailNo;

    private Long storageId;

    /**
     * 目标的建立者
     */
    private String ownerName;

    /**
     * 文件名称
     */
    private String storageName;

    private String realName;

    private String suffix;

    private String srcPath;

    private String tarPath;

    private String fileType;

    private String type;

    private String size;

    private String version;

    private String changeDesc;

    private String changeType;

    private Boolean bindingType;

    private Long bindingId;

    private Long isDel = 0L;

    private Long approvedBy;

    /**
     * 审批者名称
     */
    private String approver;

    /**
     * 是否已完成
     */
    private Boolean isDone;

    private String comment;

    private Boolean approveResult;
}
