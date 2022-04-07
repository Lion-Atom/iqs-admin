package me.zhengjie.service.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.zhengjie.base.BaseDTO;

import java.io.Serializable;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/27 09:50
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class StepDefectDto extends BaseDTO implements Serializable {

    private Long id;

    private Long issueId;

    private String processStep;

    private Boolean created = false;

    private Boolean detected = false;

    private Boolean shouldDetected = false;
}
