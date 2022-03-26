package org.code4everything.hutool

import cn.hutool.core.codec.Base64
import org.junit.Assert
import org.junit.Test

class HutoolCli {

    @Test
    fun base64() {
        var test = "123456789"
        var res = test("-c cn.hutool.core.codec.Base64 -m encode -t j.char.seq -p $test")
        Assert.assertEquals(Base64.encode(test), res)

        test = "dd55122a5a5f4a"
        Assert.assertEquals(Base64.encode(test), test("encode64 $test"))
        test = Base64.encode(test)
        Assert.assertEquals(Base64.decodeStr(test), test("decode64 $test"))
    }

    @Test
    fun alias() {
        test("alias date")
        test("-c alias -p .u")
        test("file.u#alias type")
        test("-c file.u -m alias -p type")
    }

    @Test
    fun multiCmd() {
        val test = "test_multi_cmd"
        Assert.assertEquals(test, test("encode64 $test // decode64 \\\\0"))
    }

    @Test
    fun calc() {
        Assert.assertEquals("3", test("calc 9/3"))
    }

    @Test
    fun getSupperClass() {
        test("suppers string")
        test("suppers java.util.ArrayList")
        test("suppers str.u")
    }

    @Test
    fun externalPath() {
        test("-c org.code4everything.wetool.plugin.support.util.WeUtils -m getCurrentPid -d")
    }

    @Test
    fun random() {
        test("random")
    }

    @Test
    fun value() {
        Assert.assertEquals(Int.MAX_VALUE.toString(), test("intmax -d"))
        Assert.assertEquals(Long.MAX_VALUE.toString(), test("longmax"))
        Assert.assertEquals(Int.MIN_VALUE.toString(), test("value j.int MIN_VALUE"))
        Assert.assertEquals(Short.MIN_VALUE.toString(), test("value j.short MIN_VALUE"))
    }

    @Test
    fun fields() {
        test("fields j.int")
        test("fields string")
        test("fields org.code4everything.hutool.MethodArg")
        test("fields org.code4everything.hutool.Hutool")
    }

    @Test
    fun dayp() {
        test("dayp today+-1d")
        test("dayp 2021-06-27T23:59:59")
    }

    @Test
    fun ll() {
        test("ll build.gradle")
        test("ll")
    }

    @Test
    fun calendar() {
        test("calendar")
        test("calendar 4,5")
        test("calendar 202001,2")
        test("calendar 2021")
    }

    @Test
    fun `charSequenceUtil$format`() {
        test("csu#format -t j.char.seq -t obj.arr {}#{} csu,format")
    }

    @Test
    fun usage() {
        test("")
    }

    @Test
    fun treeFile() {
        test("tree hutool/hutool.jar 2")
    }

    @Test
    fun date() {
        test("date now+(-2+7)d")
    }

    @Test
    fun lower() {
        test("lower QUERY_SPLIT_STATUS -d")
    }

    @Test
    fun echo() {
        test("echo test 123")
    }

    @Test
    fun tojson() {
        test("tojson -a:0")
        test("tojson {\"key\":\"value\"}")
    }

    @Test
    fun md5() {
        test("md5 test")
    }

    @Test
    fun randomColor() {
        test("randomc")
    }

    @Test
    fun figlet() {
        test("figlet hutool-cli")
    }

    @Test
    fun run() {
        test("run join(\",\",list(1,2,3))")
    }

    @Test
    fun open(){
        test("open src")
    }

    companion object {

        fun test(cmd: String, vararg params: String): String = Hutool.test(*(cmd.split(" ").toTypedArray() + params))
    }
}
