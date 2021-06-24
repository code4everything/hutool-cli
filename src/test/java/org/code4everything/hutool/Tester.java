package org.code4everything.hutool;

import org.junit.Test;

/**
 * @author pantao
 * @since 2021/6/23
 */
public class Tester {

    @Test
    public void parseClass() {
        System.out.println(byte[].class.getName());
        System.out.println(boolean[].class.getName());
        System.out.println(short[].class.getName());
        System.out.println(int[].class.getName());
        System.out.println(long[].class.getName());
        System.out.println(float[].class.getName());
        System.out.println(double[].class.getName());
        System.out.println(char[].class.getName());
    }
}
