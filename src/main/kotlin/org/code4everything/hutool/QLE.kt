package org.code4everything.hutool

import cn.hutool.core.io.FileUtil
import cn.hutool.core.swing.clipboard.ClipboardUtil
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.ql.util.express.DefaultContext
import com.ql.util.express.DynamicParamsUtil
import com.ql.util.express.ExpressRunner
import java.io.File
import java.lang.reflect.Type
import java.math.BigDecimal
import java.math.BigInteger
import java.util.Date
import java.util.StringJoiner
import org.code4everything.hutool.converter.FileConverter
import org.code4everything.hutool.qe.MethodBinds
import org.code4everything.hutool.qe.UnixCmdBinds

object QLE {

    @JvmStatic
    private lateinit var runner: ExpressRunner

    @JvmStatic
    @HelpInfo([
        "example: 'arg0.length()' 'some_string_to_get_length'", "",
        "param1: the script expression", "",
        "args: the others params will map to args, and very param will map to indexed arg, like arg0 arg1...",
        "auto injected args: currdir, linesep, filesep, userhome, clipboard/c, unix/cmd/u", "",
        "auto injected fields/methods: ",
        "cmd(string): execute a command in terminal",
        "nullto(object,object): if p1 is null return p2, else p1",
        "list(object): variable arguments, return a list",
        "list.join(string): join a list to string, like \"list(1,2,3).join('<')\"",
        "string.lower & string.upper: transfer string to lower case or upper case",
        "string.int & string.long & string.double: convert string to number",
        "string.strip(string): remove prefix and suffix, like \"'@abc@'.strip('@')'\"",
        "string.json: convert string or file to json",
        "string.file: convert this string to file",
        "string.date: convert this string to date",
        "list.sort: sort the list, require every item is comparable",
        "list.min & list.max: get the min or max item, require every item is an instance of comparable",
        "list.sum & list.avg: calculate sum or avg value for a list, require every item is a number, and return a double", "",
        "object.str: call toString() method",
        "run(string): run hu command in ql script, like \"run('base64','some text here')\"",
        "it return a object which implements 'CharSequence', result available methods: str(), raw().", "",
        "ql script grammar: https://github.com/alibaba/QLExpress",
    ])
    fun run(express: String): Any? {
        val exp = handleExpression(express)
        if (exp.isEmpty()) {
            return null
        }

        DynamicParamsUtil.supportDynamicParams = true
        val args = MethodArg.getSubParams(Hutool.ARG, 1)

        Hutool.debugOutput("get ql script: %s", exp)
        runner = ExpressRunner()

        val binds = MethodBinds(runner)
        binds.importPackage()
        binds.bindStaticMethods()
        binds.bind4List()
        binds.bind4String()
        binds.bind4Object()

        val context = getContext(args)
        return runner.execute(exp, context, null, true, false)
    }

    private fun getContext(args: List<String>): DefaultContext<String, Any> {
        val context = QeContext()
        context["currdir"] = Hutool.ARG.workDir
        context["linesep"] = FileUtil.getLineSeparator()
        context["filesep"] = File.separator
        context["userhome"] = FileUtil.getUserHomePath()
        context["args"] = ArgList.of(ArrayList<Any>(args))

        val joiner = StringJoiner(",", "(", ")")
        for (i in args.indices) {
            context["arg$i"] = args[i]
            joiner.add(args[i])
        }

        Hutool.debugOutput("execute expression with args: %s", joiner)
        return context
    }

    private fun handleExpression(expression: String): String {
        if (expression.startsWith("file:")) {
            val file = FileConverter().string2Object(expression.substring(5))
            if (FileUtil.exist(file)) {
                Hutool.debugOutput("get script from file: %s", file.absolutePath)
                return FileUtil.readUtf8String(file)
            }
        }
        return expression
    }

    class RunResult(var finalResult: String, private var rawResult: Any?) : CharSequence {
        override val length: Int
            get() = finalResult.length

        override fun get(index: Int): Char = finalResult[index]

        override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = finalResult.subSequence(startIndex, endIndex)

