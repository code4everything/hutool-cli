package org.code4everything.hutool.converter;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import org.code4everything.hutool.Converter;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author pantao
 * @since 2021/5/11
 */
public class SetStringConverter implements Converter<Set<String>> {

    @Override
    public Set<String> string2Object(String string) {
        if (StrUtil.isEmpty(string)) {
            return Collections.emptySet();
        }
        return new HashSet<>(StrUtil.splitTrim(StrUtil.strip(string, "[", "]"), ","));
    }

    @Override
    public String object2String(Object object) {
        return object instanceof List ? JSON.toJSONString(object) : ObjectUtil.toString(object);
    }
}