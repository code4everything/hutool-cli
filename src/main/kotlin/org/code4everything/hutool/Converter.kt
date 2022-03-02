package org.code4everything.hutool

import cn.hutool.core.util.ReflectUtil
import org.code4everything.hutool.IOConverter.WithoutConverter
import org.code4everything.hutool.converter.ArrayConverter

interface Converter<T> {

    // convert string to java type
    fun string2Object(string: String): T?

    // convert java object to string
    fun object2String(any: Any?): String

    companion object {

        @JvmStatic
        fun getConverter(converterName: String?, type: Class<*>?): Converter<*>? {
            if (converterName?.isEmpty() != false) {
                return null
            }
            if (converterName == WithoutConverter::class.java.name) {
                return WithoutConverter(type!!)
            }
            if (converterName == ArrayConverter::class.java.name) {
                return ArrayConverter(type!!)
            }
            return newConverter(Utils.parseClass(converterName) as Class<out Converter<*>>, type)
        }

        @JvmStatic
        fun newConverter(converter: Class<out Converter<*>>?, type: Class<*>?): Converter<*>? {
            return try {
                ReflectUtil.newInstance(converter)
            } catch (e: Exception) {
                ReflectUtil.newInstance(converter, type)
            }
        }
    }
}
