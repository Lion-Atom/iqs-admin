package me.zhengjie.service.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.zhengjie.base.BaseDTO;

import java.io.Serializable;

/**
 * @author TongMin Jie
 * @version V1
 * @date 2022/5/12 13:39
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TrainMaterialFileDto extends BaseDTO implements Serializable {

    private Long id;

    private Long departId;

    private String departName;

    private String author;

    private String version;

    private Boolean isInternal;

    private String toolType;

    private String realName;

    private String name;

    private String fileDesc;

    private String suffix;

    private String path;

    private String type;

    private String size;

    private Boolean enabled;

    private Boolean hasEditAuthorized;
}
