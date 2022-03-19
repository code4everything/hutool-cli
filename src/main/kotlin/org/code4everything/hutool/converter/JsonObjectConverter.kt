package org.code4everything.hutool.converter

import cn.hutool.core.io.FileUtil
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.ql.util.express.ExpressRunner
import org.code4everything.hutool.Converter
import org.code4everything.hutool.Hutool

class JsonObjectConverter(private val type: Class<*>) : Converter<Any> {

    override fun string2Object(string: String): Any? {
        if (string.startsWith("{")) {
            return JSON.parseObject(string, type)
        }
        if (string.startsWith("[")) {
            return JSON.parseArray(string, type)
        }
        if (string.startsWith("\"") && string.endsWith("\"")) {
            val unwrapped = ExpressRunner().execute(string, null, null, false, false).toString()
            return string2Object(unwrapped)
        }

        val file = FileConverter().string2Object(string)
        if (!file.exists()) {
            Hutool.debugOutput("文件不存在：" + file.absolutePath)
            return JSONObject()
        }

        val content = FileUtil.readUtf8String(file).trim()
        if (string == content) {
            Hutool.debugOutput("发现循环引用内容：$content")
            return JSONObject()
        }
        return string2Object(content)
    }

    override fun object2String(any: Any?): String = JSON.toJSONString(any, true)
}
