package me.zhengjie.service.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
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
public class TimeManagementDto extends BaseDTO implements Serializable {

    private Long id;

    private Long issueId;

    private Boolean d1Status;

    private Timestamp d1Time;

    private Boolean d2Status;

    private Timestamp d2Time;

    private Boolean d3Status;

    private Timestamp d3Time;

    private Boolean d4Status;

    private Timestamp d4Time;

    private Boolean d5Status;

    private Timestamp d5Time;

    private Boolean d6Status;

    private Timestamp d6Time;

    private Boolean d7Status;

    private Timestamp d7Time;

    private Boolean d8Status;

    private Timestamp d8Time;

    private String planStep1;

    private Timestamp planTime1;

    private String planStep2;

    private Timestamp planTime2;

    private String planStep3;

    private Timestamp planTime3;

    private String curStep;

}
