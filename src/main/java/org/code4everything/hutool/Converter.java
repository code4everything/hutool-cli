package org.code4everything.hutool;

/**
 * @author pantao
 * @since 2020/10/31
 */
public interface Converter<T> {

    /**
     * convert string to java type
     */
    T string2Object(String string);

    /**
     * convert java object to string
     */
    String object2String(Object object);
}
