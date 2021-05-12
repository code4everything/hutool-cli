package org.code4everything.hutool;

/**
 * @author pantao
 * @since 2020/10/30
 */
public class HutoolCli {

    public static void main(String[] args) {
        String testArgs = "date#alias";
        Hutool.main(testArgs.split(" "));
    }
}
