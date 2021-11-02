package org.code4everything.hutool.converter

import org.code4everything.hutool.Converter

class LineSepConverter : Converter<List<String?>> {

    private val converter = ListStringConverter().useLineSep()

    override fun string2Object(string: String): List<String> = converter.string2Object(string)

    override fun object2String(any: Any): String = converter.object2String(any)
}
