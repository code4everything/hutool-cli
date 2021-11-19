package org.code4everything.hutool.converter

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.StrUtil
import cn.hutool.http.HttpUtil
import java.util.StringJoiner
import org.code4everything.hutool.Converter
import org.code4everything.hutool.Hutool
import org.code4everything.hutool.MethodArg

class ListStringConverter : Converter<List<String?>> {

    private var directLineSep = false

    fun useLineSep(): ListStringConverter = apply { directLineSep = true }

    override fun string2Object(string: String): List<String> {
        if (string.isEmpty()) {
            return emptyList()
        }

        var str = string
        if (str.startsWith("file:")) {
            val file = FileConverter().string2Object(str.substring(5))
            Hutool.debugOutput("get file path: %s", file.absolutePath)
            if (FileUtil.exist(file)) {
                str = FileUtil.readUtf8String(file)
            }
        } else if (str.startsWith("http:") || str.startsWith("https:")) {
            str = HttpUtil.get(str)
        }

        if (directLineSep) {
            return StrUtil.splitTrim(str, "\n")
        }

        val separator = MethodArg.separator
        if (!separator.contains("\n")) {
            str = StrUtil.strip(str, "[", "]")
        }
        return StrUtil.splitTrim(str, separator)
    }

    override fun object2String(any: Any?): String {
        if (any !is Collection<*>) {
            return ""
        }
        if (any.isEmpty()) {
            return "[]"
        }

        val iterator = any.iterator()
        if (any.size == 1) {
            return Hutool.convertResult(iterator.next(), null)
        }

        val joiner: StringJoiner
        val separator = if (directLineSep) FileUtil.getLineSeparator() else MethodArg.separator
        joiner = if (separator.contains("\n")) StringJoiner(separator) else StringJoiner(",", "[", "]")

        while (iterator.hasNext()) {
            joiner.add(Hutool.convertResult(iterator.next(), null))
        }
        return joiner.toString()
    }
}
