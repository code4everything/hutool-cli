package org.code4everything.hutool.converter;

import com.alibaba.fastjson.JSON;
import org.code4everything.hutool.Converter;

/**
 * @author pantao
 * @since 2020/10/31
 */
public class JsonObjectConverter implements Converter<Object> {

    private final Class<?> type;

    public JsonObjectConverter(Class<?> type) {
        this.type = type;
    }

    @Override
    public Object string2Object(String string) {
        return JSON.parseObject(string, type);
    }

    @Override
    public String object2String(Object object) {
        return JSON.toJSONString(object, true);
    }
}
