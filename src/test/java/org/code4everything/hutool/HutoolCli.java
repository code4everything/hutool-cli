package org.code4everything.hutool;

/**
 * @author pantao
 * @since 2020/10/30
 */
public class HutoolCli {

    public static void main(String[] args) {
        String testArgs = "methods str -d";
        Hutool.main(testArgs.split(" "));
    }
}
