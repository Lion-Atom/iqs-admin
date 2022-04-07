package me.zhengjie.utils;

import lombok.RequiredArgsConstructor;
import me.zhengjie.constants.CommonConstants;
import me.zhengjie.domain.Approver;
import me.zhengjie.domain.FileDept;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.repository.ApproverRepository;
import me.zhengjie.repository.FileDeptRepository;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/8/18 10:22
 */
@Service
@RequiredArgsConstructor
public class CommonUtils {
    private final ApproverRepository approverRepository;
    private final FileDeptRepository deptRepository;

    /**
     * @return 质量部Master --- 前置条件：质量部不可删除
     */
    public Long getZlbMaster() {
        // 获取质量部Master
        Long zlbMaster = null;
        List<Approver> list = approverRepository.findByDeptIdAndIsMaster(CommonConstants.ZL_DEPART, true);
        // list 最多包含一条
        if (ValidationUtil.isNotEmpty(list)) {
            zlbMaster = list.get(0).getId();
        } else {
            throw new BadRequestException("请务必设置质量部管理人员，以保证审批流程的正常流转！");
        }
        return zlbMaster;
    }

    public Long getSuperiorId() {
        // 获取上级信息
        Long superiorId = SecurityUtils.getCurrentUserSuperior();
        if (superiorId == null) {
            Long curDeptId = SecurityUtils.getCurrentDeptId();
            FileDept dept = deptRepository.findById(curDeptId).orElseGet(FileDept::new);
            // 上级部门标识
            if (dept.getPid() != null) {
                List<Approver> list = approverRepository.findByDeptIdAndIsMaster(dept.getPid(), true);
                if (ValidationUtil.isNotEmpty(list)) {
                    superiorId = list.get(0).getId();
                }
            }
        }
        return superiorId;
    }

    public static Timestamp getNow(){
        Date date = new Date();
        return new Timestamp(date.getTime());
    }

    public Date getYearBegin(Date time) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(time);
        //设置月和日都为1，即为开始时间（注：月份是从0开始;日中0表示上个月最后一天，1表示本月开始第一天）
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        //将小时置为0
        cal.set(Calendar.HOUR_OF_DAY, 0);
        //将分钟置为0
        cal.set(Calendar.MINUTE, 0);
        //将秒置为0
        cal.set(Calendar.SECOND,0);
        //将毫秒置为0
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public Date getYearEnd(Date time) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(time);
        //设置月为12，月份从0开始
        cal.set(Calendar.MONTH, 11);
        //设置为当月最后一天
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        //将小时置为23
        cal.set(Calendar.HOUR_OF_DAY, 23);
        //将分钟置为59
        cal.set(Calendar.MINUTE, 59);
        //将秒置为59
        cal.set(Calendar.SECOND,59);
        //将毫秒置为999
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }
}
