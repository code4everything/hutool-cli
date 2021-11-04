package org.code4everything.hutool

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.ArrayUtil
import java.io.File
import java.util.stream.Collectors
import org.code4everything.hutool.converter.FileConverter
import org.code4everything.hutool.converter.LineSepConverter

object FileTree {

    @JvmStatic
    @IOConverter(LineSepConverter::class)
    fun treeFile(@IOConverter(FileConverter::class) file: File, maxDepth: Int): List<String> {
        return if (FileUtil.exist(file)) {
            treeFile(file, 1, maxDepth, true)
        } else emptyList()
    }

    private fun treeFile(file: File, currDepth: Int, maxDepth: Int, isLast: Boolean): List<String> {
        if (maxDepth > -1 && currDepth > maxDepth) {
            return emptyList()
        }

        if (file.isFile) {
            val prefix = if (isLast) "└─" else "├─"
            return listOf("$prefix${file.name}")
        }

        val files = file.listFiles()
        if (ArrayUtil.isEmpty(files)) {
            return emptyList()
        }

        val fileList = files.filter { !it.isHidden && (!it.isDirectory || !it.name.startsWith(".")) }.stream()
            .sorted(Comparator.comparing { obj: File -> obj.isDirectory }.reversed()).collect(Collectors.toList())
        if (fileList.isEmpty()) {
            return emptyList()
        }

        val last = fileList.size - 1
        val list: MutableList<String> = ArrayList()
        for (i in 0..last) {
            val f = fileList[i]
            val isLastInner = i == last
            val directory = f.isDirectory
            if (directory) {
                val prefix = if (isLastInner) "└─" else "├─"
                list.add("$prefix${f.name}")
            }

            val innerList = treeFile(f, if (directory) currDepth + 1 else currDepth, maxDepth, isLastInner)
            if (directory) {
                list.addAll(innerList.stream().map { e ->
                    "${if (isLastInner) " " else "│"} $e"
                }.collect(Collectors.toList()))
            } else {
                list.addAll(innerList)
            }
        }

        return list
    }
}
