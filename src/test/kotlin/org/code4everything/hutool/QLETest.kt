package org.code4everything.hutool

import org.junit.Test

class QLETest {

    @Test
    @Throws(Exception::class)
    fun run() {
        println(QLE.run("cmd(\"hu now\").concat(\" 你好\")", false))
    }

    @Test
    fun cmd() {
        println(QLE.cmd("hu now"))
    }
}
