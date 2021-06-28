package org.code4everything.hutool.converter;

import org.code4everything.hutool.Converter;

import java.util.List;

/**
 * @author pantao
 * @since 2021/6/28
 */
public class LineStringConverter implements Converter<List<String>> {

    private final ListStringConverter converter = new ListStringConverter().useLineSep();

    @Override
    public List<String> string2Object(String string) {
        return converter.string2Object(string);
    }

    @Override
    public String object2String(Object object) throws Exception {
        return converter.object2String(object);
    }
}
