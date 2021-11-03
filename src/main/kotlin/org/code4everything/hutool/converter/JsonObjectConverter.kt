package org.code4everything.hutool.converter

import com.alibaba.fastjson.JSON
import org.code4everything.hutool.Converter

class JsonObjectConverter(private val type: Class<*>) : Converter<Any> {

    override fun string2Object(string: String): Any = JSON.parseObject(string, type)

    override fun object2String(any: Any?): String = JSON.toJSONString(any, true)
}