        override fun toString(): String = finalResult

        override fun equals(other: Any?): Boolean = other?.toString() == finalResult

        override fun hashCode(): Int = finalResult.hashCode()

        fun raw(): Any? = rawResult

        fun str(): String = finalResult
    }

    private class QeContext : DefaultContext<String, Any>() {
        override fun get(key: String?): Any? {
            val value = super.get(key)
            if (value != null) {
                return value
            }

            // for lazy loading
            if (key == "u" || key == "unix" || key == "cmd") {
                return UnixCmdBinds.getCmdBuilder(runner)
            }
            if (key == "c" || key == "clipboard") {
                return ClipboardUtil.getStr()
            }

            return null
        }
    }

    private class ArgList(list: List<Any>) : JSONArray(list) {

        override fun get(index: Int): Any? = if (index >= size) null else super.get(index)

        override fun getJSONObject(index: Int): JSONObject? = if (index >= size) null else super.getJSONObject(index)

        override fun getJSONArray(index: Int): JSONArray? = if (index >= size) null else super.getJSONArray(index)

        override fun <T> getObject(index: Int, clazz: Class<T>): T? = if (index >= size) null else super.getObject(index, clazz)

        override fun <T> getObject(index: Int, type: Type): T? = if (index >= size) null else super.getObject(index, type)

        override fun getBoolean(index: Int): Boolean? = if (index >= size) null else super.getBoolean(index)

        override fun getBooleanValue(index: Int): Boolean = getBooleanValue(index, false)

        fun getBooleanValue(index: Int, value: Boolean): Boolean = if (index >= size) value else super.getBooleanValue(index)

        override fun getByte(index: Int): Byte? = if (index >= size) null else super.getByte(index)

        override fun getByteValue(index: Int): Byte = getByteValue(index, 0)

        fun getByteValue(index: Int, value: Byte): Byte = if (index >= size) value else super.getByteValue(index)

        override fun getShort(index: Int): Short? = if (index >= size) null else super.getShort(index)

        override fun getShortValue(index: Int): Short = getShortValue(index, 0)

        fun getShortValue(index: Int, value: Short): Short = if (index >= size) value else super.getShortValue(index)

        override fun getInteger(index: Int): Int? = if (index >= size) null else super.getInteger(index)

        override fun getIntValue(index: Int): Int = getIntValue(index, 0)

        fun getIntValue(index: Int, value: Int): Int = if (index >= size) value else super.getIntValue(index)

        override fun getLong(index: Int): Long? = if (index >= size) null else super.getLong(index)

        override fun getLongValue(index: Int): Long = getLongValue(index, 0)

        fun getLongValue(index: Int, value: Long): Long = if (index >= size) value else super.getLongValue(index)

        override fun getFloat(index: Int): Float? = if (index >= size) null else super.getFloat(index)

        override fun getFloatValue(index: Int): Float = getFloatValue(index, 0f)

        fun getFloatValue(index: Int, value: Float): Float = if (index >= size) value else super.getFloatValue(index)

        override fun getDouble(index: Int): Double? = if (index >= size) null else super.getDouble(index)

        override fun getDoubleValue(index: Int): Double = getDoubleValue(index, 0.0)

        fun getDoubleValue(idx: Int, value: Double): Double = if (idx >= size) value else super.getDoubleValue(idx)

        override fun getBigDecimal(index: Int): BigDecimal? = if (index >= size) null else super.getBigDecimal(index)

        override fun getBigInteger(index: Int): BigInteger? = if (index >= size) null else super.getBigInteger(index)

        override fun getString(index: Int): String? = if (index >= size) null else super.getString(index)

        override fun getDate(index: Int): Date? = if (index >= size) null else super.getDate(index)

        override fun getSqlDate(index: Int): Any? = if (index >= size) null else super.getSqlDate(index)

        override fun getTimestamp(index: Int): Any? = if (index >= size) null else super.getTimestamp(index)

        companion object {

            @JvmStatic
            fun of(list: List<Any>): ArgList {
                return ArgList(list)
            }
        }
    }
}
