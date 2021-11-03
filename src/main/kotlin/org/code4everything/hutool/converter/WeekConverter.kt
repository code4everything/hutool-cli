package org.code4everything.hutool.converter

import cn.hutool.core.date.Week
import org.code4everything.hutool.Converter

class WeekConverter : Converter<Week> {

    override fun string2Object(string: String): Week = Week.valueOf(string.uppercase())

    override fun object2String(any: Any?): String = if (any is Week) any.toChinese() else ""
}
