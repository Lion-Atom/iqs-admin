package me.zhengjie.service.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.zhengjie.base.BaseDTO;
import me.zhengjie.base.CommonDTO;
import me.zhengjie.domain.TimeManagement;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author tmj
 * @version 1.0
 * @date 2022/05/09 10:31
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TrainExamDepartDto extends BaseDTO implements Serializable {

    private Long id;

    private Long departId;

    private String departName;

    private Boolean enabled;



}
