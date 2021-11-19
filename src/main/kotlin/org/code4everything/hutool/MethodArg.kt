package org.code4everything.hutool

import cn.hutool.core.io.FileUtil
import com.alibaba.fastjson.JSON
import com.beust.jcommander.Parameter
import com.beust.jcommander.Parameters
import com.beust.jcommander.Strings
import org.code4everything.hutool.Utils.isStringEmpty

@Parameters(separators = ": ")
class MethodArg {

    @JvmField
    @Parameter(names = ["-r", "--run", "--command"], description = COMMAND_DESC, variableArity = true, order = 0)
    var command: MutableList<String> = ArrayList()

    @JvmField
    @Parameter(names = ["-c", "--class"], description = CLASS_DESC, help = true, order = 1)
    var className: String? = null

    @JvmField
    @Parameter(names = ["-m", "--method"], description = "the static method name(ignore case)", help = true, order = 2)
    var methodName: String? = null

    @JvmField
    @Parameter(names = ["-t", "--type"], description = "the class type of parameter(not required)", order = 3)
    var paramTypes: MutableList<String> = ArrayList()

    @JvmField
    @Parameter(names = ["-p", "--param", "--parameter"], description = PARAM_DESC, order = 4)
    var params: MutableList<String> = ArrayList()

    @JvmField
    @Parameter(names = ["-y", "--yank", "--copy"], description = "copy result to clipboard", order = 5)
    var copy = false

    @JvmField
    @Parameter(names = ["-a", "--auto-param"], description = "clipboard string into indexed parameter", order = 6)
    var paramIdxFromClipboard = -1

    @JvmField
    @Parameter(names = ["-v", "--version"], description = "the current version of hutool command line tool", order = 8)
    var version = false

    @JvmField
    @Parameter(names = ["-d", "--debug"], description = "enable debug mode", order = 9)
    var debug = false

    @JvmField
    @Parameter(names = ["--exception"], description = EXCEPTION_DESC, hidden = true, order = 10)
    var exception = false

    @JvmField
    @Parameter(description = "for command missing", variableArity = true, hidden = true, order = 11)
    var main: MutableList<String> = ArrayList()

    @JvmField
    @Parameter(names = ["--work-dir"], description = "current work dir", hidden = true, order = 12)
    var workDir = "."

    @Parameter(names = ["--sep", "-s"], description = "separator for array, list, etc. system line sep use '%n'.", order = 13)
    var sep = ""
        get() {
            if (isStringEmpty(field)) {
                return ","
            }
            return when (field) {
                "%n" -> FileUtil.getLineSeparator()
                "\\n" -> "\n"
                "\\r" -> "\r"
                "\\r\\n" -> "\r\n"
                else -> field
            }
        }

    override fun toString(): String {
        return JSON.toJSONString(this, true)
    }

    companion object {

        private const val CLASS_DESC = "full class name or name alias"
        private const val PARAM_DESC = "the parameter(s) of method invoking required"
        private const val EXCEPTION_DESC = "thrown an exception, only work on debug mode"
        private const val COMMAND_DESC = "build in method(can miss '-r')"

        @JvmStatic
        var separator: String = ""
            get() {
                val arg = Hutool.ARG
                if (!Strings.isStringEmpty(arg.sep) || Strings.isStringEmpty(field)) {
                    field = arg.sep
                }

                return field.ifEmpty { "," }
            }
            private set

        @JvmStatic
        fun getSubParams(methodArg: MethodArg?, fromIdx: Int): List<String> {
            return methodArg?.params?.subList(fromIdx, methodArg.params.size) ?: emptyList()
        }
    }
}
