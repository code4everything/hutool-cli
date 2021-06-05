package org.code4everything.hutool;

import org.junit.Test;

/**
 * @author pantao
 * @since 2020/10/30
 */
public class HutoolCli {

    public static void test(String cmd) {
        Hutool.main(cmd.split(" "));
    }

    @Test
    public void base64() {
        test("-c cn.hutool.core.codec.Base64 -m encode -t j.char.seq -p 123456789");
    }

    @Test
    public void multiCmd() {
        test("base64-encode 'test_multi_cmd' // base64-decode \\\\0");
        test("date2ms now // calc \\\\0/1000");
        test("alias // grep methods \\\\0");
    }

    @Test
    public void calc() {
        test("calc 88/9");
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
        test("value j.int MAX_VALUE");
        test("value j.long MAX_VALUE");
        test("maxint");
        test("maxlong");
    }
}
