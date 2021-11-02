package org.code4everything.hutool.converter;

import com.alibaba.fastjson.JSONObject;
import org.code4everything.hutool.Hutool;
import org.code4everything.hutool.Utils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

public class ArrayConverterTest {

    @Test
    public void string2Object() throws Exception {
        JSONObject converterJson = Hutool.getAlias("", "", Hutool.CONVERTER_JSON);
        ArrayConverter converter = new ArrayConverter(Utils.parseClass("[B"));
        Assert.assertArrayEquals(new byte[]{1, 2, 3}, (byte[]) converter.string2Object("1,2,3"));

        converter.setComponentClass(Utils.parseClass("[C"));
        Assert.assertArrayEquals(new char[]{'1', '2', '3'}, (char[]) converter.string2Object("1,2,3"));

        converter.setComponentClass(Utils.parseClass("[Z"));
        Assert.assertArrayEquals(new boolean[]{false, true, true}, (boolean[]) converter.string2Object("false,true,true"));

        converter.setComponentClass(Utils.parseClass("[S"));
        Assert.assertArrayEquals(new short[]{1, 2, 3}, (short[]) converter.string2Object("1,2,3"));

        converter.setComponentClass(Utils.parseClass("[I"));
        Assert.assertArrayEquals(new int[]{1, 2, 3}, (int[]) converter.string2Object("1,2,3"));

        converter.setComponentClass(Utils.parseClass("[J"));
        Assert.assertArrayEquals(new long[]{1, 2, 3}, (long[]) converter.string2Object("1,2,3"));

        converter.setComponentClass(Utils.parseClass("[F"));
        Assert.assertArrayEquals(new float[]{1.1f, 2.2f, 3.3f}, (float[]) converter.string2Object("1.1,2.2,3.3"), 1);

        converter.setComponentClass(Utils.parseClass("[D"));
        Assert.assertArrayEquals(new double[]{1.1, 2.2, 3.3}, (double[]) converter.string2Object("1.1,2.2,3.3"), 1);

        converter.setComponentClass(Utils.parseClass("[Lstring;"));
        Assert.assertArrayEquals(new String[]{"a", "b", "c"}, (String[]) converter.string2Object("a,b,c"));

        converter.setComponentClass(Utils.parseClass("[Lj.int;"));
        Assert.assertArrayEquals(new Integer[]{7, 8, 9}, (Integer[]) converter.string2Object("7,8,9"));

        converter.setComponentClass(Utils.parseClass("[Ldate;"));
        long now = System.currentTimeMillis();
        Assert.assertArrayEquals(new Date[]{new Date(now)}, (Date[]) converter.string2Object(String.valueOf(now)));
    }
}
