package org.code4everything.hutool.converter;

import cn.hutool.core.io.FileUtil;
import org.code4everything.hutool.Converter;
import org.code4everything.hutool.Hutool;

import java.io.File;

/**
 * @author pantao
 * @since 2020/10/31
 */
public class FileConverter implements Converter<File> {

    @Override
    public File string2Object(String string) {
        return FileUtil.isAbsolutePath(string) ? FileUtil.file(string) : FileUtil.file(Hutool.ARG.workDir, string);
    }

    @Override
    public String object2String(Object object) {
        return object instanceof File ? ((File) object).getAbsolutePath() : "";
    }
}
