package org.code4everything.hutool.converter

import org.code4everything.hutool.Converter
import org.code4everything.hutool.Utils

class ClassConverter : Converter<Class<*>> {

    override fun string2Object(string: String): Class<*> = Utils.parseClass(string)

    override fun object2String(any: Any): String = if (any is Class<*>) any.name else ""
}
