package org.code4everything.hutool.converter;

import cn.hutool.core.util.ArrayUtil;
import org.code4everything.hutool.CliException;
import org.code4everything.hutool.Converter;
import org.code4everything.hutool.Hutool;
import org.code4everything.hutool.MethodArg;
import org.code4everything.hutool.Utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author pantao
 * @since 2021/6/24
 */
public class ArrayConverter implements Converter<Object> {

    private Class<?> componentClass;

    public ArrayConverter(Class<?> arrayClass) {
        setComponentClass(arrayClass);
    }

    public void setComponentClass(Class<?> arrayClass) {
        Objects.requireNonNull(arrayClass);
        componentClass = arrayClass.getComponentType();
        if (componentClass == null || componentClass.isArray()) {
            String msg = "convert array error: only support linear array, cannot convert multidimensional array";
            Hutool.debugOutput(msg);
            throw new CliException(msg);
        }
    }

    @Override
    public Object string2Object(String string) {
        if (Utils.isStringEmpty(string)) {
            return Array.newInstance(componentClass, 0);
        }
        List<String> segments = Arrays.asList(string.split(MethodArg.getSeparator()));
        Object array = Array.newInstance(componentClass, segments.size());
        for (int i = 0; i < segments.size(); i++) {
            Array.set(array, i, Hutool.castParam2JavaType(null, segments.get(i), componentClass, false));
        }
        return array;
    }

    @Override
    public String object2String(Object object) throws Exception {
        if (!ArrayUtil.isArray(object)) {
            return "";
        }
        int length = Array.getLength(object);
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            list.add(Array.get(object, i));
        }
        return ArrayUtil.isArray(object) ? new ListStringConverter().object2String(list) : "";
    }
}
