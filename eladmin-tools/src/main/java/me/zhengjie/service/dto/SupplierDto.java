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
package me.zhengjie.service.dto;

import lombok.Getter;
import lombok.Setter;
import me.zhengjie.base.BaseDTO;
import me.zhengjie.domain.SupplierContact;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tong Minjie
 * @date 2021-07-23
 */
@Getter
@Setter
public class SupplierDto extends BaseDTO implements Serializable {

    private Long id;

    // 供应商基本信息

    private String supplierCode;

    private String name;

    private String production;

    private String[] prodTags;

    private String abb;

    private Boolean isQualified;

//    private Timestamp certificationTime;

    private String type;

    private String level;

    private String profile;

    private String status;

//    private String approvalStatus;

    // 公司信息
    private String country;

    private String provincial;

    private String city;

    private String zip;

    private String address;

    private String fax;

    private String dunsNum;

    private String leyalPerson;

    private Timestamp foundationDate;

    private String regId;

    private Timestamp regDate;

    private String onlineConsul;

    private String companyEmail;

    private String webSite;

    private String companyScope;

    // 公司数据
    private Integer empTotal;

    private Integer manNum;

    private Integer workerNum;

    private Integer tempNum;

    private Double dailyHour;

    private Integer workDay;

    private String thirdTurnover;

    private String secondTurnover;

    private String nowTurnover;

    private String forecastTurnover;

    //  财务信息
    private String settlement;

    private String currency;

    private String bankAccount;

    private String bankName;

    private String accountName;

    private String dutyParagraph;

    // 准入信息
    private Timestamp qualifiedDate;

    private Double qualifiedScore;

    // 审核信息
    private Timestamp auditDate;

    private String auditResult;

    private String questionStatus;

    // 物流信息
    private String deliveryMethod;

    // 物流公司可能多个“,”分隔
    private String logistics;

    private String[] logisticsTags;

    // 最新年度审核结果
    private Double assessScore;

    // 联系人信息
    private List<SupplierContact> contacts = new ArrayList<>();
}
