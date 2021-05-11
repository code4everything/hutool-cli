package org.code4everything.hutool;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;

/**
 * @author pantao
 * @since 2021/5/11
 */
public class HutoolAlias {

    public static void main(String[] args) {
        Hutool.workDir = "./hutool";
        String json = FileUtil.readUtf8String(System.getenv("HUTOOL_PATH") + "\\class.json");
        JSON.parseObject(json).forEach((k, v) -> {
            String arg = k + "#alias -d";
            Hutool.main(arg.split(" "));
        });
    }
}
