package org.code4everything.hutool

import cn.hutool.core.io.FileUtil
import cn.hutool.core.swing.clipboard.ClipboardUtil
import cn.hutool.core.util.RuntimeUtil
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

object QLE {

    @JvmStatic
    @HelpInfo([
        "example: 'arg0.length()' 'false' 'some_string_to_get_length'", "",
        "param1: the script expression",
        "param2: if expression has '\${n}', set param2 true to replace '\${n}' to indexed arg", "",
        "args: the others params will map to args, and very param will map to indexed arg, like arg0 arg1...",
        "auto injected args: currDir, lineSep, fileSep, userHome",
        "auto injected methods: cmd(p1), nullto(p1,p2), clipboard(), list(p1,p2..pn), join(p1,p2)",
        "you can use it in your expression", "",
        "ql script grammar: https://github.com/alibaba/QLExpress",
    ])
    fun run(express: String, replaceArg: Boolean): Any? {
        var exp = handleExpression(express)
        if (exp.isEmpty()) {
            return null
        }

        DynamicParamsUtil.supportDynamicParams = true
        val args = MethodArg.getSubParams(Hutool.ARG, 2)
        if (replaceArg) {
            for (i in args.indices) {
                exp = exp.replace("\${$i}", args[i])
            }
        }

        Hutool.debugOutput("get ql script: %s", exp)
        val runner = ExpressRunner()

        importPackage(runner)
        bindMethod(runner)

        val context = getContext(args)
        return runner.execute(exp, context, null, true, false)
    }

    private fun getContext(args: List<String>): DefaultContext<String, Any> {
        val context = DefaultContext<String, Any>()
        context["currDir"] = Hutool.ARG.workDir
        context["lineSep"] = FileUtil.getLineSeparator()
        context["fileSep"] = File.separator
        context["userHome"] = FileUtil.getUserHomePath()
        context["args"] = ArgList.of(ArrayList<Any>(args))

        val joiner = StringJoiner(",", "(", ")")
        for (i in args.indices) {
            context["arg$i"] = args[i]
            joiner.add(args[i])
        }

        Hutool.debugOutput("execute expression with args: %s", joiner)
        return context
    }

    private fun bindMethod(runner: ExpressRunner) {
        val clz = QLE::class.java
        runner.addFunctionOfClassMethod("cmd", clz, "cmd", arrayOf(String::class.java), null)
        runner.addFunctionOfClassMethod("nullto", clz, "nullTo", Array(2) { Any::class.java }, null)
        runner.addFunctionOfClassMethod("list", clz, "list", arrayOf(Array::class.java), null)
        runner.addFunctionOfClassMethod("join", clz, "join", arrayOf(String::class.java, List::class.java), null)

        runner.addFunctionOfClassMethod("clipboard", ClipboardUtil::class.java, "getStr", emptyArray(), null)
    }


    private fun importPackage(runner: ExpressRunner) {
        val expressPackage = runner.rootExpressPackage
        expressPackage.addPackage("org.code4everything.hutool")
        expressPackage.addPackage("org.code4everything.hutool.converter")
        expressPackage.addPackage("com.alibaba.fastjson")
        expressPackage.addPackage("cn.hutool.core.util")
        expressPackage.addPackage("cn.hutool.core.collection")
        expressPackage.addPackage("cn.hutool.core.date")
        expressPackage.addPackage("cn.hutool.core.io")
        expressPackage.addPackage("cn.hutool.core.lang")
    }

    @JvmStatic
    fun cmd(cmd: String?): String {
        val result = RuntimeUtil.execForStr(cmd)
        return if (result?.isNotEmpty() == true) result.trim() else ""
    }

    @JvmStatic
    fun nullTo(v1: Any?, v2: Any): Any = v1 ?: v2

    @JvmStatic
    fun join(separator: String, list: List<Any?>): String = list.filterNotNull().joinToString(separator)

    @JvmStatic
    fun list(array: Array<*>): List<Any?> = array.toList()

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
