package org.code4everything.hutool.converter

import org.code4everything.hutool.Converter

class SetStringConverter : Converter<Set<String?>> {

    private val converter = ListStringConverter()

    override fun string2Object(string: String): Set<String> = HashSet(converter.string2Object(string))

    override fun object2String(any: Any): String = if (any is Set<*>) converter.object2String(any) else ""
}
