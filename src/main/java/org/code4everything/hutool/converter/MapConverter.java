package org.code4everything.hutool.converter;

import cn.hutool.core.lang.Holder;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import org.code4everything.hutool.Converter;

import java.util.*;

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
            List<String> kvs = StrUtil.splitTrim(token, '=');
            String key = kvs.get(0);
            String value = "";
            if (kvs.size() > 1) {
                value = kvs.get(1);
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
