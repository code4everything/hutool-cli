package org.code4everything.hutool

import cn.hutool.core.comparator.ComparatorChain
import cn.hutool.core.date.DateUtil
import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.StrUtil
import com.alibaba.fastjson.JSON
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.util.Arrays
import java.util.Date
import java.util.regex.Pattern
import java.util.stream.Collectors
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import org.code4everything.hutool.converter.DateConverter
import org.code4everything.hutool.converter.FileConverter
import org.code4everything.hutool.converter.LineSepConverter

object FileList {

    private val dateConverter by lazy { DateConverter() }

    private val now by lazy { Date() }

    @JvmStatic
    @IOConverter(LineSepConverter::class)
    @HelpInfo([
        "example: '.' 'name' 'file'", "",
        "param1: the source folder",
        "param2: the filter pattern, support regex", "",
        "option params: file dir hidden ignoreempty depth:9 ctime+3m utime+12h atime+1d",
        "file: just find file, dir: just find dir",
        "hidden: contains hidden file, ignoreempty: ignore empty folder or empty file",
        "depth: the max depth to recursion, time: create time, update time, access time"
    ])
    fun find(parent: File, name: String): List<String> {
        val arrayOf = arrayOf("")
        val filter = FileFindFilter()
        filter.nameFilter = Pattern.compile(name, Pattern.CASE_INSENSITIVE)

        val params = Hutool.ARG.params.subList(2, Hutool.ARG.params.size)
        val onlyFile = params.remove("file")
        val onlyDir = params.remove("dir")

        filter.ignoreEmpty = params.remove("ignoreempty")
        filter.findHidden = params.remove("hidden")
        if (onlyFile || onlyDir) {
            filter.hasFile = onlyFile
            filter.hasDir = onlyDir
        }

        params.forEach {
            if (it.startsWith("ctime")) {
                filter.createTimeFilter = it
            } else if (it.startsWith("atime")) {
                filter.accessTimeFilter = it
            } else if (it.startsWith("utime")) {
                filter.updateTimeFilter = it
            } else if (it.startsWith("depth")) {
                filter.depth = it.removePrefix("depth").removePrefix(":").toInt()
            }
        }

        Hutool.debugOutput("filter condition: " + JSON.toJSONString(filter))
        return find(parent, filter, 0)
    }

    private fun find(parent: File, filter: FileFindFilter, depth: Int): List<String> {
        if (depth > filter.depth) {
            return emptyList()
        }
        if (filter.ignoreEmpty && parent.length() == 0L) {
            return emptyList()
        }
        if (parent.isHidden && !filter.findHidden) {
            return emptyList()
        }

        if (parent.isDirectory) {
            if (filter.ignoreEmpty && parent.listFiles()?.isEmpty() != false) {
                return emptyList()
            }
            Hutool.debugOutput("find from: " + parent.absolutePath)
        }

        val matched = filter.nameFilter.matcher(parent.name).find()
        if (matched && ((filter.hasFile && parent.isFile) || (filter.hasDir && parent.isDirectory))) {
            val attributes by lazy { Files.readAttributes(parent.toPath(), BasicFileAttributes::class.java) }
            val timeFilters = java.util.ArrayList<String>()
            if (filter.createTimeFilter.startsWith("ctime")) {
                val dateFormat = attributes.creationTime().toMillis().toString() + filter.createTimeFilter.substring(5)
                timeFilters.add(dateFormat)
            }
            if (filter.accessTimeFilter.startsWith("atime")) {
                val dateFormat = attributes.lastAccessTime().toMillis().toString() + filter.accessTimeFilter.substring(5)
                timeFilters.add(dateFormat)
            }
            if (filter.updateTimeFilter.startsWith("utime")) {
                val dateFormat = parent.lastModified().toString() + filter.updateTimeFilter.substring(5)
                timeFilters.add(dateFormat)
            }
            if (timeFilters.stream().anyMatch { dateConverter.string2Object(it).before(now) }) {
                return emptyList()
            }
            return listOf(parent.absolutePath)
        }

        if (!parent.isDirectory) {
            return emptyList()
        }
        return parent.listFiles()?.flatMap {
            if (!filter.hasFile && it.isFile) emptyList() else find(it, filter, depth + 1)
        }?.toList() ?: emptyList()
    }

