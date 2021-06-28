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
        ArrayConverter converter = new ArrayConverter(Utils.parseClass("[B").getTypeName(), converterJson);
        Assert.assertArrayEquals(new byte[]{1, 2, 3}, (byte[]) converter.string2Object("1,2,3"));

        converter.setElementClass(Utils.parseClass("[C").getTypeName());
        Assert.assertArrayEquals(new char[]{'1', '2', '3'}, (char[]) converter.string2Object("1,2,3"));

        converter.setElementClass(Utils.parseClass("[Z").getTypeName());
        Assert.assertArrayEquals(new boolean[]{false, true, true}, (boolean[]) converter.string2Object("false,true,true"));

        converter.setElementClass(Utils.parseClass("[S").getTypeName());
        Assert.assertArrayEquals(new short[]{1, 2, 3}, (short[]) converter.string2Object("1,2,3"));

        converter.setElementClass(Utils.parseClass("[I").getTypeName());
        Assert.assertArrayEquals(new int[]{1, 2, 3}, (int[]) converter.string2Object("1,2,3"));

        converter.setElementClass(Utils.parseClass("[J").getTypeName());
        Assert.assertArrayEquals(new long[]{1, 2, 3}, (long[]) converter.string2Object("1,2,3"));

        converter.setElementClass(Utils.parseClass("[F").getTypeName());
        Assert.assertArrayEquals(new float[]{1.1f, 2.2f, 3.3f}, (float[]) converter.string2Object("1.1,2.2,3.3"), 1);

        converter.setElementClass(Utils.parseClass("[D").getTypeName());
        Assert.assertArrayEquals(new double[]{1.1, 2.2, 3.3}, (double[]) converter.string2Object("1.1,2.2,3.3"), 1);

        converter.setElementClass(Utils.parseClass("[Lstring;").getTypeName());
        Assert.assertArrayEquals(new String[]{"a", "b", "c"}, (String[]) converter.string2Object("a,b,c"));

        converter.setElementClass(Utils.parseClass("[Lj.int;").getTypeName());
        Assert.assertArrayEquals(new Integer[]{7, 8, 9}, (Integer[]) converter.string2Object("7,8,9"));

        converter.setElementClass(Utils.parseClass("[Ldate;").getTypeName());
        long now = System.currentTimeMillis();
        Assert.assertArrayEquals(new Date[]{new Date(now)}, (Date[]) converter.string2Object(String.valueOf(now)));
    }
}
