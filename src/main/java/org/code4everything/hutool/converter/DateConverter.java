package org.code4everything.hutool.converter;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.math.Calculator;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import org.code4everything.hutool.Converter;
import org.code4everything.hutool.Hutool;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * @author pantao
 * @since 2020/12/19
 */
public class DateConverter implements Converter<DateTime> {

    private static Map<String, DateField> offsetMap = null;

    private static Map<String, String> endMethodMap = null;

    private static Map<String, String> beginMethodMap = null;

    private DateTime baseDate;

    public static Map<String, String> getEndMethodMap() {
        if (Objects.isNull(endMethodMap)) {
            endMethodMap = new HashMap<>(16);
            endMethodMap.put("s", "endOfSecond");
            endMethodMap.put("sec", "endOfSecond");
            endMethodMap.put("min", "endOfMinute");
            endMethodMap.put("h", "endOfHour");
            endMethodMap.put("hour", "endOfHour");
            endMethodMap.put("d", "endOfDay");
            endMethodMap.put("day", "endOfDay");
            endMethodMap.put("w", "endOfWeek");
            endMethodMap.put("week", "endOfWeek");
            endMethodMap.put("m", "endOfMonth");
            endMethodMap.put("mon", "endOfMonth");
            endMethodMap.put("month", "endOfMonth");
            endMethodMap.put("y", "endOfYear");
            endMethodMap.put("year", "endOfYear");
        }
        return endMethodMap;
    }

    public static Map<String, String> getBeginMethodMap() {
        if (Objects.isNull(beginMethodMap)) {
            beginMethodMap = new HashMap<>(16);
            beginMethodMap.put("s", "beginOfSecond");
            beginMethodMap.put("sec", "beginOfSecond");
            beginMethodMap.put("min", "beginOfMinute");
            beginMethodMap.put("h", "beginOfHour");
            beginMethodMap.put("hour", "beginOfHour");
            beginMethodMap.put("d", "beginOfDay");
            beginMethodMap.put("day", "beginOfDay");
            beginMethodMap.put("w", "beginOfWeek");
            beginMethodMap.put("week", "beginOfWeek");
            beginMethodMap.put("m", "beginOfMonth");
            beginMethodMap.put("mon", "beginOfMonth");
            beginMethodMap.put("month", "beginOfMonth");
            beginMethodMap.put("y", "beginOfYear");
            beginMethodMap.put("year", "beginOfYear");
        }
        return beginMethodMap;
    }

    private static Map<String, DateField> getOffsetMap() {
        if (Objects.isNull(offsetMap)) {
            offsetMap = new HashMap<>(16);
            offsetMap.put("ms", DateField.MILLISECOND);
            offsetMap.put("s", DateField.SECOND);
            offsetMap.put("sec", DateField.SECOND);
            offsetMap.put("min", DateField.MINUTE);
            offsetMap.put("h", DateField.HOUR);
            offsetMap.put("hour", DateField.HOUR);
            offsetMap.put("d", DateField.DAY_OF_YEAR);
            offsetMap.put("day", DateField.DAY_OF_YEAR);
            offsetMap.put("w", DateField.WEEK_OF_YEAR);
            offsetMap.put("week", DateField.WEEK_OF_YEAR);
            offsetMap.put("m", DateField.MONTH);
            offsetMap.put("mon", DateField.MONTH);
            offsetMap.put("month", DateField.MONTH);
            offsetMap.put("y", DateField.YEAR);
            offsetMap.put("year", DateField.YEAR);
        }
        return offsetMap;
    }

    /**
     * 仅解析后，未经计算的时间
     */
    public DateTime getBaseDate() {
        return baseDate;
    }

    @Override
    public DateTime string2Object(String string) throws Exception {
        int idx = string.indexOf("+");
        if (idx > 0) {
            DateTime dateTime = parseDate(string.substring(0, idx));
            String offsetStr = string.substring(idx + 1);
            return getOffsetDate(dateTime, offsetStr);
        }

        idx = string.indexOf("-");
        if (idx > 0) {
            DateTime dateTime = parseDate(string.substring(0, idx));
            String offsetStr = string.substring(idx);
            return getOffsetDate(dateTime, offsetStr);
        }

        idx = string.indexOf(">");
        if (idx > 0) {
            DateTime dateTime = parseDate(string.substring(0, idx));
            String methodName = getEndMethodMap().getOrDefault(string.substring(idx + 1), "date");
            return (DateTime) ReflectUtil.getPublicMethod(DateUtil.class, methodName, Date.class).invoke(null, dateTime);
        }

        idx = string.indexOf("<");
        if (idx > 0) {
            DateTime dateTime = parseDate(string.substring(0, idx));
            String methodName = getBeginMethodMap().getOrDefault(string.substring(idx + 1), "date");
            return (DateTime) ReflectUtil.getPublicMethod(DateUtil.class, methodName, Date.class).invoke(null, dateTime);
        }

        return parseDate(string);
    }

    public DateTime getOffsetDate(DateTime dateTime, String offsetStr) {
        for (Entry<String, DateField> entry : getOffsetMap().entrySet()) {
            if (offsetStr.endsWith(entry.getKey())) {
                int offset = (int) Calculator.conversion(StrUtil.removeSuffix(offsetStr, entry.getKey()));
                return dateTime.offset(entry.getValue(), offset);
            }
        }
        return dateTime.offset(DateField.MILLISECOND, (int) Calculator.conversion(offsetStr));
    }

    private DateTime parseDate(String string) {
        baseDate = null;
        if ("now".equals(string)) {
            baseDate = DateUtil.date();
        } else if ("today".equals(string)) {
            baseDate = DateUtil.beginOfDay(DateUtil.date());
        } else if (string.length() == 2) {
            string = DateUtil.format(DateUtil.date(), "yyyy-MM-") + string;
        } else if (string.length() == 5) {
            char sep = string.charAt(2);
            if (sep == '-') {
                string = DateUtil.format(DateUtil.date(), "yyyy-") + string;
            } else if (sep == ':') {
                string = DateUtil.format(DateUtil.date(), "yyyy-MM-dd ") + string;
            }
        }

        if (baseDate == null) {
            baseDate = DateUtil.parse(string.replace('T', ' '));
        }
        return baseDate;
    }

    @Override
    public String object2String(Object object) {
        return object instanceof Date ? Hutool.getSimpleDateFormat().format(object) : "";
    }
}
