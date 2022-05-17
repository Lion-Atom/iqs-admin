package me.zhengjie.domain;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.zhengjie.base.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author TongMin Jie
 * @version V1
 * @date 2022/5/11 14:24
 */
@Getter
@Setter
@Entity
@Table(name = "train_exam_staff_transcript")
@NoArgsConstructor
public class TrExamStaffTranscript extends BaseEntity implements Serializable {

    @Id
    @Column(name = "tr_exam_staff_transcript_id")
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "train_exam_staff_id")
    @ApiModelProperty(value = "培训考试员工ID")
    private Long trExamStaffId;

    @NotNull
    @ApiModelProperty(value = "考试内容")
    private String examContent;

    @ApiModelProperty(value = "考试日期")
    private Timestamp examDate;

    @NotNull
    @ApiModelProperty(value = "考试分数")
    private Integer examScore;

    @NotNull
    @ApiModelProperty(value = "是否通过")
    private Boolean examPassed;

    @NotNull
    @ApiModelProperty(value = "考试类型 /n 初试/补考")
    private String examType;

    @ApiModelProperty(value = "下次补考日期")
    private Timestamp nextDate;

    @ApiModelProperty(value = "补考次序")
    private Integer resitSort;

    @ApiModelProperty(value = "备注")
    private String examDesc;

    @ApiModelProperty(value = "真实文件名")
    private String realName;

    @ApiModelProperty(value = "文件名")
    private String name;

    @ApiModelProperty(value = "后缀")
    private String suffix;

    @ApiModelProperty(value = "路径")
    private String path;

    @ApiModelProperty(value = "类型")
    private String type;

    @ApiModelProperty(value = "大小")
    private String size;

    public TrExamStaffTranscript(Long trExamStaffId, String examContent, Timestamp examDate, Integer examScore, Boolean examPassed, String examType,
                                 Timestamp nextDate, Integer resitSort, String examDesc, String realName, String name, String suffix, String path, String type, String size) {
        this.trExamStaffId = trExamStaffId;
        this.examContent = examContent;
        this.examDate = examDate;
        this.examScore = examScore;
        this.examPassed = examPassed;
        this.examType = examType;
        this.nextDate = nextDate;
        this.resitSort = resitSort;
        this.examDesc = examDesc;
        this.realName = realName;
        this.name = name;
        this.suffix = suffix;
        this.path = path;
        this.type = type;
        this.size = size;
    }

    public void copy(TrExamStaffTranscript source) {
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
    }
}
