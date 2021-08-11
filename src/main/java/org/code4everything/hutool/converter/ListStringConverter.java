package org.code4everything.hutool.converter;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import org.code4everything.hutool.Converter;
import org.code4everything.hutool.Hutool;
import org.code4everything.hutool.MethodArg;
import org.code4everything.hutool.Utils;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;

/**
 * @author pantao
 * @since 2020/11/10
 */
public class ListStringConverter implements Converter<List<String>> {

    public boolean directLineSep = false;

    public ListStringConverter useLineSep() {
        directLineSep = true;
        return this;
    }

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

        if (directLineSep) {
            return StrUtil.splitTrim(string, "\n");
        }

        String separator = MethodArg.getSeparator();
        if (!separator.contains("\n")) {
            string = StrUtil.strip(string, "[", "]");
        }
        return StrUtil.splitTrim(string, separator);
    }

    @Override
    public String object2String(Object object) throws Exception {
        if (!(object instanceof Collection)) {
            return "";
        }

        Collection<?> list = (Collection<?>) object;
        if (Utils.isCollectionEmpty(list)) {
            return "[]";
        }

        Iterator<?> iterator = list.iterator();
        if (list.size() == 1) {
            return Hutool.convertResult(iterator.next(), null);
        }

        StringJoiner joiner;
        String separator = MethodArg.getSeparator();
        if (directLineSep || separator.contains("\n")) {
            joiner = new StringJoiner(separator);
        } else {
            joiner = new StringJoiner(",", "[", "]");
        }

        while (iterator.hasNext()) {
            joiner.add(Hutool.convertResult(iterator.next(), null));
        }
        return joiner.toString();
    }
}
