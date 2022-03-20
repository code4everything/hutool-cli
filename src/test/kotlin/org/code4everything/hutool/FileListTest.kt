package org.code4everything.hutool

import java.util.regex.Pattern
import java.util.zip.ZipFile
import org.junit.Test

class FileListTest {

    @Test
    fun treeZip() {
        val zipFile = FileList.ZipProp(ZipFile("/Users/vjshi/Desktop/myproj/hutool-cli/hutool/hutool.jar"))
        zipFile.listEntry("org/").forEach { println(it.getName()) }
    }

    @Test
    fun test() {
        println(Pattern.compile("hutool", Pattern.CASE_INSENSITIVE).matcher("HutoolAlias.class").find())
    }
}
