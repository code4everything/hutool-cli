package org.code4everything.hutool.converter;

import cn.hutool.core.bean.BeanUtil;
import org.code4everything.hutool.Converter;

/**
 * @author pantao
 * @since 2020/11/1
 */
public class ObjectPropertyConverter implements Converter<Object> {

    @Override
    public Object string2Object(String string) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public String object2String(Object object) {
        MapConverter mapConverter = new MapConverter();
        return mapConverter.object2String(BeanUtil.beanToMap(object));
    }
}
