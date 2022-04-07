package me.zhengjie.service.dto;

import lombok.Data;
import me.zhengjie.base.BaseDTO;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/23 9:57
 */

@Data
public class TeamDto extends BaseDTO implements Serializable {

    private Long id;

    private Long issueId;

    private String name;

    private Long leaderId;
}
