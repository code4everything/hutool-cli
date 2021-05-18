package org.code4everything.hutool.converter;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import org.code4everything.hutool.Converter;
import org.code4everything.hutool.Hutool;
import org.code4everything.hutool.Utils;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * @author pantao
 * @since 2020/11/10
 */
public class ListStringConverter implements Converter<List<String>> {

    @Override
    public List<String> string2Object(String string) {
        if (Utils.isStringEmpty(string)) {
            return Collections.emptyList();
        }

        if (string.startsWith("file:")) {
            File file = new FileConverter().string2Object(string.substring(5));
            Hutool.debugOutput("get file path: " + file.getAbsolutePath());
            if (FileUtil.exist(file)) {
                string = FileUtil.readUtf8String(file);
            }
        } else if (string.startsWith("http:") || string.startsWith("https:")) {
            string = HttpUtil.get(string);
        }

        char[] chars = string.toCharArray();
        int iterCnt = chars.length >> 1;
        for (int i = 0; i < iterCnt; i++) {
            if (chars[i] == '\n') {
                // has line sep
                return StrUtil.splitTrim(string, "\n");
            }
        }
        return StrUtil.splitTrim(StrUtil.strip(string, "[", "]"), ",");
    }

    @Override
    public String object2String(Object object) {
        return object instanceof List ? JSON.toJSONString(object) : ObjectUtil.toString(object);
    }
}
