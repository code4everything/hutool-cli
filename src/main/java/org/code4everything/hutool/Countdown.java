package org.code4everything.hutool;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.NumberUtil;
import org.code4everything.hutool.converter.DateConverter;

/**
 * @author pantao
 * @since 2021/7/13
 */
public class Countdown {

    public static String countdown(@IOConverter String deadline, String unit) throws Exception {
        DateConverter dateConverter = new DateConverter();
        long count;
        if (NumberUtil.isNumber(deadline)) {
            DateTime dateTime = new DateTime(0);
            count = dateConverter.getOffsetDate(dateTime, deadline + unit).getTime();
        } else {
            DateTime dateTime = dateConverter.string2Object(deadline);
            count = dateTime.getTime() - dateConverter.getBaseDate().getTime();
        }

        if (count <= 0) {
            return "倒计时已过期";
        }

        long ms = count % 1000;
        long sec = count / 1000 % 60;
        long min = count / 1000 / 60 % 60;
        long hour = count / 1000 / 60 / 60 % 24;
        long day = count / 1000 / 60 / 60 / 24;

        String res = "";
        boolean hasStart = day > 0;
        boolean hasEnd = ms > 0;
        if (hasStart) {
            res += day + "天";
        }

        if ((hasStart && hasEnd) || hour > 0) {
            hasStart = true;
            res += hour + "小时";
        }

        if ((hasStart && hasEnd) || min > 0) {
            hasStart = true;
            res += min + "分";
        }

        if ((hasStart && hasEnd) || sec > 0) {
            res += sec + "秒";
        }

        if (hasEnd) {
            res += ms + "毫秒";
        }
        return res;
    }
}
