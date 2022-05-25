package me.zhengjie.constants;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/6/7 11:17
 */

public class CommonConstants {

    /**
     * 已删除，已作废数据
     */
    public static final Long IS_DEL = 1L;

    /**
     * 未删除，有效数据
     */
    public static final Long NOT_DEL = 0L;

    /**
     * 字典-文件用途标识
     */
    public static final Long DICT_FILE_TYPE = 9L;

    /**
     * 字典-8D执行选择标识
     */
    public static final Long DICT_E_EXECUTE = 14L;

    /**
     * 字典-审核种类
     */
    public static final String AUDIT_TYPE = "audit_type";

    /**
     * 字典-审核体系
     */
    public static final String AUDIT_SYSTEM_TYPE = "audit_system";

    /**
     * 字典-审核原因
     */
    public static final String AUDIT_REASON_TYPE = "audit_reason";

    /**
     * 质量部门标识
     */
    public static final Long ZL_DEPART = 28L;

    /**
     * 新建
     */
    public static final String NEW = "NEW 新建";

    /**
     * 变更
     */
    public static final String REVISE = "REVISE 修改";

    /**
     * 变更
     */
    public static final String UPGRADE_VERSION = "UPGRADE 升版";

    /**
     * 文件状态：草稿
     */
    public static final String DRAFT_STATUS = "draft";

    /**
     * 文件状态：已发布
     */
    public static final String RELEASE_STATUS = "release";

    /**
     * 文件状态：草稿
     */
    public static final String TEMP_STATUS = "temp";

    /**
     * 文件状态：报废
     */
    public static final String OBSOLETE_STATUS = "obsolete";

    /**
     * 文件保密性：内部
     */
    public static final String SECURITY_INTERNAL = "internal";

    /**
     * 文件保密性：外部
     */
    public static final String SECURITY_EXTERNAL = "external";

    /**
     * 审批状态：已作废
     */
    public static final String OBSOLETED_STATUS = "obsoleted";

    /**
     * 审批状态：已审批
     */
    public static final String APPROVED_STATUS = "approved";

    /**
     * 审批状态：待审批
     */
    public static final String WAITING_FOR_STATUS = "waitingfor";

    /**
     * 日志类型 - 文件信息
     */
    public static final String LOG_TYPE_FILE = "文件信息";

    /**
     * 日志类型 - 仪器校准
     */
    public static final String LOG_TYPE_INSTRUMENT_CALIBRATION = "仪器校准";

    /**
     * 8D状态-待审核
     */
    public static final String D_STATUS_AUDIT = "待审核";

    /**
     * 8D状态-待审核
     */
    public static final String D_STATUS_REJECT = "驳回";

    /**
     * 8D状态-待进行
     */
    public static final String D_STATUS_OPEN = "待进行";

    /**
     * 8D状态-进行中
     */
    public static final String D_STATUS_IN_PROCESS = "进行中";

    /**
     * 8D状态-完成
     */
    public static final String D_STATUS_DONE = "完成";

    /**
     * 8D状态-直接关闭
     */
    public static final String D_STATUS_CLOSE = "直接关闭";

    /**
     * 8D状态-单独报告
     */
    public static final String D_STATUS_SINGLE_REPORT = "单独报告";

    /**
     * 8D小组成员角色-组长
     */
    public static final String D_TEAM_ROLE_LEADER = "组长";

    /**
     * 8D小组成员角色-管理员
     */
    public static final String D_TEAM_ROLE_MANAGER = "管理层";

    /**
     * 8D小组成员角色-组员
     */
    public static final String D_TEAM_ROLE_MEMBER = "组员";

    /**
     * 临床服务列表的不显示的服务类型集合
     */
    public static final List<String> D_STEP_LIST = new ArrayList<>();

    static {
        D_STEP_LIST.add("D1");
        D_STEP_LIST.add("D2");
        D_STEP_LIST.add("D3");
        D_STEP_LIST.add("D4");
        D_STEP_LIST.add("D5");
        D_STEP_LIST.add("D6");
        D_STEP_LIST.add("D7");
        D_STEP_LIST.add("D8");
    }

