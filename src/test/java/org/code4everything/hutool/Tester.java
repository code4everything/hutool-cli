package org.code4everything.hutool;

import org.junit.Test;

/**
 * @author pantao
 * @since 2021/6/23
 */
public class Tester {

    @Test
    public void parseClass() throws Exception {
        Class<?> clazz = Utils.parseClass("[I");
        System.out.println(clazz.getName());
        System.out.println(long[].class.getName());
    }
}
