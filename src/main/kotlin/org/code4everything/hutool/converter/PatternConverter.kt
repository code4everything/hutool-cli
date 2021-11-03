package org.code4everything.hutool.converter

import java.util.regex.Pattern
import org.code4everything.hutool.Converter

class PatternConverter : Converter<Pattern> {

    override fun string2Object(string: String): Pattern = Pattern.compile(string)

    override fun object2String(any: Any?): String = if (any is Pattern) any.pattern() else ""
}
