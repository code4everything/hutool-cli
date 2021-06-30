package org.code4everything.hutool.converter;

import cn.hutool.core.io.FileUtil;
import org.code4everything.hutool.Converter;
import org.code4everything.hutool.Hutool;

import java.io.File;
import java.nio.file.Paths;

/**
 * @author pantao
 * @since 2020/10/31
 */
public class FileConverter implements Converter<File> {

    @Override
    public File string2Object(String string) {
        if ("~".equals(string)) {
            return FileUtil.getUserHomeDir();
        }
        if ("!".equals(string)) {
            return FileUtil.file(System.getenv("HUTOOL_PATH"));
        }
        if ("$".equals(string)) {
            return FileUtil.file(System.getenv("JAVA_HOME"));
        }
        if (".".equals(string)) {
            return FileUtil.file(Hutool.ARG.workDir);
        }
        return FileUtil.isAbsolutePath(string) ? FileUtil.file(string) : Paths.get(Hutool.ARG.workDir, string).toAbsolutePath().toFile();
    }

    @Override
    public String object2String(Object object) {
        return object instanceof File ? ((File) object).getAbsolutePath() : "";
    }
}
