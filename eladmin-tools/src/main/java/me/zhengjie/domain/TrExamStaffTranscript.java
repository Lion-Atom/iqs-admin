package me.zhengjie.domain;

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

}
