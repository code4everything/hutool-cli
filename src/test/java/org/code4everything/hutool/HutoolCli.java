package org.code4everything.hutool;

/**
 * @author pantao
 * @since 2020/10/30
 */
public class HutoolCli {

    public static void main(String[] args) {
        //String testArgs = "base64-encode 'test_multi_cmd' // base64-decode \\\\0`";
        //String testArgs = "date2ms now // calc \\\\0/1000";
        //String testArgs = "alias // grep methods \\\\0";
        String testArgs = "-c cn.hutool.core.codec.Base64 -m encode -p 123456789";
        Hutool.main(testArgs.split(" "));
    }
}
