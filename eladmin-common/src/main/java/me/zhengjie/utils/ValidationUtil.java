/*
 *  Copyright 2019-2020 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package me.zhengjie.utils;

import cn.hutool.core.util.ObjectUtil;
import me.zhengjie.exception.BadRequestException;
import org.apache.poi.ss.formula.functions.T;
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 验证工具
 *
 * @author Zheng Jie
 * @date 2018-11-23
 */
public class ValidationUtil {

    private static volatile int Guid = 100;

    public static String getGuid() {

        ValidationUtil.Guid += 1;

        long now = System.currentTimeMillis();
        //获取4位年份数字
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
        //获取时间戳
        String time = dateFormat.format(now);
        String info = now + "";
        //获取三位随机数
        //int ran=(int) ((Math.random()*9+1)*100);
        //要是一段时间内的数据量过大会有重复的情况，所以做以下修改
        int ran = 0;
        if (ValidationUtil.Guid > 999) {
            ValidationUtil.Guid = 100;
        }
        ran = ValidationUtil.Guid;

        return time + info.substring(2, info.length()) + ran;

    }

    public static String transToDate(Timestamp time) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(time);
    }

    public static Long initGuid() {

        return System.currentTimeMillis();

    }

    /**
     * 验证空
     */
    public static void isNull(Object obj, String entity, String parameter, Object value) {
        if (ObjectUtil.isNull(obj)) {
            String msg = entity + " 不存在: " + parameter + " is " + value;
            throw new BadRequestException(msg);
        }
    }


    /**
     * 验证字段为空
     */
    public static boolean isBlank(String str) {
        return str == null || str.equals("");
    }

    /**
     * 验证列表为空
     */
    public static boolean isEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    /**
     * 验证列表不为空
     */
    public static boolean isNotEmpty(List<?> list) {
        return list != null && !list.isEmpty();
    }

    /**
     * 验证是否为邮箱
     */
    public static boolean isEmail(String email) {
        return new EmailValidator().isValid(email, null);
    }

    public static String getDuration(Timestamp createTime) {
        // 确认结案，计算时长
        String duration = "1天";
        Date now = new Date();
        long diff = now.getTime() - createTime.getTime();
        // long diffSeconds = diff / 1000 % 60; //秒
        // long diffMinutes = diff / (60 * 1000) % 60; //分
        // long diffHours = diff / (60 * 60 * 1000) % 24; //时
        // 若是当天完成，则设置为1天
            /*int closeDuration = (int) (diff / (24 * 60 * 60 * 1000));//天
            if (closeDuration == 0) {
                closeDuration = (int) (diff / (60 * 60 * 1000) % 24);//时
                if (closeDuration == 0) {
                    closeDuration = (int) (diff / (60 * 1000) % 60) == 0 ? 1 : (int) (diff / (60 * 1000) % 60);
                    duration = closeDuration + "分钟";
                } else {
                    duration = closeDuration + "小时";
                }
            } else {
                duration = closeDuration + "天";
            }*/
        int closeDuration = (int) (diff / (24 * 60 * 60 * 1000)) == 0 ? 1 : (int) (diff / (24 * 60 * 60 * 1000));
        duration = closeDuration + "天";
        return duration;
    }

    public static boolean isEquals(Object val1, Object val2) {
        boolean b = false;
        if (val1 != null) {
            if (val2 == null) {
                b = true;
            } else if (val1.toString().equals(val2.toString())) {
                b = true;
            }
        } else {
            b = val2 == null;
        }
        return b;
    }
}
