package org.code4everything.hutool

import java.util.zip.ZipFile
import org.junit.Test

class FileListTest {

    @Test
    fun treeZip() {
        val zipFile = FileList.ZipProp(ZipFile("/Users/vjshi/Desktop/myproj/hutool-cli/hutool/hutool.jar"))
        zipFile.listEntry("org/").forEach { println(it.getName()) }
    }
}
