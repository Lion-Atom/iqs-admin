package me.zhengjie.domain;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import me.zhengjie.base.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/21 18:02
 */
@Entity
@Data
@Table(name = "tool_issue")
public class Issue extends BaseEntity implements Serializable {

    @Id
    @Column(name = "issue_id")
    @NotNull(groups = Update.class)
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @ApiModelProperty(value = "问题标题")
    private String issueTitle;

    @NotBlank
    @ApiModelProperty(value = "客户名称")
    private String customerName;

    @Email
    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "电话号码")
    private String phone;

    @ApiModelProperty(value = "客户追踪码")
    private String caNum;

    @ApiModelProperty(value = "物料编码")
    private String partNum;

    @ApiModelProperty(value = "问题来源")
    private String source;

    @ApiModelProperty(value = "紧急计划")
    private String urgencyPlan;

    @ApiModelProperty(value = "初始时间")
    private Timestamp initTime;

    @ApiModelProperty(value = "问题描述")
    private String description;

    @ApiModelProperty(value = "问题类型")
    private String type;

    @ApiModelProperty(value = "是否是重复问题")
    private Boolean isRepeat;

    @ApiModelProperty(value = "是否存在类似问题")
    private Boolean hasSimilar;

    @ApiModelProperty(value = "初始风险评估")
    private String initRisk;

    @ApiModelProperty(value = "关联部门")
    // todo 后续考虑从部门中获取
    private String department;

    @ApiModelProperty(value = "客户要求时间")
    private Timestamp customerTime;

    @ApiModelProperty(value = "其他补充")
    private String other;

    @ApiModelProperty(value = "8D执行选择")
    private String hasReport;

    @ApiModelProperty(value = "是否评分")
    private Boolean hasScore;

    @ApiModelProperty(value = "D2-供应商问题描述")
    private String supplierDescription;

    @ApiModelProperty(value = "D3-风险评估")
    private String riskAssessment;

    @ApiModelProperty(value = "D4-风险评估")
    private String rbi;

    @ApiModelProperty(value = "D4-特殊事件")
    private String specialEvent;

    @ApiModelProperty(value = "8D状态")
    private String status;

    @ApiModelProperty(value = "清空时间")
    private Timestamp cleanTime;

    @ApiModelProperty(value = "关闭时间")
    private Timestamp closeTime;

    @ApiModelProperty(value = "8D分数")
    private Integer score;

    @ApiModelProperty(value = "D5描述")
    @Column(name = "comment_d5")
    private String commentD5;

    @ApiModelProperty(value = "D6描述")
    @Column(name = "comment_d6")
    private String commentD6;

    @ApiModelProperty(value = "D7描述")
    @Column(name = "comment_d7")
    private String commentD7;

    @ApiModelProperty(value = "D3-是否存在临时文件")
    private Boolean hasTempFile = false;

    @ApiModelProperty(value = "D7-是否回收临时文件")
    private Boolean recoverTempFile = false;

    @ApiModelProperty(value = "D7-临时文件描述")
    private String tempFileComment;

    @ApiModelProperty(value = "关闭时长")
    private String duration;

    @ApiModelProperty(value = "8D编码")
    private String encodeNum;

    @ApiModelProperty(value = "小组组长")
    private Long leaderId;

    @ApiModelProperty(value = "拒绝理由")
    private String reason;

    @Override
    public int hashCode() {
        return Objects.hash(id, issueTitle);
    }

    public void copy(Issue source) {
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
    }
}
