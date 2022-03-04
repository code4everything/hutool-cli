package org.code4everything.hutool.converter

import org.junit.Test

class FileConverterTest {

    @Test
    fun string2Object() {
        println(FileConverter().string2Object("~/Downloads"))
    }
}
