package org.code4everything.hutool;

import cn.hutool.core.util.ReflectUtil;
import org.code4everything.hutool.converter.ArrayConverter;

/**
 * @author pantao
 * @since 2020/10/31
 */
public interface Converter<T> {

    @SuppressWarnings("unchecked")
    static Converter<?> getConverter(IOConverter ioConverter, Class<?> type) throws Exception {
        Class<? extends Converter<?>> converter = ioConverter.value();
        if (converter == IOConverter.WithoutConverter.class) {
            if (!Utils.isStringEmpty(ioConverter.className())) {
                converter = (Class<? extends Converter<?>>) Utils.parseClass(ioConverter.className());
            }
            if (converter == IOConverter.WithoutConverter.class) {
                return new IOConverter.WithoutConverter(type);
            }
        }
        if (converter == ArrayConverter.class) {
            return new ArrayConverter(type);
        }
        return newConverter(converter, type);
    }

    static Converter<?> newConverter(Class<? extends Converter<?>> converter, Class<?> type) {
        try {
            return ReflectUtil.newInstance(converter);
        } catch (Exception e) {
            return ReflectUtil.newInstance(converter, type);
        }
    }

    /**
     * convert string to java type
     */
    T string2Object(String string) throws Exception;

    /**
     * convert java object to string
     */
    String object2String(Object object) throws Exception;
}
