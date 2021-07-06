package org.code4everything.hutool.converter;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.math.Calculator;
import cn.hutool.core.util.StrUtil;
import org.code4everything.hutool.Converter;
import org.code4everything.hutool.Hutool;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author pantao
 * @since 2020/12/19
 */
public class DateConverter implements Converter<DateTime> {

    private static final Map<String, Integer> offsetMap = new HashMap<>(16, 1);

    static {
        offsetMap.put("ms", 1);
        offsetMap.put("s", 1000);
        offsetMap.put("sec", 1000);
        offsetMap.put("min", 60_000);
        offsetMap.put("h", 3600_000);
        offsetMap.put("d", 86400_000);
        offsetMap.put("day", 86400_000);
    }

    @Override
    public DateTime string2Object(String string) {
        int idx = string.indexOf("+");
        if (idx > 0) {
            DateTime dateTime = parseDate(string.substring(0, idx));
            String offsetStr = string.substring(idx + 1);
            double offset = Calculator.conversion(StrUtil.strip(offsetStr, "(", ")"));
        }
        return parseDate(string);
    }

    private DateTime parseDate(String string) {
        if (StrUtil.equalsIgnoreCase("now", string)) {
            return DateUtil.date();
        }
        if (StrUtil.equalsIgnoreCase("today", string)) {
            return DateUtil.beginOfDay(DateUtil.date());
        }
        if (StrUtil.equalsIgnoreCase("yesterday", string)) {
            return DateUtil.beginOfDay(DateUtil.offsetDay(DateUtil.date(), -1));
        }
        if (StrUtil.equalsIgnoreCase("tomorrow", string)) {
            return DateUtil.beginOfDay(DateUtil.offsetDay(DateUtil.date(), 1));
        }
        return DateUtil.parse(string.replace('T', ' '));
    }

    @Override
    public String object2String(Object object) {
        return object instanceof Date ? Hutool.getSimpleDateFormat().format(object) : "";
    }
}