    /**
     * 临床服务列表的不显示的服务类型集合
     */
    public static final List<String> FILE_TYPE_LIST = new ArrayList<>();

    static {
        FILE_TYPE_LIST.add("图片");
        FILE_TYPE_LIST.add("文档");
        FILE_TYPE_LIST.add("音乐");
        FILE_TYPE_LIST.add("视频");
        FILE_TYPE_LIST.add("其他");
    }



    /**
     * 内容为空
     */
    public static final String IS_BLANK = "-";

    /**
     * 8D-当前步骤-D0（审核）
     */
    public static final String D_STEP_D0 = "D0";

    /**
     * 8D-当前步骤-D1
     */
    public static final String D_STEP_D1 = "D1";

    /**
     * 8D-当前步骤-D2
     */
    public static final String D_STEP_D2 = "D2";

    /**
     * 8D-当前步骤-D3
     */
    public static final String D_STEP_D3 = "D3";

    /**
     * 8D-当前步骤-D4
     */
    public static final String D_STEP_D4 = "D4";

    /**
     * 8D-当前步骤-D5
     */
    public static final String D_STEP_D5 = "D5";

    /**
     * 8D-当前步骤-D6
     */
    public static final String D_STEP_D6 = "D6";

    /**
     * 8D-当前步骤-D7
     */
    public static final String D_STEP_D7 = "D7";

    /**
     * 8D-当前步骤-D8
     */
    public static final String D_STEP_D8 = "D8";

    /**
     * 8D-当前步骤-单独报告
     */
    public static final String D_STEP_REPORT = "separateReport";


    /**
     * 8D-文档更新描述
     */
    public static final String D7_CHANGE_DESC = "生产/流程技术文档更新";

    /**
     * 8D-文档其他描述
     */
    public static final String D7_OTHERS_DESC = "其他";

    /**
     * 原因分析-机器
     */
    public static final String CAUSE_MACHINE = "机器";

    /**
     * 原因分析-人员
     */
    public static final String CAUSE_MANPOWER = "人员";

    /**
     * 原因分析-材料
     */
    public static final String CAUSE_MATERIAL = "材料";

    /**
     * 原因分析-方法
     */
    public static final String CAUSE_METHOD = "方法";

    /**
     * 原因分析-环境
     */
    public static final String CAUSE_ENVIRONMENT = "环境";

    /**
     * 措施状态 - 开始
     */
    public static final String ACTION_OPEN = "开始";

    /**
     * 措施状态 - 移除
     */
    public static final String ACTION_REMOVED = "已移除";

    /**
     * 过程分析 - PART1
     */
    public static final String ANALYSIS_PART_1 = "所发现的问题是否会影响到产品、流程、工厂、部门?";

    /**
     * 过程分析 - PART2
     */
    public static final String ANALYSIS_PART_2 = "检测到的问题是否会影响到产品、流程、工厂、部门?";

    /**
     * 过程分析 - PART3
     */
    public static final String ANALYSIS_PART_3 = "分析过程中发现的其他潜在问题？";

    /**
     * 步骤格式化-成功
     */
    public static final String D_FORMAT_SUCCESS = "success";

    /**
     * 步骤格式化-进行中
     */
    public static final String D_FORMAT_PROCESS = "process";

    /**
     * 步骤格式化-进行中
     */
    public static final String D_FORMAT_WAIT = "wait";

    /**
     * 步骤格式化-超时
     */
    public static final String D_FORMAT_ERROR = "error";

    /**
     * 5W2H-when
     */
    public static final String QUESTION_WHEN = "when 问题时间";

    /**
     * 5W2H-where
     */
    public static final String QUESTION_WHERE = "where 问题在哪发生？";

    /**
     * 5W2H-what
     */
    public static final String QUESTION_WHAT = "what 问题是什么？";

    /**
     * 5W2H-who
     */
    public static final String QUESTION_WHO = "who 谁？";

    /**
     * 5W2H-why
     */
    public static final String QUESTION_WHY = "why 为什么是问题？";

    /**
     * 5W2H-how
     */
    public static final String QUESTION_HOW = "how 如何发现的？";

