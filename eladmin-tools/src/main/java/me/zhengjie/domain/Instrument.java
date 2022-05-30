package me.zhengjie.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
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
 * @date 2022/5/27 10:21
 */
@Entity
@Getter
@Setter
@Table(name = "tools_instrument")
public class Instrument extends BaseEntity implements Serializable {

    @Id
    @Column(name = "instru_id")
    @NotNull(groups = Update.class)
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @ApiModelProperty(value = "仪器名称")
    private String instruName;

    @NotBlank
    @ApiModelProperty(value = "出厂型号")
    private String instruNum;

    @ApiModelProperty(value = "资产号")
    private String assetNum;

    @ApiModelProperty(value = "出厂日期")
    private Timestamp purDate;

    @NotBlank
    @ApiModelProperty(value = "内部ID")
    private String innerId;

    @NotBlank
    @ApiModelProperty(value = "存放位置")
    private String position;

    @NotBlank
    @ApiModelProperty(value = "保管人")
    private String keeper;

    @NotBlank
    @ApiModelProperty(value = "测量范围")
    private String caliScope;

    @NotBlank
    @ApiModelProperty(value = "精度要求")
    private String precise;

    @NotBlank
    @ApiModelProperty(value = "允许误差")
    private String errorRange;

    @NotBlank
    @ApiModelProperty(value = "使用区域")
    private String useArea;

    @NotBlank
    @ApiModelProperty(value = "使用人")
    private String useBy;

    @NotBlank
    @ApiModelProperty(value = "仪器状态")
    private String status;

    @ApiModelProperty(value = "校准状态")
    private String caliStatus;

    @ApiModelProperty(value = "限制使用说明")
    private String limitRemark;

    @ApiModelProperty(value = "报废说明")
    private String dropRemark;

    @ApiModelProperty(value = "校准周期")
    private Integer caliPeriod;

    @ApiModelProperty(value = "校准周期时间单位")
    private String periodUnit;

    @ApiModelProperty(value = "上次校准日期")
    private Timestamp lastCaliDate;

    @ApiModelProperty(value = "下次校准日期")
    private Timestamp nextCaliDate;

    @ApiModelProperty(value = "是否是内部校准")
    private Boolean innerChecked;

    @ApiModelProperty(value = "校准机构ID")
    private Long caliOrgId;

    @ApiModelProperty(value = "是否是上门校准")
    private Boolean isDoor;

    @ApiModelProperty(value = "是否需要下次校准前提醒")
    private Boolean isRemind;

    @ApiModelProperty(value = "提前提醒天数（需要提醒则须填写提前几天提醒）")
    private Integer remindDays;

}
