package me.zhengjie.service.dto;

import lombok.Getter;
import lombok.Setter;
import me.zhengjie.base.BaseDTO;
import java.io.Serializable;
import java.util.List;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/9/28 13:35
 */
@Getter
@Setter
public class SelfTemplateDto extends BaseDTO implements Serializable {

    private Long id;

    private Long templateId;

    private String itemName;

    private String itemContent;

    private Double itemScore;

    private Long pid;

    private String pItemName;

    private Boolean oldFlag;

    private List<SelfTemplateDto> children;
}
