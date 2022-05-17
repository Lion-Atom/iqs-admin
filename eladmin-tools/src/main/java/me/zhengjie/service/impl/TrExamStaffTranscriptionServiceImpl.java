package me.zhengjie.service.impl;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.config.FileProperties;
import me.zhengjie.domain.TrExamStaffTranscript;
import me.zhengjie.domain.TrNewStaffFile;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.TrExamStaffTranscriptRepository;
import me.zhengjie.repository.TrainExamStaffRepository;
import me.zhengjie.service.TrExamStaffTranscriptionService;
import me.zhengjie.utils.DateUtil;
import me.zhengjie.utils.FileUtil;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadFile(Long trExamStaffId, String examContent, String examDate, Integer examScore, Boolean examPassed, String examType, String nextDate, Integer resitSort, String examDesc, String name, MultipartFile multipartFile) throws ParseException {
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
                    examDate != null ? DateUtil.transToTimestamp(examDate) : null,
                    examScore,
                    examPassed,
                    examType,
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
    public void delete(Set<Long> ids) {
        transcriptRepository.deleteAllByIdIn(ids);
    }
}
