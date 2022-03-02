package org.code4everything.hutool

import com.alibaba.fastjson.JSON
import com.ql.util.express.ExpressRunner
import org.code4everything.hutool.converter.JsonObjectConverter

object JsonFormatter {

    @JvmStatic
    @IOConverter(JsonObjectConverter::class)
    fun format(content: String): Any {
        if (content.isEmpty()) {
            return emptyList<Any>()
        }

        if (content.startsWith("\"") && content.endsWith("\"")) {
            val unwrapped = ExpressRunner().execute(content, null, null, false, false).toString()
            return format(unwrapped)
        }

        return try {
            if (content.startsWith('[')) JSON.parseArray(content) else JSON.parseObject(content)
        } catch (e: Exception) {
            mapOf("error" to "not support format")
        }
    }
}
