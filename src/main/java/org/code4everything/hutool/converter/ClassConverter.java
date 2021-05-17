package org.code4everything.hutool.converter;

import org.code4everything.hutool.Converter;
import org.code4everything.hutool.Utils;

/**
 * @author pantao
 * @since 2020/10/31
 */
public class ClassConverter implements Converter<Class<?>> {

    @Override
    public Class<?> string2Object(String string) throws Exception {
        return Utils.parseClass(string);
    }

    @Override
    public String object2String(Object object) {
        return object instanceof Class<?> ? ((Class<?>) object).getName() : "";
    }
}
