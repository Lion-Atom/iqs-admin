package me.zhengjie.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.zhengjie.base.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author TongMin Jie
 * @version V1
 * @date 2022/5/11 11:34
 */
@Getter
@Setter
@Entity
@Table(name = "train_exam_staff")
@NoArgsConstructor
public class TrainExamStaff extends BaseEntity implements Serializable {

    @Id
    @Column(name = "train_exam_staff_id")
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ApiModelProperty(value = "员工姓名")
    private String staffName;

    @NotNull
    @ApiModelProperty(value = "入职日期")
    private Timestamp hireDate;

    @NotNull
    @Column(name = "depart_id")
    @ApiModelProperty(value = "部门ID")
    private Long departId;

    @NotBlank
    @ApiModelProperty(value = "上级主管")
    private String superior;

    @NotNull
    @ApiModelProperty(value = "岗位级别")
    private String jobName;

    @ApiModelProperty(value = "车间")
    private String workshop;

    @ApiModelProperty(value = "班组")
    private String team;

    @ApiModelProperty(value = "工号")
    private String jobNum;

    @NotBlank
    @ApiModelProperty(value = "员工分类")
    private String staffType;

    @ApiModelProperty(value = "工种")
    private String jobType;

    @NotNull
    @Column(name = "train_schedule_id")
    @ApiModelProperty(value = "培训计划ID")
    private Long trScheduleId;

    @NotNull
    @ApiModelProperty(value = "允许操作")
    private Boolean isAuthorize;
}
