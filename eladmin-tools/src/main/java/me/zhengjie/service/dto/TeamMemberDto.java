package me.zhengjie.service.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.zhengjie.base.BaseDTO;

import java.io.Serializable;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/23 14:50
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class TeamMemberDto extends BaseDTO implements Serializable {

    private Long id;

    private Long teamId;

    private Long issueId;

    private Long userId;

    private String companyName;

    private String deptName;

    private String userName;

    private String phone;

    private String email;

    private Boolean isLeader;

    private String teamRole;
}
