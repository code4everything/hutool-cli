package org.code4everything.hutool;

import org.junit.Test;

/**
 * @author pantao
 * @since 2020/10/30
 */
public class HutoolCli {

    public static void test(String cmd) {
        //String testArgs = "base64-encode 'test_multi_cmd' // base64-decode \\\\0`";
        //String testArgs = "date2ms now // calc \\\\0/1000";
        //String testArgs = "alias // grep methods \\\\0";
        //String testArgs = "-c cn.hutool.core.codec.Base64 -m encode -p 123456789";
        Hutool.ARG = new MethodArg();
        Hutool.main(cmd.split(" "));
    }

    @Test
    public void getSupperClass() {
        test("suppers string");
        test("suppers java.util.ArrayList");
        test("suppers str");
    }
}
