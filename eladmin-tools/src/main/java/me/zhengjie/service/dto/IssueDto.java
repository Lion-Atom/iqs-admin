package me.zhengjie.service.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import me.zhengjie.base.BaseDTO;
import me.zhengjie.base.CommonDTO;
import me.zhengjie.domain.TimeManagement;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/21 18:04
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class IssueDto extends BaseDTO implements Serializable {

    private Long id;

    private String issueTitle;

    private String customerName;

    private String email;

    private String phone;

    private String caNum;

    private String partNum;

    private String source;

    private String urgencyPlan;

    private Timestamp initTime;

    private String description;

    private String type;

    private Boolean isRepeat;

    private Boolean hasSimilar;

    private String initRisk;

    private String department;

    private Timestamp customerTime;

    private String other;

    /**
     * 8的执行选择
     */
    private String hasReport;

    private Boolean hasScore;

    private String supplierDescription;

    private String riskAssessment;

    private String rbi;

    private String specialEvent;

    private String status;

    private Timestamp cleanTime;

    private Timestamp closeTime;

    private Integer score;

    private String commentD5;

    private String commentD6;

    private String commentD7;

    private Boolean hasTempFile;

    private Boolean recoverTempFile;

    private String tempFileComment;

    private String duration;

    private String encodeNum;

    private Long leaderId;

    /**
     * 拒绝理由
     */
    private String reason;

    private TimeManagement timeManagement;

    private List<CommonDTO> commonDTOList;
}
