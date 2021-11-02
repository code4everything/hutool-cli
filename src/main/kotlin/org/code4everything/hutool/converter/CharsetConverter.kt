package org.code4everything.hutool.converter

import java.nio.charset.Charset
import org.code4everything.hutool.Converter

class CharsetConverter : Converter<Charset> {

    override fun string2Object(string: String): Charset = Charset.forName(string.uppercase())

    override fun object2String(any: Any): String = if (any is Charset) any.name() else ""
}
