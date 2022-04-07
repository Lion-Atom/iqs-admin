/*
 *  Copyright 2019-2020 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package me.zhengjie.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import me.zhengjie.base.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author Tong Minjie
 * @date 2021-07-23
 */
@Entity
@Getter
@Setter
@Table(name = "tool_supplier")
public class Supplier extends BaseEntity implements Serializable {

    @Id
    @Column(name = "supplier_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "ID", hidden = true)
    private Long id;

    // 供应商基本信息
    @ApiModelProperty(value = "供应商编号")
    private String supplierCode;

    @NotBlank
    @ApiModelProperty(value = "供应商名称")
    private String name;

    @ApiModelProperty(value = "是否是准入供应商")
    private Boolean isQualified;

    @ApiModelProperty(value = "供应商简称")
    private String abb;

/*    @ApiModelProperty(value = "认证时间")
    private Timestamp certificationTime;*/

    @NotBlank
    @ApiModelProperty(value = "供应商类别")
    private String type;

    @ApiModelProperty(value = "供应商级别")
    private String level;

    @ApiModelProperty(value = "供应商简介")
    private String profile;

    @ApiModelProperty(value = "供应商状态")
    private String status;

    /*@ApiModelProperty(value = "审核状态")
    private String approvalStatus;*/

    @ApiModelProperty(value = "产品")
    private String production;

    // 物流信息
    @ApiModelProperty(value = "运送方式")
    private String deliveryMethod;

    @ApiModelProperty(value = "物流公司")
    private String logistics;

    // 公司信息

    @ApiModelProperty(value = "国家")
    private String country;

    @ApiModelProperty(value = "省份")
    private String provincial;

    @ApiModelProperty(value = "市（县）")
    private String city;

    @ApiModelProperty(value = "邮政编码")
    private String zip;

    @ApiModelProperty(value = "通讯地址")
    private String address;

    @ApiModelProperty(value = "传真")
    private String fax;

    @ApiModelProperty(value = "DUNS编码")
    private String dunsNum;

    @ApiModelProperty(value = "法人名称")
    private String leyalPerson;

    @ApiModelProperty(value = "成立时间")
    private Timestamp foundationDate;

    @ApiModelProperty(value = "注册ID")
    private String regId;

    @ApiModelProperty(value = "注册时间")
    private Timestamp regDate;

    @ApiModelProperty(value = "在线咨询")
    private String onlineConsul;

    @ApiModelProperty(value = "公司邮箱")
    private String companyEmail;

    @ApiModelProperty(value = "公司主页网址")
    private String webSite;

    @ApiModelProperty(value = "经营范围")
    private String companyScope;

    // 公司数据
    @ApiModelProperty(value = "员工总数")
    private Integer empTotal;

    @ApiModelProperty(value = "管理层人数")
    private Integer manNum;

    @ApiModelProperty(value = "普通员工数目")
    private Integer workerNum;

    @ApiModelProperty(value = "临时员工数目")
    private Integer tempNum;

    @ApiModelProperty(value = "每日工作时长")
    private Double dailyHour;

    @ApiModelProperty(value = "每周工作天数")
    private Integer workDay;

    @ApiModelProperty(value = "前年营业额")
    private String thirdTurnover;

    @ApiModelProperty(value = "去年营业额")
    private String secondTurnover;

    @ApiModelProperty(value = "今年营业额")
    private String nowTurnover;

    @ApiModelProperty(value = "今年营业额")
    private String forecastTurnover;

    //  财务信息
    @ApiModelProperty(value = "结算方式")
    private String settlement;

    @ApiModelProperty(value = "币种")
    private String currency;

    @ApiModelProperty(value = "银行账号")
    private String bankAccount;

    @ApiModelProperty(value = "开户行")
    private String bankName;

    @ApiModelProperty(value = "户名")
    private String accountName;

    @ApiModelProperty(value = "税号")
    private String dutyParagraph;

    // 准入信息
    @ApiModelProperty(value = "获取准入资格时间")
    private Timestamp qualifiedDate;

    @ApiModelProperty(value = "准入得分")
    private Double qualifiedScore;

    // 审核信息
    @ApiModelProperty(value = "审核时间")
    private Timestamp auditDate;

    @ApiModelProperty(value = "审核结果")
    private String auditResult;

    @ApiModelProperty(value = "问题状态")
    private String questionStatus;

}