    @JvmStatic
    @IOConverter(LineSepConverter::class)
    @HelpInfo([
        "example: '.' '3'", "",
        "param1: the parent file, support zip file",
        "param2: the max depth to recursion"
    ])
    fun treeFile(@IOConverter(FileConverter::class) file: File, maxDepth: Int): List<String> {
        if (!FileUtil.exist(file)) {
            return emptyList()
        }
        if (FileUtil.isDirectory(file)) {
            return treeFile(DocFileProp(file), 1, maxDepth, true)
        }
        if (file.name.endsWith(".jar") || file.name.endsWith(".zip")) {
            val zip = ZipFile(file)
            return treeFile(ZipProp(zip), 1, maxDepth, true)
        }
        return listOf(file.name)
    }

    private fun treeFile(file: FileProp, currDepth: Int, maxDepth: Int, isLast: Boolean): List<String> {
        if (maxDepth > -1 && currDepth > maxDepth) {
            return emptyList()
        }

        if (file.isFile()) {
            val prefix = if (isLast) "└─" else "├─"
            return listOf("$prefix${file.getName()}")
        }

        val files = file.listEntries()
        if (files.isEmpty()) {
            return emptyList()
        }

        val fileList = files.filter { !it.isHidden() && (!it.isDirectory() || !it.getName().startsWith(".")) }.stream()
            .sorted(Comparator.comparing { obj: FileProp -> obj.isDirectory() }.reversed()).collect(Collectors.toList())
        if (fileList.isEmpty()) {
            return emptyList()
        }

        val last = fileList.size - 1
        val list: MutableList<String> = ArrayList()
        for (i in 0..last) {
            val f = fileList[i]
            val isLastInner = i == last
            val directory = f.isDirectory()
            if (directory) {
                val prefix = if (isLastInner) "└─" else "├─"
                list.add("$prefix${f.getName()}")
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

    class FileFindFilter {
        var nameFilter = Pattern.compile(".*", Pattern.CASE_INSENSITIVE)

        var hasFile = true

        var hasDir = true

        var ignoreEmpty = false

        var findHidden = false

        var createTimeFilter = ""

        var updateTimeFilter = ""

        var accessTimeFilter = ""

        var depth = Int.MAX_VALUE
    }

    interface FileProp {
        fun isFile(): Boolean
        fun getName(): String
        fun isHidden(): Boolean
        fun isDirectory(): Boolean
        fun listEntries(): List<FileProp>
    }

    class DocFileProp(private val file: File) : FileProp {
        override fun isFile(): Boolean = file.isFile

        override fun getName(): String = file.name

        override fun isHidden(): Boolean = file.isHidden

        override fun isDirectory(): Boolean = file.isDirectory

        override fun listEntries(): List<FileProp> = file.listFiles()?.map { DocFileProp(it) } ?: emptyList()
    }

    open class ZipProp(private val zip: ZipFile) : FileProp {
        override fun isFile(): Boolean = false

        override fun getName(): String = zip.name

        override fun isHidden(): Boolean = false

        override fun isDirectory(): Boolean = true

        override fun listEntries(): List<FileProp> = listEntry("")

        fun listEntry(prefix: String): List<FileProp> {
            val entries = zip.entries()
            val list = arrayListOf<FileProp>()
            val cnt1 = StrUtil.count(prefix, '/')

            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                if (entry.name.equals(prefix)) {
                    continue
                }
                if (prefix.isNotEmpty() && !entry.name.startsWith(prefix)) {
                    continue
                }
                val cnt2 = StrUtil.count(entry.name.removeSuffix("/"), '/')
                if (cnt2 > cnt1) {
                    // mean multi dir
                    continue
                }
                list.add(ZipEntryProp(zip, entry))
            }

            return list
        }
    }

    class ZipEntryProp(zip: ZipFile, private val entry: ZipEntry) : ZipProp(zip) {

        private var name: String? = null

        override fun isFile(): Boolean = !isDirectory()

        override fun getName(): String {
            if (name == null) {
                name = entry.name.removeSuffix("/").split("/").last()
            }
            return name!!
        }

        override fun isHidden(): Boolean = false

        override fun isDirectory(): Boolean = entry.isDirectory

        override fun listEntries(): List<FileProp> {
            if (isFile()) {
                return emptyList()
            }
            return listEntry(entry.name)
        }
    }
}
