package org.code4everything.hutool

import org.junit.Test
import org.unix4j.Unix4j
import org.unix4j.unix.sed.SedOptions

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
        println(Unix4j.fromString("hutool").sed(SedOptions.EMPTY, "hu", "our ").toStringResult())
    }
}
