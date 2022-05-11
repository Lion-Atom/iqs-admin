package me.zhengjie.service.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.zhengjie.base.BaseDTO;

import java.io.Serializable;

/**
 * @author TongMin Jie
 * @version V1
 * @date 2022/5/11 9:35
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TrExamDepartFileDto extends BaseDTO implements Serializable {

    private Long id;

    private Long departId;

    private String realName;

    private String name;

    private String fileDesc;

    private String suffix;

    private String path;

    private String type;

    private String size;

    private boolean hasDownloadAuthority = true;
}
