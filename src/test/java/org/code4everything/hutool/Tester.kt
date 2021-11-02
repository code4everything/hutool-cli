package org.code4everything.hutool

import org.junit.Test

/**
 * @author pantao
 * @since 2021/6/23
 */
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
}
