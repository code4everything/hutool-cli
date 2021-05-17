package org.code4everything.hutool.converter;

import org.code4everything.hutool.Converter;

import java.nio.charset.Charset;

/**
 * @author pantao
 * @since 2020/10/31
 */
public class CharsetConverter implements Converter<Charset> {

    @Override
    public Charset string2Object(String string) {
        return Charset.forName(string.toUpperCase());
    }

    @Override
    public String object2String(Object object) {
        return object instanceof Charset ? ((Charset) object).name() : "";
    }
}
