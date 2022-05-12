package me.zhengjie.service.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.zhengjie.base.BaseDTO;

import java.io.Serializable;

/**
 * @author tmj
 * @version 1.0
 * @date 2022/05/12 10:31
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TrainMaterialDepartDto extends BaseDTO implements Serializable {

    private Long id;

    private Long departId;

    private String departName;

    private Boolean enabled;


}
