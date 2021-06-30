package org.code4everything.hutool;

import cn.hutool.core.codec.Base64;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author pantao
 * @since 2020/10/30
 */
public class HutoolCli {

    public static String test(String cmd, Object... params) {
        return Hutool.test(cmd, params);
    }

    @Test
    public void base64() {
        String test = "123456789";
        Assert.assertEquals(Base64.encode(test), test("-c cn.hutool.core.codec.Base64 -m encode -t j.char.seq -p %s", test));

        test = "dd55122a5a5f4a";
        Assert.assertEquals(Base64.encode(test), test("encode64 %s", test));

        test = Base64.encode(test);
        Assert.assertEquals(Base64.decodeStr(test), test("decode64 %s", test));
    }

    @Test
    public void multiCmd() {
        String test = "test_multi_cmd";
        Assert.assertEquals(test, test("encode64 %s // decode64 \\\\0", test));
    }

    @Test
    public void calc() {
        Assert.assertEquals("3", test("calc 9/3"));
    }

    @Test
    public void getSupperClass() {
        test("suppers string");
        test("suppers java.util.ArrayList");
        test("suppers str");
    }

    @Test
    public void externalPath() {
        test("-c org.code4everything.wetool.plugin.support.util.WeUtils -m getCurrentPid");
    }

    @Test
    public void random() {
        test("random");
    }

    @Test
    public void value() {
        Assert.assertEquals(String.valueOf(Integer.MAX_VALUE), test("intmax -d"));
        Assert.assertEquals(String.valueOf(Long.MAX_VALUE), test("longmax"));
        Assert.assertEquals(String.valueOf(Integer.MIN_VALUE), test("value j.int MIN_VALUE"));
        Assert.assertEquals(String.valueOf(Short.MIN_VALUE), test("value j.short MIN_VALUE"));
    }

    @Test
    public void fields() {
        test("fields j.int");
        test("fields string");
        test("fields org.code4everything.hutool.MethodArg");
        test("fields org.code4everything.hutool.Hutool");
    }

    @Test
    public void dayp() {
        test("dayp yesterday");
        test("dayp 2021-06-27T23:59:59");
    }

    @Test
    public void ll() {
        test("ll build.gradle");
        test("ll");
    }

    @Test
    public void calendar() {
        test("calendar");
        test("calendar 4,5");
        test("calendar 202001,2");
        test("calendar 2021");
    }

    @Test
    public void charSequenceUtil$format() {
        test("csu#format -t j.char.seq -t obj.arr {}#{} csu,format");
    }

    @Test
    public void usage() {
        test("");
    }

    @Test
    public void treeFile() {
        test("tree ~ 3");
    }
}
