package org.code4everything.hutool.converter

import cn.hutool.core.util.StrUtil
import java.awt.Color
import org.code4everything.hutool.CliException
import org.code4everything.hutool.Converter

class ColorConverter : Converter<Color> {

    override fun string2Object(string: String): Color {
        if (string.contains(",")) {
            val array = string.split(",").filter { it.isNotEmpty() }.map { it.trim().toInt() }
            if (array.size > 3) {
                // 带alpha通道的RGB
                return Color(array[0], array[1], array[2], array[3])
            }
            if (array.isNotEmpty()) {
                // 普通RGB
                return Color(array[0], array.getLastNonNull(1), array.getLastNonNull(2))
            }
        }

        // hex格式
        return Color.decode(StrUtil.addPrefixIfNot(string, "#"))
    }


    override fun object2String(any: Any?): String {
        return if (any is Color) any.run {
            val hex = String.format("#%02X%02X%02X", red, green, blue)
            "HEX: $hex  RGB($red,$green,$blue)"
        } else ""
    }


    private fun List<Int>.getLastNonNull(idx: Int): Int {
        for (i in 0..idx) {
            val value = getOrNull(idx - i)
            if (value != null) {
                return value
            }
        }
        throw CliException("unknown error")
    }
}
