package org.code4everything.hutool;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;

/**
 * @author pantao
 * @since 2021/5/11
 */
public class HutoolAlias {

    public static void main(String[] args) {
        String json = FileUtil.readUtf8String(Hutool.workDir + "\\class.json");
        JSON.parseObject(json).forEach((k, v) -> {
            String arg = k + "#alias -d";
            Hutool.main(arg.split(" "));
            System.out.println();
        });
    }
}
