package org.code4everything.hutool.converter

import java.util.Date
import org.code4everything.hutool.Utils
import org.junit.Assert
import org.junit.Test

class ArrayConverterTest {

    @Test
    @Throws(Exception::class)
    fun string2Object() {
        val converter = ArrayConverter(Utils.parseClass("[B"))
        Assert.assertArrayEquals(byteArrayOf(1, 2, 3), converter.string2Object("1,2,3") as ByteArray)

        converter.setComponentClass(Utils.parseClass("[C"))
        Assert.assertArrayEquals(charArrayOf('1', '2', '3'), converter.string2Object("1,2,3") as CharArray)

        converter.setComponentClass(Utils.parseClass("[Z"))
        var resByBool = converter.string2Object("false,true,true") as BooleanArray
        Assert.assertArrayEquals(booleanArrayOf(false, true, true), resByBool)

        converter.setComponentClass(Utils.parseClass("[S"))
        Assert.assertArrayEquals(shortArrayOf(1, 2, 3), converter.string2Object("1,2,3") as ShortArray)

        converter.setComponentClass(Utils.parseClass("[I"))
        Assert.assertArrayEquals(intArrayOf(1, 2, 3), converter.string2Object("1,2,3") as IntArray)

        converter.setComponentClass(Utils.parseClass("[J"))
        Assert.assertArrayEquals(longArrayOf(1, 2, 3), converter.string2Object("1,2,3") as LongArray)

        converter.setComponentClass(Utils.parseClass("[F"))
        var resByFloat = converter.string2Object("1.1,2.2,3.3") as FloatArray
        Assert.assertArrayEquals(floatArrayOf(1.1f, 2.2f, 3.3f), resByFloat, 1f)

        converter.setComponentClass(Utils.parseClass("[D"))
        var resByDouble = converter.string2Object("1.1,2.2,3.3") as DoubleArray
        Assert.assertArrayEquals(doubleArrayOf(1.1, 2.2, 3.3), resByDouble, 1.0)

        converter.setComponentClass(Utils.parseClass("[Lstring;"))
        Assert.assertArrayEquals(arrayOf("a", "b", "c"), converter.string2Object("a,b,c") as Array<String>)

        converter.setComponentClass(Utils.parseClass("[Lj.int;"))
        Assert.assertArrayEquals(arrayOf(7, 8, 9), converter.string2Object("7,8,9") as Array<Int>)

        converter.setComponentClass(Utils.parseClass("[Ldate;"))
        val now = System.currentTimeMillis()
        Assert.assertArrayEquals(arrayOf(Date(now)), converter.string2Object(now.toString()) as Array<Date>)
    }
}
