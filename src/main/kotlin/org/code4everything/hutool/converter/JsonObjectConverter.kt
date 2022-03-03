package org.code4everything.hutool.converter

import cn.hutool.core.io.FileUtil
import com.alibaba.fastjson.JSON
import org.code4everything.hutool.Converter

class JsonObjectConverter(private val type: Class<*>) : Converter<Any> {

    override fun string2Object(string: String): Any {
        if (string.startsWith("{") || string.startsWith("]")) {
            return JSON.parseObject(string, type)
        }

        val file = FileConverter().string2Object(string)
        return JSON.parseObject(FileUtil.readUtf8String(file))
    }

    override fun object2String(any: Any?): String = JSON.toJSONString(any, true)
}
