package org.code4everything.hutool

import cn.hutool.core.comparator.ComparatorChain
import cn.hutool.core.date.DateUtil
import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.ArrayUtil
import java.io.File
import java.util.Arrays
import java.util.Date
import java.util.stream.Collectors
import org.code4everything.hutool.converter.FileConverter
import org.code4everything.hutool.converter.LineSepConverter

object FileList {

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

        val fileList = files!!.filter { !it.isHidden && (!it.isDirectory || !it.name.startsWith(".")) }.stream()
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

    @JvmStatic
    @IOConverter(LineSepConverter::class)
    fun listFiles(@IOConverter(FileConverter::class) file: File): List<String> {
        if (!FileUtil.exist(file)) {
            return listOf("file not found!")
        }

        if (FileUtil.isFile(file)) {
            val date = DateUtil.formatDateTime(Date(file.lastModified()))
            val size = FileUtil.readableFileSize(file)
            return listOf("$date  $size  ${file.name}")
        }

        val filter = MethodArg.getSubParams(Hutool.ARG, 1)
        val files = file.listFiles { _, name ->
            Utils.isCollectionEmpty(filter) || filter.stream().anyMatch { name.lowercase().contains(it) }
        }

        if (Utils.isArrayEmpty(files)) {
            return emptyList()
        }

        Arrays.sort(
            files!!, ComparatorChain.of(Comparator.comparingInt { if (it.isDirectory) 0 else 1 },
            Comparator.comparing { it.name },
            Comparator.comparingLong { it.lastModified() })
        )

        val list = arrayListOf<String>()
        var maxLen = 0
        val size = Array(files.size) { "" }
        for (i in files.indices) {
            size[i] = Utils.addSuffixIfNot(FileUtil.readableFileSize(files[i]).replace(" ", ""), "B")
            val last = size[i].length - 2
            if (!Character.isDigit(size[i][last])) {
                // remove 'B' if has K,M...
                size[i] = size[i].substring(0, size[i].length - 1)
            }
            if (size[i].length > maxLen) {
                maxLen = size[i].length
            }
        }

        for (i in files.indices) {
            val date = DateUtil.formatDateTime(Date(files[i].lastModified()))
            val fmtSize = size[i].padStart(maxLen, ' ')
            list.add("$date  $fmtSize  ${files[i].name}")
        }

        return list
    }
}
