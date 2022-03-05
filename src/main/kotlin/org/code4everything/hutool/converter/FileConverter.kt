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
        val map = mapOf(
            "~" to FileUtil.getUserHomePath(),
            "." to Hutool.ARG.workDir,
            "!" to System.getenv("HUTOOL_PATH"),
            "$" to System.getenv("JAVA_HOME")
        )

        for (entry in map.entries) {
            if (entry.key == path) {
                return FileUtil.file(entry.value)
            }
            if (path.startsWith(entry.key + File.separator)) {
                return FileUtil.file(entry.value, path.substring(2))
            }
        }

        if (FileUtil.isAbsolutePath(path)) {
            return FileUtil.file(path)
        }
        return Paths.get(Hutool.ARG.workDir, path).toAbsolutePath().toFile()
    }

    override fun object2String(any: Any?): String = if (any is File) any.absolutePath else ""
}
