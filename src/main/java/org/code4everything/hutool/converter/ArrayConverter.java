package org.code4everything.hutool.converter;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import org.code4everything.hutool.CliException;
import org.code4everything.hutool.Converter;
import org.code4everything.hutool.Hutool;
import org.code4everything.hutool.Utils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author pantao
 * @since 2021/6/24
 */
public class ArrayConverter implements Converter<Object> {

    private final JSONObject convertJson;

    private Class<?> elementClass;

    public ArrayConverter(String arrayTypeName, JSONObject convertJson) throws Exception {
        setElementClass(arrayTypeName);
        this.convertJson = convertJson;
    }

    public void setElementClass(String arrayTypeName) throws Exception {
        Objects.requireNonNull(arrayTypeName);
        String elementClassName = arrayTypeName.substring(0, arrayTypeName.length() - 2);
        if (elementClassName.endsWith("[]")) {
            String msg = "convert array error: only support linear array, cannot convert multidimensional array";
            Hutool.debugOutput(msg);
            throw new CliException(msg);
        }
        elementClass = Utils.parseClass(elementClassName);
    }

    @Override
    public Object string2Object(String string) {
        if (Utils.isStringEmpty(string)) {
            return Array.newInstance(elementClass, 0);
        }
        List<String> segments = Arrays.asList(string.split(","));
        Object array = Array.newInstance(elementClass, segments.size());
        for (int i = 0; i < segments.size(); i++) {
            Array.set(array, i, Hutool.castParam2JavaType(convertJson, null, segments.get(i), elementClass, false));
        }
        return array;
    }

    @Override
    public String object2String(Object object) {
        return ObjectUtil.toString(object);
    }
}
