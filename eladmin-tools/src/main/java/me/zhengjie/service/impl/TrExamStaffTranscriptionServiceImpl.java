package me.zhengjie.service.impl;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.config.FileProperties;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.*;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.*;
import me.zhengjie.service.TrExamStaffTranscriptionService;
import me.zhengjie.utils.DateUtil;
import me.zhengjie.utils.FileUtil;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.List;
import java.util.Set;

/**
 * @author TongMin Jie
 * @version V1
 * @date 2022/5/16 13:54
 */
@Service
@RequiredArgsConstructor
public class TrExamStaffTranscriptionServiceImpl implements TrExamStaffTranscriptionService {

    private final TrExamStaffTranscriptRepository transcriptRepository;
    private final FileProperties properties;
    private final TrainNewStaffRepository staffTrainRepository;
    private final TrainExamStaffRepository examStaffRepository;
    private final TrainCertificationRepository certificationRepository;
    private final TrainScheduleRepository trScheduleRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadFile(Long trExamStaffId, String examContent, String examDate, Integer examScore, Boolean examPassed, String examType, String nextDate, Integer resitSort, String examDesc, String name, MultipartFile multipartFile) throws ParseException {
        TrainExamStaff examStaff = examStaffRepository.findById(trExamStaffId).orElseGet(TrainExamStaff::new);
        ValidationUtil.isNull(examStaff.getId(), "TrainExamStaff", "id", trExamStaffId);
        FileUtil.checkSize(properties.getMaxSize(), multipartFile.getSize());
        String suffix = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
        assert suffix != null;
        String type = FileUtil.getFileType(suffix);

        if (examPassed) {
            nextDate = null;
        }
        File file = FileUtil.upload(multipartFile, properties.getPath().getPath() + type + File.separator);
        if (ObjectUtil.isNull(file)) {
            throw new BadRequestException("上传失败");
        }
        try {
            TrExamStaffTranscript transcript = new TrExamStaffTranscript(
                    trExamStaffId,
                    examContent,
                    // string转timestamp
                    examDate != null ? DateUtil.transToTimestamp(examDate) : null,
                    examScore,
                    examPassed,
                    examType,
                    // string转timestamp
                    nextDate != null ? DateUtil.transToTimestamp(nextDate) : null,
                    resitSort,
                    examDesc,
                    file.getName(),
                    name,
                    suffix,
                    file.getPath(),
                    type,
                    FileUtil.getSize(multipartFile.getSize())
            );
            transcriptRepository.save(transcript);
            // 反查员工培训，确定是否发证书？若需要，当考试通过时候则需要同步证书
            TrainNewStaff staff = staffTrainRepository.findAllByTrScheduleIdAndUserId(examStaff.getTrScheduleId(), examStaff.getUserId());
            TrainSchedule schedule = trScheduleRepository.findById(examStaff.getTrScheduleId()).orElseGet(TrainSchedule::new);
            ValidationUtil.isNull(schedule.getId(), "TrainSchedule", "id", examStaff.getTrScheduleId());
            if (examPassed) {
                // 考试通过则更改员工培训记录信息
                staff.setIsAuthorize(true);
                if (!staff.getIsFinished()) {
                    staff.setIsFinished(false);
                    staff.setReason("通过考试，待上传相关证明");
                } else {
                    staff.setReason(null);
                }
                // 通过考试后，若需要发证则生成培训证书记录
                if (schedule.getIsCert()) {
                    TrainCertification cert = new TrainCertification();
                    cert.setUserId(staff.getUserId());
                    cert.setStaffName(staff.getStaffName());
                    cert.setHireDate(staff.getHireDate());
                    cert.setDepartId(staff.getDepartId());
                    cert.setSuperior(staff.getSuperior());
                    cert.setJobName(staff.getJobName());
                    cert.setJobNum(staff.getJobNum());
                    cert.setCertificationType(CommonConstants.STAFF_CER_TYPE_JOB);
                    cert.setTrScheduleId(staff.getTrScheduleId());
                    cert.setIsRemind(false);
                    if (schedule.getIsDelay()) {
                        cert.setTrainDate(schedule.getNewTrainTime());
                    } else {
                        cert.setTrainDate(schedule.getTrainTime());
                    }
                    cert.setTrainContent(schedule.getTrainContent());
                    cert.setTrainResult("通过考试");
                    certificationRepository.save(cert);
                }
            } else {
                // 考试不通过则更正员工培训未完成原因
                staff.setIsAuthorize(false);
                staff.setReason("尚未通过考试");
                // 若存在发证信息
                certificationRepository.deleteAllByCertTypeAndTrScheduleIdAndUserId(CommonConstants.STAFF_CER_TYPE_JOB, examStaff.getTrScheduleId(), examStaff.getUserId());
            }
            staffTrainRepository.save(staff);
        } catch (Exception e) {
            FileUtil.del(file);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TrExamStaffTranscript> getByTrExamStaffId(Long trExamStaffId) {
        return transcriptRepository.findByTrExamStaffId(trExamStaffId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        TrExamStaffTranscript trans = transcriptRepository.findById(id).orElseGet(TrExamStaffTranscript::new);
        ValidationUtil.isNull(trans.getId(), "TrExamStaffTranscript", "id", id);
        if (trans.getExamPassed()) {
            // 考试原是通过但被删除则需要更改员工培训记录信息
            // 1.获取员工考试信息
            TrainExamStaff examStaff = examStaffRepository.findById(trans.getTrExamStaffId()).orElseGet(TrainExamStaff::new);
            ValidationUtil.isNull(examStaff.getId(), "TrainExamStaff", "id", trans.getTrExamStaffId());
            // 2.定位到培训计划
            TrainSchedule schedule = trScheduleRepository.findById(examStaff.getTrScheduleId()).orElseGet(TrainSchedule::new);
            ValidationUtil.isNull(schedule.getId(), "TrainSchedule", "id", examStaff.getTrScheduleId());
            // 3.收回员工培训记录状态变更为考试未通过
            TrainNewStaff staff = staffTrainRepository.findAllByTrScheduleIdAndUserId(examStaff.getTrScheduleId(), examStaff.getUserId());
            // 考试不通过则更正员工培训未完成原因
            staff.setIsAuthorize(false);
            staff.setReason("尚未通过考试");
            staffTrainRepository.save(staff);
            // 4.若发放证书应当及时召回
            if (schedule.getIsCert()) {
                // 找回证书
                certificationRepository.deleteAllByCertTypeAndTrScheduleIdAndUserId(CommonConstants.STAFF_CER_TYPE_JOB, examStaff.getTrScheduleId(), examStaff.getUserId());
            }
        }
        transcriptRepository.deleteAllById(id);
    }
}
