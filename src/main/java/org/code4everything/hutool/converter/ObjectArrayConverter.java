package org.code4everything.hutool.converter;

import cn.hutool.core.util.ObjectUtil;
import org.code4everything.hutool.Converter;
import org.code4everything.hutool.Utils;

import java.util.Arrays;

/**
 * @author pantao
 * @since 2021/6/24
 */
public class ObjectArrayConverter implements Converter<Object[]> {

    @Override
    public Object[] string2Object(String string) throws Exception {
        return Utils.isStringEmpty(string) ? new Object[0] : Arrays.stream(string.split(",")).toArray(Object[]::new);
    }

    @Override
    public String object2String(Object object) {
        return ObjectUtil.toString(object);
    }
}