    /**
     * 5W2H-how many
     */
    public static final String QUESTION_HOW_MANY = "how many 数量？";

    /**
     * question-type-5W2H
     */
    public static final String QUESTION_5W2H = "5W2H";

    /**
     * question-type-IS/ISNot
     */
    public static final String QUESTION_IS_ISNOT = "IS/NOT";

    /**
     * IS/NOT-when
     */
    public static final String QUESTION_WHEN_IS = "when 何时";

    /**
     * IS/NOT-where
     */
    public static final String QUESTION_WHERE_IS = "where 哪里";

    /**
     * IS/NOT-what
     */
    public static final String QUESTION_WHAT_IS = "what 什么";

    /**
     * IS/NOT-what-对象
     */
    public static final String QUESTION_WHAT_IS_OBJECT = "对象";

    /**
     * IS/NOT-what-缺陷
     */
    public static final String QUESTION_WHAT_IS_DEFECT = "缺陷";

    /**
     * IS/NOT-where-位置/过程
     */
    public static final String QUESTION_WHERE_IS_POSITION = "位置/过程";

    /**
     * IS/NOT-where-何处
     */
    public static final String QUESTION_WHERE_IS_WHERE = "何处";

    /**
     * IS/NOT-when-何时
     */
    public static final String QUESTION_WHEN_IS_WHEN = "何时";

    /**
     * IS/NOT-when-模式
     */
    public static final String QUESTION_WHERE_IS_PATTERN = "模式";

    /**
     * IS/NOT-extent
     */
    public static final String QUESTION_EXTENT_IS = "extent 严重程度";

    /**
     * IS/NOT-extent-qt
     */
    public static final String QUESTION_EXTENT_IS_QT = "数量/程度";

    /**
     * 8D执行选择-系统8D
     */
    public static final String EXECUTE_8D = "系统8D";

    /**
     * 8D执行选择-系统8D
     */
    public static final String EXECUTE_CLOSE = "直接结案";

    /**
     * 8D执行选择-系统8D
     */
    public static final String EXECUTE_SINGLE_REPORT = "单独报告";

    /**
     * 任务类型-8D
     */
    public static final String TRAIL_TYPE_8D = "8D";

    /**
     * 任务类型-文件
     */
    public static final String TRAIL_TYPE_FILE = "文件";

    /**
     * 任务类型-审核人员审核
     */
    public static final String TRAIL_TYPE_AUDITOR = "审核人员批准";

    /**
     * 任务类型-审核
     */
    public static final String TRAIL_TYPE_AUDIT_PLAN = "审核计划";

    /**
     * 审核人员审核状态 - 无法激活
     */
    public static final String AUDIT_PLAN_STATUS_UNABLE_ACTIVATED = "无法激活";

    /**
     * 审核人员审核状态 - 待激活
     */
    public static final String AUDIT_PLAN_STATUS_TO_ACTIVATED = "待激活";

    /**
     * 审核人员审核状态 - 待审批
     */
    public static final String AUDIT_PLAN_STATUS_WAIT = "待批准";

    /**
     * 审核人员审核状态 - 已批准
     */
    public static final String AUDIT_PLAN_STATUS_APPROVED = "已批准";

    /**
     * 审核人员审核状态 - 不批准
     */
    public static final String AUDIT_PLAN_STATUS_REFUSED = "被驳回";

    /**
     * 审核人员有效状态 - 已过期
     */
    public static final String AUDITOR_STATUS_EXPIRE = "过期";

    /**
     * 审核人员有效状态 - 即将过期
     */
    public static final String AUDITOR_STATUS_SOON_TO_EXPIRE = "即将过期";

    /**
     * 审核人员状态 - 有效
     */
    public static final String AUDIT_PLAN_STATUS_VALID = "有效";

    /**
     * 审核人员有效期状态集合
     */
    public static final List<String> AUDITOR_STATUS_LIST = new ArrayList<>();

