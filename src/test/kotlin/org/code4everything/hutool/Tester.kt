package org.code4everything.hutool

import cn.hutool.core.date.DateUtil
import org.junit.Test

class Tester {

    @Test
    fun parseClass() {
        println(ByteArray::class.java.name)
        println(BooleanArray::class.java.name)
        println(ShortArray::class.java.name)
        println(IntArray::class.java.name)
        println(LongArray::class.java.name)
        println(FloatArray::class.java.name)
        println(DoubleArray::class.java.name)
        println(CharArray::class.java.name)
    }

    @Test
    fun kotlin() {
        println(DateUtil.parse("2021-06-27 23:59:59"))
    }
}
