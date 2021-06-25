package org.code4everything.hutool;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrJoiner;
import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author pantao
 * @since 2021/6/25
 */
public class HuCalendar {

    private int month = 0;

    private int currentMonth;

    private DateTime beginDate;

    public HuCalendar(String yearMonth) {
        DateTime now = DateUtil.date();
        beginDate = DateUtil.beginOfMonth(now);
        currentMonth = beginDate.getField(DateField.MONTH);
        int length = StrUtil.length(yearMonth);
        if (length == 4) {
            month = -1;
            beginDate = DateUtil.parse(yearMonth, "yyyy");
        } else if (length == 6) {
            beginDate = DateUtil.parse(yearMonth, "yyyyMM");
        } else if (length == 7) {
            beginDate = DateUtil.parse(yearMonth, "yyyy-MM");
        }

        if (month >= 0) {
            month = beginDate.getField(DateField.MONTH);
        }
    }

    public static String calendar(String[] yearMonths) {
        if (Objects.isNull(yearMonths) || yearMonths.length == 0) {
            return new HuCalendar(null).getCalenderStr();
        }

        StrJoiner joiner = StrJoiner.of("\n\n");
        String previousYear = DateUtil.format(DateUtil.date(), "yyyy");
        for (String yearMonth : yearMonths) {
            int len = StrUtil.length(yearMonth);
            if (len == 0) {
                continue;
            }
            if (len < 3) {
                yearMonth = previousYear + StrUtil.padPre(yearMonth, 2, '0');
            }
            previousYear = yearMonth.substring(0, 4);
            joiner.append(new HuCalendar(yearMonth).getCalenderStr());
        }
        return joiner.toString();
    }

    public String getCalenderStr() {
        if (month < 0) {
            StrJoiner joiner = StrJoiner.of("\n\n");
            for (int i = 0; i < 12; i++) {
                joiner.append(getCalendarStr(DateUtil.offsetMonth(beginDate, i)));
            }
            return joiner.toString();
        } else {
            return getCalendarStr(beginDate);
        }
    }

    private String getCalendarStr(DateTime begin) {
        int week = begin.dayOfWeek();
        week = (week == 0 ? 7 : week) - 1;

        String[] line = null;
        List<String> result = new ArrayList<>();
        if (begin.getField(DateField.MONTH) == currentMonth) {
            result.add("     " + DateUtil.format(DateUtil.date(), "yyyy-MM-dd") + "    ");
        } else {
            result.add("      " + DateUtil.format(begin, "yyyy-MM") + "       ");
        }
        result.add("Mo Tu We Th Fr Sa Su");

        int end = DateUtil.endOfMonth(begin).dayOfMonth();
        for (int start = 1; start <= end; start++) {
            if (Objects.isNull(line)) {
                line = new String[7];
                Arrays.fill(line, "  ");
            }

            line[week] = StrUtil.padPre(String.valueOf(start), 2, ' ');

            if (week >= 6) {
                result.add(StrJoiner.of(" ").append(line).toString());
                line = null;
                week = 0;
            } else {
                week++;
            }
        }

        if (Objects.nonNull(line)) {
            result.add(StrJoiner.of(" ").append(line).toString());
        }
        return StrJoiner.of("\n").append(result).toString();
    }
}