    static {
        AUDITOR_STATUS_LIST.add(AUDITOR_STATUS_EXPIRE);
        AUDITOR_STATUS_LIST.add(AUDITOR_STATUS_SOON_TO_EXPIRE);
        AUDITOR_STATUS_LIST.add(AUDIT_PLAN_STATUS_VALID);
    }

    /**
     * 审核计划状态 - 有效
     */
    public static final String AUDIT_PLAN_STATUS_REFUSE = "驳回";

    /**
     * 8D分数类型 - 3分制
     */
    public static final String SCORE_TYPE_THREE = "THREE";

    /**
     * 8D分数类型 - 5分制
     */
    public static final String SCORE_TYPE_FIVE = "FIVE";

    /**
     * 8D分数类型 - 7分制
     */
    public static final String SCORE_TYPE_SEVEN = "SEVEN";

    /**
     * 8D分数类型 - 10分制
     */
    public static final String SCORE_TYPE_TEN = "TEN";

    /**
     * 审核种类 -过程审核
     */
    public static final String AUDIT_PLAN_TYPE_GC = "过程审核";

    /**
     * 审核种类 -体系审核
     */
    public static final String AUDIT_PLAN_TYPE_TX = "体系审核";

    /**
     * 审核种类 - 产品审核
     */
    public static final String AUDIT_PLAN_TYPE_CP = "产品审核";

    /**
     * 审核种类 - 管理评审
     */
    public static final String AUDIT_PLAN_TYPE_GL = "管理评审";

    /**
     * 审核计划状态 - 计划
     */
    public static final String AUDIT_PLAN_STATUS_TO = "计划";

    /**
     * 审核计划状态 - 进行
     */
    public static final String AUDIT_PLAN_STATUS_PROCESS = "进行";

    /**
     * 审核计划状态 - 追踪
     */
    public static final String AUDIT_PLAN_STATUS_TRACE = "追踪";

    /**
     * 审核计划状态 - 结案
     */
    public static final String AUDIT_PLAN_STATUS_FINISHED = "结案";

    /**
     * 审核计划状态 - 过期
     */
    public static final String AUDIT_PLAN_STATUS_OVERDUE = "过期";

    /**
     * 审核计划状态
     */
    public static final List<String> AUDIT_PLAN_STATUS_LIST = new ArrayList<>();

    static {
        AUDIT_PLAN_STATUS_LIST.add(AUDIT_PLAN_STATUS_TO);
        AUDIT_PLAN_STATUS_LIST.add(AUDIT_PLAN_STATUS_PROCESS);
        AUDIT_PLAN_STATUS_LIST.add(AUDIT_PLAN_STATUS_TRACE);
        AUDIT_PLAN_STATUS_LIST.add(AUDIT_PLAN_STATUS_FINISHED);
    }

    /**
     * 审核计划-VDA6.3类型
     */
    public static final String VDA6_3_TEMP_TYPE = "VDA6.3";

    /**
     * 审核计划-推荐模板类型
     */
    public static final List<String> AUDIT_PLAN_TEMPLATE_LIST = new ArrayList<>();

    static {
        AUDIT_PLAN_TEMPLATE_LIST.add(VDA6_3_TEMP_TYPE);
    }

    /**
     * 审核人员审核计数 - 一个月
     */
    public static final int AUDIT_DAYS_MONTH = 30;

    /**
     * 审核人员审核计数 - 三个月
     */
    public static final int AUDIT_DAYS_SEASON = 90;

    /**
     * 改善措施-停用/完成
     */
    public static final List<String> ACTION_STATUS_LIST = new ArrayList<>();

    static {
        ACTION_STATUS_LIST.add("停用");
        ACTION_STATUS_LIST.add("完成");
    }

    /**
     * VDA6.3问题清单-项目管理
     */
    public static final String TEMPLATE_QUES_P2 = "P2";

    /**
     * VDA6.3问题清单-产品和过程开发的策划
     */
    public static final String TEMPLATE_QUES_P3 = "P3";

    /**
     * VDA6.3问题清单-产品和过程开发的实现
     */
    public static final String TEMPLATE_QUES_P4 = "P4";

    /**
     * VDA6.3问题清单-供应商管理
     */
    public static final String TEMPLATE_QUES_P5 = "P5";

