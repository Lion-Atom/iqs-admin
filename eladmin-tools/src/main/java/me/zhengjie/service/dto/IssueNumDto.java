package me.zhengjie.service.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.zhengjie.base.BaseDTO;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/23 9:57
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class IssueNumDto extends BaseDTO implements Serializable {

    private Long id;

    private Long issueId;

    private String caPartNum;

    private Timestamp componentDateCode;

    private String componentLotNum;

    private String defectQuantity;

    private String customerImpact;
}
