package org.code4everything.hutool.converter

import cn.hutool.core.bean.BeanUtil
import org.code4everything.hutool.Converter

class ObjectPropertyConverter : Converter<Any> {

    override fun string2Object(string: String): Any = TODO("not implemented")

    override fun object2String(any: Any): String = MapConverter().object2String(BeanUtil.beanToMap(any))
}
