package org.code4everything.hutool.converter;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import org.code4everything.hutool.Converter;
import org.code4everything.hutool.Hutool;

import java.util.Date;

/**
 * @author pantao
 * @since 2020/12/19
 */
public class DateConverter implements Converter<DateTime> {

    @Override
    public DateTime string2Object(String string) {
        if (StrUtil.equalsIgnoreCase("now", string)) {
            return DateUtil.date();
        }
        if (StrUtil.equalsIgnoreCase("yesterday", string)) {
            return DateUtil.offsetDay(DateUtil.date(), -1);
        }
        if (StrUtil.equalsIgnoreCase("tomorrow", string)) {
            return DateUtil.offsetDay(DateUtil.date(), 1);
        }
        return DateUtil.parse(string.replace('T', ' '));
    }

    @Override
    public String object2String(Object object) {
        return object instanceof Date ? Hutool.getSimpleDateFormat().format(object) : "";
    }
}
