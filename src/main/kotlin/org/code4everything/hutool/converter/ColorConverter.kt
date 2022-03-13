package org.code4everything.hutool.converter

import cn.hutool.core.util.StrUtil
import java.awt.Color
import org.code4everything.hutool.Converter

class ColorConverter : Converter<Color> {

    override fun string2Object(string: String): Color {
        if (string.contains(",")) {
            val array = string.split(",").map { it.toInt() }
            if (array.size > 3) {
                // 带alpha通道的RGB
                return Color(array[0], array[1], array[2], array[3])
            }
            if (array.size > 2) {
                // 普通RGB
                return Color(array[0], array[1], array[2])
            }
        }

        // hex格式
        return Color.decode(StrUtil.addPrefixIfNot(string, "#"))
    }

    override fun object2String(any: Any?): String {
        return if (any is Color) any.run {
            val hex = String.format("#%02X%02X%02X", red, green, blue)
            "HEX: $hex, RGB($red,$green,$blue)"
        } else ""
    }
}
