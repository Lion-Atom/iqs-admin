package me.zhengjie.service.dto;

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
public class PreTrailDto extends BaseDTO implements Serializable {

    private Long id;

    private String preTrailNo;

    private Long storageId;

    /**
     * 文件名称
     */
    private String storageName;

    private String realName;

    private String suffix;

    private String srcPath;

    private String tarPath;

    private String type;

    private String size;

    private String version;

    private String changeType;

    private Boolean bindingType;

    private String changeDesc;

    private Long isDel = 0L;

    private Long approvedBy;

    /**
     * 审批者名称
     */
    private String approver;

    private Boolean isDone;

    private Boolean approveResult;

    private String comment;
}
