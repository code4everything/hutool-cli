package org.code4everything.hutool.converter;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import org.code4everything.hutool.Converter;

import java.util.Collections;
import java.util.List;

/**
 * @author pantao
 * @since 2020/11/10
 */
public class ListStringConverter implements Converter<List<String>> {

    @Override
    public List<String> string2Object(String string) {
        if (StrUtil.isEmpty(string)) {
            return Collections.emptyList();
        }
        return StrUtil.splitTrim(StrUtil.strip(string, "[", "]"), ",");
    }

    @Override
    public String object2String(Object object) {
        return object instanceof List ? JSON.toJSONString(object) : ObjectUtil.toString(object);
    }
}
