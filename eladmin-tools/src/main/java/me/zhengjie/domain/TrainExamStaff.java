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
    @Column(name = "depart_id")
    @ApiModelProperty(value = "部门ID")
    private Long departId;

    @NotNull
    @ApiModelProperty(value = "上级主管")
    private String superior;

    @NotNull
    @ApiModelProperty(value = "岗位级别")
    private String jobName;

    @NotNull
    @ApiModelProperty(value = "车间")
    private String workshop;

    @NotNull
    @ApiModelProperty(value = "工种")
    private String jobType;


}
