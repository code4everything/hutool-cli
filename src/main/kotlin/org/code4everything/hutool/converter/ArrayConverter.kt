package org.code4everything.hutool.converter

import java.lang.reflect.Array
import org.code4everything.hutool.CliException
import org.code4everything.hutool.Converter
import org.code4everything.hutool.Hutool
import org.code4everything.hutool.MethodArg
import org.code4everything.hutool.Utils

class ArrayConverter(arrayClass: Class<*>) : Converter<Any> {

    private val msg = "convert array error: only support linear array, cannot convert multidimensional array"

    private lateinit var componentClass: Class<*>

    fun setComponentClass(arrayClass: Class<*>) {
        componentClass = arrayClass.componentType.also { if (it == null || it.isArray) throw CliException(msg) }
    }

    override fun string2Object(string: String): Any {
        if (Utils.isStringEmpty(string)) {
            return Array.newInstance(componentClass, 0)
        }
        val segments = string.split(MethodArg.separator)
        return Array(segments.size) { i -> Hutool.castParam2JavaType(null, segments[i], componentClass, false) }
    }

    override fun object2String(any: Any?): String {
        return if (any is kotlin.Array<*> || any is Array) convert(any) else ""
    }

    private fun convert(any: Any): String {
        val list = List(Array.getLength(any)) { i -> Array.get(any, i) }
        return ListStringConverter().object2String(list)
    }

    init {
        setComponentClass(arrayClass)
    }
}
