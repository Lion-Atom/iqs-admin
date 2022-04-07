package me.zhengjie.service.dto;

import lombok.Getter;
import lombok.Setter;
import me.zhengjie.base.BaseDTO;

import java.io.Serializable;
import java.util.List;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/8/3 14:17
 */

@Getter
@Setter
public class IssueAnalysisDto extends BaseDTO implements Serializable {

    private Long id;

    private Long issueId;

    private String systemWide;

    private Long systemNum;

    private String result;

    /**
     * 永久措施名称集合
     */
    private List<String> actionNames;

}
