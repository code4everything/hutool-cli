package org.code4everything.hutool;

import cn.hutool.core.date.DateTime;
import org.code4everything.hutool.converter.DateConverter;

/**
 * @author pantao
 * @since 2021/7/13
 */
public class Countdown {

    public static String countdown(@IOConverter long number, String unit) {
        DateTime dateTime = new DateTime(0);
        long count = new DateConverter().getOffsetDate(dateTime, number + unit).getTime();

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
