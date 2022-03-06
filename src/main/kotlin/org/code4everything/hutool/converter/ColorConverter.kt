package org.code4everything.hutool.converter

import cn.hutool.core.util.StrUtil
import java.awt.Color
import org.code4everything.hutool.Converter

class ColorConverter : Converter<Color> {

    override fun string2Object(string: String): Color = Color.decode(StrUtil.addPrefixIfNot(string, "#"))

    override fun object2String(any: Any?): String {
        return if (any is Color) any.run {
            val hex = String.format("#%02X%02X%02X", red, green, blue)
            "HEX: $hex, RGB($red,$green,$blue)"
        } else ""
    }
}
