package org.code4everything.hutool.converter;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import org.code4everything.hutool.Converter;
import org.code4everything.hutool.Hutool;

import java.util.Objects;

/**
 * @author pantao
 * @since 2020/10/31
 */
public class ClassConverter implements Converter<Class<?>> {

    @Override
    public Class<?> string2Object(String string) throws ClassNotFoundException {
        JSONObject aliasJson = Hutool.getAlias(Hutool.CLASS_JSON);
        JSONObject classJson = aliasJson.getJSONObject(string);
        if (Objects.nonNull(classJson)) {
            String className = classJson.getString(Hutool.CLAZZ_KEY);
            if (StrUtil.isNotEmpty(className)) {
                string = className;
            }
        }

        return Class.forName(string);
    }

    @Override
    public String object2String(Object object) {
        return object instanceof Class<?> ? ((Class<?>) object).getName() : StrUtil.EMPTY;
    }
}
