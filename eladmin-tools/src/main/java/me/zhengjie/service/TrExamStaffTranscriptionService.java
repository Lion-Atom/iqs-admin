package me.zhengjie.service;

import me.zhengjie.domain.TrExamStaffTranscript;
import me.zhengjie.domain.TrNewStaffFile;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2022/05/07 09:53
 */
public interface TrExamStaffTranscriptionService {

    /**
     * 上传新员工培训相关试卷信息
     *
     * @param trExamStaffId 新员工培训ID
     * @param examContent   考试内容
     * @param examDate      考试日期
     * @param examScore     考试分数
     * @param examPassed    是否通过
     * @param examType      考试类型
     * @param nextDate      下次考试日期
     * @param resitSort     考试次数
     * @param examDesc      考试描述
     * @param name          试卷名称
     * @param file          试卷信息
     */
    void uploadFile(Long trExamStaffId, String examContent, String examDate, Integer examScore, Boolean examPassed, String examType, String nextDate, Integer resitSort, String examDesc, String name, MultipartFile file) throws ParseException;

    /**
     * 根据新员工培训ID查询相关试卷信息
     *
     * @param trExamStaffId 新员工培训ID
     * @return /
     */
    List<TrExamStaffTranscript> getByTrExamStaffId(Long trExamStaffId);

    /**
     * @param id 附件标识
     */
    void delete(Long id);

}
