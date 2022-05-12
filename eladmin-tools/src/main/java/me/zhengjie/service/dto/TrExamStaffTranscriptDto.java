package me.zhengjie.service.dto;


import lombok.*;
import me.zhengjie.base.BaseDTO;
import me.zhengjie.domain.TrExamStaffFile;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @author TongMin Jie
 * @version V1
 * @date 2022/5/11 14:24
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TrExamStaffTranscriptDto extends BaseDTO implements Serializable {

    private Long id;

    private Long trExamStaffId;

    private String examContent;

    private Timestamp examDate;

    private Integer examScore;

    private Boolean examPassed;

    private String examType;

    private Timestamp nextDate;

    private Integer resitSort;

    private String examDesc;

    private List<TrExamStaffFile> fileList = new ArrayList<>();

}
