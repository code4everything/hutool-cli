package org.code4everything.hutool

import com.alibaba.fastjson.JSON

object JsonFormatter {

    @JvmStatic
    fun format(content: String): String {
        if (content.isEmpty()) {
            return "{}"
        }

        return try {
            val value = if (content.startsWith('[')) JSON.parseArray(content) else JSON.parseObject(content)
            JSON.toJSONString(value, true)
        } catch (e: Exception) {
            "not support format"
        }
    }
}
