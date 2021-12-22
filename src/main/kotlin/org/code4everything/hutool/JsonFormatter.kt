package org.code4everything.hutool

import com.alibaba.fastjson.JSON
import com.ql.util.express.ExpressRunner

object JsonFormatter {

    @JvmStatic
    fun format(content: String): String {
        if (content.isEmpty()) {
            return "{}"
        }

        if (content.startsWith("\"") && content.endsWith("\"")) {
            var unwrapped = ExpressRunner().execute(content, null, null, false, false).toString()
            return format(unwrapped)
        }

        return try {
            val value = if (content.startsWith('[')) JSON.parseArray(content) else JSON.parseObject(content)
            JSON.toJSONString(value, true)
        } catch (e: Exception) {
            "not support format"
        }
    }
}
