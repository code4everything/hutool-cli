package org.code4everything.hutool.converter;

import cn.hutool.core.lang.Holder;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import org.code4everything.hutool.Converter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * @author pantao
 * @since 2020/11/1
 */
public class MapConverter implements Converter<Map<Object, Object>> {

    @Override
    public Map<Object, Object> string2Object(String string) {
        Map<Object, Object> map = new HashMap<>(16);
        if (StrUtil.isEmpty(string)) {
            return map;
        }
        StringTokenizer tokenizer = new StringTokenizer(string, ",");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (StrUtil.isEmpty(token)) {
                continue;
            }

            int idx = token.indexOf('=');
            String key;
            String value = "";

            if (idx < 1) {
                key = token;
            } else {
                key = StrUtil.sub(token, 0, idx).trim();
                value = StrUtil.sub(token, idx + 1, token.length()).trim();
            }

            map.put(key, value);
        }
        return map;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String object2String(Object object) {
        if (!(object instanceof Map)) {
            return StrUtil.EMPTY;
        }

        Map<Object, Object> map = (Map<Object, Object>) object;
        Holder<Integer> maxLen = Holder.of(0);
        Map<String, String> tempMap = new TreeMap<>();
        map.forEach((k, v) -> {
            if (Objects.isNull(k)) {
                return;
            }
            String key = k.toString();
            if (key.length() > maxLen.get()) {
                maxLen.set(key.length());
            }
            tempMap.put(key, ObjectUtil.toString(v));
        });

        StringJoiner joiner = new StringJoiner("\n");
        tempMap.forEach((k, v) -> joiner.add(StrUtil.padAfter(k, maxLen.get(), ' ') + " = " + v));
        return joiner.toString();
    }
}