    /**
     * VDA6.3问题清单-生产过程分析
     */
    public static final String TEMPLATE_QUES_P6 = "P6";

    /**
     * VDA6.3问题清单-顾客关怀/顾客满意/服务
     */
    public static final String TEMPLATE_QUES_P7 = "P7";

    /**
     * 全部
     */
    public static final String ALL = "全部";

    /**
     * 供应商-特殊附件类型
     */
    public static final List<String> SUPPLIER_SPECIAL_FILE_TYPE_LIST = new ArrayList<>();

    static {
        SUPPLIER_SPECIAL_FILE_TYPE_LIST.add("质量管理认证");
        SUPPLIER_SPECIAL_FILE_TYPE_LIST.add("客户审核");
        SUPPLIER_SPECIAL_FILE_TYPE_LIST.add("质量管理方法");
    }

    /**
     * VDA6.3模板-产品
     */
    public static final String VDA_TEMPLATE_PRODUCT = "产品";

    /**
     * VDA6.3模板-过程
     */
    public static final String VDA_TEMPLATE_PROCESS = "过程";

    /**
     * 变更管理-影响因素类型
     */
    public static final String CHANGE_FACTOR_TYPE_MAJOR = "重大变更";

    /**
     * 变更管理-影响因素类型
     */
    public static final String CHANGE_FACTOR_TYPE_MINOR = "微小变更";

    /**
     * 仪器校准状态 - 报告待传
     */
    public static final String INSTRU_CALI_STATUS_UPLOAD = "报告待传";

    /**
     * 仪器校准状态 - 已完成
     */
    public static final String INSTRU_CALI_STATUS_FINISHED = "已完成";

    /**
     * 仪器校准状态 - 已超期
     */
    public static final String INSTRU_CALI_STATUS_OVERDUE = "超时未校准";

    /**
     * 时间周期单位 - 年
     */
    public static final String PERIOD_UNIT_YEAR = "年";

    /**
     * 时间周期单位 - 季
     */
    public static final String PERIOD_UNIT_QUARTER = "季";

    /**
     * 时间周期单位 - 月
     */
    public static final String PERIOD_UNIT_MONTH = "月";

    /**
     * 时间周期单位 - 周
     */
    public static final String PERIOD_UNIT_WEEK = "周";

    /**
     * 时间周期单位 - 日
     */
    public static final String PERIOD_UNIT_DAY = "日";

    /**
     * 设备验收-外观分类
     */
    public static final String EQUIP_ACCEPTANCE_APPEARANCE = "外观";

    /**
     * 设备验收-软件资料分类
     */
    public static final String EQUIP_ACCEPTANCE_SOFTWARE_INFO = "软件资料";

    /**
     * 设备验收-运转测试
     */
    public static final String EQUIP_ACCEPTANCE_OPERATIONAL_TEST = "运转测试";

    /**
     * 证书状态 - 生效中
     */
    public static final String CERTIFICATION_STATUS_VALID = "生效中";

    /**
     * 证书状态 - 即将过期
     */
    public static final String CERTIFICATION_STATUS_SOON_TO_EXPIRE = "即将过期";

    /**
     * 证书状态 - 已过期
     */
    public static final String CERTIFICATION_STATUS_OVERDUE = "已过期";

    /**
     * 培训提示类型-认证
     */
    public static final String TRAIN_TIP_TYPE_CERTIFICATION = "证书";

    /**
     * 培训提示类型-日程安排
     */
    public static final String TRAIN_TIP_TYPE_SCHEDULE = "培训计划";

    /**
     * 日程安排状态 - 开放中
     */
    public static final String SCHEDULE_STATUS_OPENED = "开放中";

    /**
     * 日程安排状态 - 已关闭
     */
    public static final String SCHEDULE_STATUS_CLOSED = "已关闭";

    /**
     * 员工认证类型
     */
    public static final List<String> STAFF_CER_TYPE_LIST = new ArrayList<>();

    static {
        STAFF_CER_TYPE_LIST.add("特殊工种证明");
        STAFF_CER_TYPE_LIST.add("三方机构证明");
    }

}
