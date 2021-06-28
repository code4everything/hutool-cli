package org.code4everything.hutool.converter;

import org.code4everything.hutool.Converter;

import java.util.HashSet;
import java.util.Set;

/**
 * @author pantao
 * @since 2021/5/11
 */
public class SetStringConverter implements Converter<Set<String>> {

    private final ListStringConverter converter = new ListStringConverter();

    @Override
    public Set<String> string2Object(String string) {
        return new HashSet<>(converter.string2Object(string));
    }

    @Override
    @SuppressWarnings("unchecked")
    public String object2String(Object object) throws Exception {
        return object instanceof Set ? converter.object2String(object) : "";
    }
}
