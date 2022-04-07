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
public class ApprovalProcessDto extends BaseDTO implements Serializable {

    private Long id;

    private Long bindingId;

    private String processNo;

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

    private String approvedResult;

    private String approvedComment;

    private Boolean isDone;

    private String duration;

}
