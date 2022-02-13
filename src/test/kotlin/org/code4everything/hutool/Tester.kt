package org.code4everything.hutool

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
    fun accumulationFund() {
        println(AccumulationFund.calcChengdu("1080*15,1000*10,2500*3"))
    }

    @Test
    fun kotlin() {
        Array(5) { i -> i }.forEach {
            if (it == 3) {
                return@forEach
            }
            println(it)
        }
    }
}
