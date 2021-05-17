package org.code4everything.hutool.converter;

import cn.hutool.core.date.Week;
import org.code4everything.hutool.Converter;

/**
 * @author pantao
 * @since 2020/10/31
 */
public class WeekConverter implements Converter<Week> {

    @Override
    public Week string2Object(String string) {
        return Week.valueOf(string.toUpperCase());
    }

    @Override
    public String object2String(Object object) {
        return object instanceof Week ? ((Week) object).toChinese() : "";
    }
}
