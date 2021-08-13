package org.code4everything.hutool;

import org.junit.Test;

public class QLETest {

    @Test
    public void run() throws Exception {
        System.out.println(QLE.run("cmd(\"hu now\").concat(\" 你好\")", false));
    }

    @Test
    public void cmd() {
        System.out.println(QLE.cmd("hu now"));
    }
}
