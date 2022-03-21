package org.code4everything.hutool.csv

import cn.hutool.core.text.csv.CsvReadConfig
import cn.hutool.core.text.csv.CsvUtil
import com.alibaba.fastjson.JSONArray
import java.io.FileReader
import java.io.Reader
import java.io.StringReader
import org.code4everything.hutool.IOConverter
import org.code4everything.hutool.converter.FileConverter
import org.code4everything.hutool.converter.JsonObjectConverter

object CSV {

    @JvmStatic
    @IOConverter(JsonObjectConverter::class)
    fun toJson(content: String): JSONArray {
        var csvReader: Reader? = null
        if (content.length < 100 && !content.contains('\n')) {
            val file = FileConverter().string2Object(content)
            if (file.exists()) {
                csvReader = FileReader(file)
            }
        }
        if (csvReader == null) {
            csvReader = StringReader(content)
        }

        val jsonArray = JSONArray()
        val config = CsvReadConfig()
        config.setContainsHeader(true)
        config.setTrimField(true)
        CsvUtil.getReader(csvReader, config).forEach { jsonArray.add(it.fieldMap) }
        return jsonArray
    }
}
