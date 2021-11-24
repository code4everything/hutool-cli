package org.code4everything.hutool.converter

import cn.hutool.core.util.ObjectUtil
import java.util.StringJoiner
import java.util.StringTokenizer
import java.util.TreeMap
import org.code4everything.hutool.Converter
import org.code4everything.hutool.MethodArg

class MapConverter : Converter<Map<Any?, Any?>> {

    override fun string2Object(string: String): Map<Any?, Any?> {
        val map: MutableMap<Any?, Any?> = HashMap(16)
        if (string.isEmpty()) {
            return map
        }

        val tokenizer = StringTokenizer(string, MethodArg.separator)
        while (tokenizer.hasMoreTokens()) {
            val token = tokenizer.nextToken()
            if (token.isEmpty()) {
                continue
            }
            val idx = token.indexOf('=')
            map[token] = ""
            if (idx > 1) {
                map[token.substring(0, idx).trim()] = token.substring(idx + 1).trim()
            }
        }
        return map
    }

    override fun object2String(any: Any?): String {
        if (any !is Map<*, *>) {
            return ""
        }

        var maxLen = 0
        val tempMap: MutableMap<String, String> = TreeMap()
        any.forEach { (k: Any?, v: Any?) ->
            k?.run { toString() }?.also {
                if (it.length > maxLen) {
                    maxLen = it.length
                }
                tempMap[it] = ObjectUtil.toString(v)
            }
        }

        val joiner = StringJoiner("\n")
        tempMap.forEach { (k, v) -> joiner.add("${k.padEnd(maxLen, ' ')} = $v") }
        return joiner.toString()
    }
}
