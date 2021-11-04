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
        fun getConverter(ioConverter: IOConverter, type: Class<*>?): Converter<*>? {
            var converter: Class<out Converter<*>> = ioConverter.value.java
            if (converter == WithoutConverter::class.java) {
                if (ioConverter.className.isNotEmpty()) {
                    converter = Utils.parseClass(ioConverter.className) as Class<out Converter<*>>
                }
                if (converter == WithoutConverter::class.java) {
                    return WithoutConverter(type!!)
                }
            }

            return if (converter == ArrayConverter::class.java) {
                ArrayConverter(type!!)
            } else newConverter(converter, type)
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
