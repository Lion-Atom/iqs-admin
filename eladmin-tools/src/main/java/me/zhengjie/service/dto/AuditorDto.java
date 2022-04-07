package me.zhengjie.service.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.zhengjie.base.BaseDTO;
import me.zhengjie.domain.AuditorFile;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/23 14:50
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class AuditorDto extends BaseDTO implements Serializable {

    private Long id;

    private Long teamId;

    private Long issueId;

    private Long userId;

    private String companyName;

    private String deptName;

    private String username;

    private String system;

    private String status;

    private Timestamp certificationTime;

    private Integer validity;

    private Timestamp NextCertificationTime;

    private String certificationUnit;

    private String approvalStatus;

    private String rejectComment;

    private Long approvedBy;

    /**
     * 批准人姓名
     */
    private String approver;

    private Timestamp approvedTime;

    private String styleType;

    private List<AuditorFile> auditorFiles = new ArrayList<>();
}
