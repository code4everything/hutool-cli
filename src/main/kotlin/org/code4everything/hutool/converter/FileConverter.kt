package org.code4everything.hutool.converter

import cn.hutool.core.io.FileUtil
import java.io.File
import java.nio.file.Paths
import org.code4everything.hutool.Converter
import org.code4everything.hutool.Hutool
import org.code4everything.hutool.Utils

class FileConverter : Converter<File> {

    override fun string2Object(string: String): File {
        val path = if (Utils.isStringEmpty(string)) "." else string
        when (path) {
            "~" -> return FileUtil.getUserHomeDir()
            "." -> return FileUtil.file(Hutool.ARG.workDir)
            "!" -> return FileUtil.file(System.getenv("HUTOOL_PATH"))
            "$" -> return FileUtil.file(System.getenv("JAVA_HOME"))
        }

        if (path.startsWith("~")) {
            return FileUtil.file(FileUtil.getUserHomePath(), string.substring(1))
        }
        if (path.startsWith(".")) {
            return FileUtil.file(Hutool.ARG.workDir, path.substring(1))
        }
        if (FileUtil.isAbsolutePath(path)) {
            return FileUtil.file(path)
        }
        return Paths.get(Hutool.ARG.workDir, path).toAbsolutePath().toFile()
    }

    override fun object2String(any: Any): String = if (any is File) any.absolutePath else ""
}
