package org.code4everything.hutool;

import cn.hutool.core.date.DateUnit;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;

/**
 * @author pantao
 * @since 2020/10/30
 */
public class Test {

    public static void main(String[] args) {
        ParserConfig config = new ParserConfig();
        System.out.println(TypeUtils.cast("day", DateUnit.class, config));
    }
}
