package org.code4everything.hutool.converter;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import org.code4everything.hutool.Converter;

import java.io.File;

/**
 * @author pantao
 * @since 2020/10/31
 */
public class FileConverter implements Converter<File> {

    @Override
    public File string2Object(String string) {
        return FileUtil.file(string);
    }

    @Override
    public String object2String(Object object) {
        return object instanceof File ? ((File) object).getAbsolutePath() : StrUtil.EMPTY;
    }
}
