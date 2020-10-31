package org.code4everything.hutool;

import com.alibaba.fastjson.JSON;

/**
 * @author pantao
 * @since 2020/10/31
 */
public abstract class JsonConverter<T> implements Converter<T> {

    @Override
    public T string2Object(String string) {
        return JSON.parseObject(string, jsonType());
    }

    @Override
    public String object2String(Object object) {
        return JSON.toJSONString(object, true);
    }

    /**
     * provide json type
     */
    public abstract Class<T> jsonType();
}
