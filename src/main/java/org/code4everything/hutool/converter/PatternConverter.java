package org.code4everything.hutool.converter;

import cn.hutool.core.util.StrUtil;
import org.code4everything.hutool.Converter;

import java.util.regex.Pattern;

/**
 * @author pantao
 * @since 2020/10/31
 */
public class PatternConverter implements Converter<Pattern> {

    @Override
    public Pattern string2Object(String string) {
        return Pattern.compile(string);
    }

    @Override
    public String object2String(Object object) {
        return object instanceof Pattern ? ((Pattern) object).pattern() : StrUtil.EMPTY;
    }
}
