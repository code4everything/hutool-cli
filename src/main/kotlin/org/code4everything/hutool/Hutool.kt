package org.code4everything.hutool

import cn.hutool.core.date.Week
import cn.hutool.core.exceptions.ExceptionUtil
import cn.hutool.core.io.FileUtil
import cn.hutool.core.lang.Holder
import cn.hutool.core.lang.JarClassLoader
import cn.hutool.core.swing.clipboard.ClipboardUtil
import cn.hutool.core.util.ArrayUtil
import cn.hutool.core.util.ObjectUtil
import cn.hutool.core.util.ReflectUtil
import cn.hutool.log.dialect.console.ConsoleLog
import cn.hutool.log.level.Level
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.TypeReference
import com.alibaba.fastjson.util.TypeUtils
import com.beust.jcommander.JCommander
import java.io.File
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Parameter
import java.nio.charset.Charset
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.Collections
import java.util.Date
import java.util.Objects
import java.util.StringJoiner
import java.util.TreeMap
import java.util.concurrent.FutureTask
import java.util.function.Consumer
import java.util.regex.Pattern
import java.util.stream.Collectors
import kotlin.math.min
import org.code4everything.hutool.Converter.Companion.newConverter
import org.code4everything.hutool.Utils.getMethodFullInfo
import org.code4everything.hutool.Utils.isArrayEmpty
import org.code4everything.hutool.Utils.isCollectionEmpty
import org.code4everything.hutool.Utils.isStringEmpty
import org.code4everything.hutool.Utils.parseClass
import org.code4everything.hutool.Utils.parseClassName
import org.code4everything.hutool.converter.ArrayConverter
import org.code4everything.hutool.converter.CharsetConverter
import org.code4everything.hutool.converter.DateConverter
import org.code4everything.hutool.converter.FileConverter
import org.code4everything.hutool.converter.JsonObjectConverter
import org.code4everything.hutool.converter.LineSepConverter
import org.code4everything.hutool.converter.ListStringConverter
import org.code4everything.hutool.converter.MapConverter
import org.code4everything.hutool.converter.PatternConverter
import org.code4everything.hutool.converter.SetStringConverter
import org.code4everything.hutool.converter.WeekConverter

object Hutool {

    internal const val CLASS_JSON = "class.json"
    internal const val CLAZZ_KEY = "clazz"

    private const val CONVERTER_JSON = "converter.json"
    private const val PARAM_KEY = "paramTypes"
    private const val VERSION = "v1.6"
    private const val ALIAS = "alias"
    private const val COMMAND_JSON = "command.json"

    const val PLUGIN_NAME = "org.code4everything.hutool.PluginEntry"

    private val ALIAS_CACHE by lazy { HashMap<String, JSONObject>(4, 1f) }
    private val resultContainer by lazy { ArrayList<String>(5) }

    val homeDir: String by lazy { System.getenv("HUTOOL_PATH") }
    val HUTOOL_USER_HOME by lazy { "${System.getProperty("user.home")}${File.separator}hutool-cli" }
    val pluginHome by lazy { homeDir + File.separator + "plugins" }
    val simpleDateFormat by lazy { SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS") }
    val isDebug: Boolean get() = ARG.debug

    private val argLocal = ThreadLocal<MethodArg>()
    var ARG: MethodArg
        set(value) = argLocal.set(value)
        get() = argLocal.get()!!

    val resultString = ThreadLocal<String>()
    private lateinit var commander: JCommander

    private val resultLocal = ThreadLocal<Any?>()
    private var result: Any?
        set(value) = resultLocal.set(value)
        get() = resultLocal.get()

    private var omitParamType = ThreadLocal<Boolean>().apply { set(true) }
    private var outputConverterName = ThreadLocal<String?>()
    private var inputConverterNames = ThreadLocal<List<String>?>()

    @JvmStatic
    internal fun resolveCmd(args: Array<String>): String {
        commander = JCommander.newBuilder().addObject(ARG).build()
        commander.programName = "hutool-cli"

        try {
            commander.parse(*args)
        } catch (e: Exception) {
            seeUsage()
            return ""
        }

        if (isDebug && ARG.exception) {
            throw CliException()
        }
        if (ARG.version) {
            return "hutool-cli: $VERSION"
        } else {
            debugOutput("hutool-cli: %s", VERSION)
        }

        debugOutput("received arguments: %s", ArrayUtil.toString(args))
        debugOutput("starting resolve")
        ARG.command.addAll(ARG.main)

        resolveResult()
        debugOutput("result handled success")
        resultString.set(convertResult())

        if (ARG.copy) {
            debugOutput("copying result into clipboard")
            try {
                ClipboardUtil.setStr(resultString.get())
                debugOutput("result copied")
            } catch (e: Exception) {
                debugOutput("copy result failed")
            }
        }

        if (isDebug) {
            println()
        }

        return resultString.get()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        if (isArrayEmpty(args)) {
            seeUsage()
            return
        }

        ConsoleLog.setLevel(Level.ERROR)
        ARG = MethodArg()
        val list: MutableList<String> = ArrayList(8)

        // 使用字符串 `//` 切割多条命令
        for (arg in args) {
            if ("//" == arg) {
                // 处理上条命令，并记录结果
                val res = resolveCmd(list.toTypedArray())
                if (isDebug) {
                    println(res)
                }
                resultContainer.add(res)
                list.clear()
                ARG = MethodArg().apply { workDir = ARG.workDir }
            } else {
                list.add(arg)
            }
        }

        if (list.isNotEmpty()) {
            println()
            println(resolveCmd(list.toTypedArray()))
        }
    }

    private fun resolveResult() {
        var fixClassName = true
        if (ARG.command.isNotEmpty()) {
            // 命令带 @ 符号，覆盖文件定义的方法
            val tokens = ARG.command[0].split('@')
            val command = tokens[0]
            debugOutput("get command: %s", command)
            ARG.params.addAll(ARG.command.subList(1, ARG.command.size))

            if (ALIAS == command) {
                seeAlias("", COMMAND_JSON)
                return
            }

            val sharp = '#'
            var idx = command.indexOf(sharp)
            if (idx > 0) {
                // 非类方法别名，使用类别名和方法别名调用
                debugOutput("invoke use class name and method name combined in command mode")
                ARG.className = command.substring(0, idx)
                ARG.methodName = command.substring(idx + 1)
                resolveResultByClassMethod(true)
                return
            }

            omitParamType.set(false)
            fixClassName = false

            // 从命令文件中找到类名和方法名以及参数类型，默认值
            val methodKey = "method"
            val aliasJson = getAlias(COMMAND_JSON)
            var methodJson = aliasJson.getJSONObject(command)
            if (methodJson?.containsKey(methodKey) != true) {
                val plugin = FileUtil.file(pluginHome, "${command.removePrefix("p.")}.jar")
                if (FileUtil.exist(plugin)) {
                    methodJson = JSONObject().fluentPut(methodKey, "$PLUGIN_NAME#run()")
                    Utils.classLoader = JarClassLoader().apply { addJar(plugin) }
                    debugOutput("get command from plugin: " + plugin.name)
                } else if (ARG.params.size > 0 && parseClassName("@$command") != "@$command") {
                    val methodName = ARG.params.removeFirst()
                    methodJson = JSONObject().fluentPut(methodKey, "@$command#$methodName")
                    debugOutput("get method name from param[0]")
                    omitParamType.set(true)
                } else {
                    result = "command[$command] not found!"
                    return
                }
            }

            if (ARG.help && methodJson.containsKey("helps")) {
                val helps = methodJson.getJSONArray("helps")
                if (helps.isNotEmpty()) {
                    result = LineSepConverter().object2String(helps)
                    return
                }
            }

            val classMethod = methodJson.getString(methodKey)
            idx = classMethod.lastIndexOf(sharp)
            if (idx < 1) {
                result = "method[$classMethod] format error, required: com.example.Main#main"
                return
            }

            outputConverterName.set(methodJson.getString("outConverter"))
            inputConverterNames.set(methodJson.getJSONArray("inConverters")?.map { it.toString() })
            methodJson.getString("outArgs")?.also {
                val methodArg = MethodArg()
                JCommander.newBuilder().addObject(methodArg).build().parse(*(it.split(" ").toTypedArray()))
                ARG.copy = if (ARG.copy) true else methodArg.copy
                ARG.debug = if (ARG.debug) true else methodArg.debug
                ARG.sep = if (it.contains("-s") || it.contains("--sep")) methodArg.sep else ARG.sep
            }

            debugOutput("parse method to class name and method name")
            ARG.className = classMethod.substring(0, idx)
            ARG.methodName = classMethod.substring(idx + 1)
            parseMethod(methodJson)
            if (tokens.size > 1) {
                val userParamTypes = tokens[1].split(',').filter { it.isNotEmpty() }
                if (userParamTypes.isEmpty()) {
                    val filter = listOf(ARG.methodName!!.lowercase())
                    outputConverterName.set(LineSepConverter::class.java.name)
                    result = Utils.outputPublicStaticMethods0(ARG.className!!, filter, true)
                    return
                } else {
                    ARG.paramTypes.clear()
                    ARG.paramTypes.addAll(userParamTypes)
                }
            }
            debugOutput("get method: %s, params: %s", ARG.methodName, ARG.paramTypes)
        }

        resolveResultByClassMethod(fixClassName)
    }

    private fun resolveResultByClassMethod(fixName: Boolean) {
        var innerFixName = fixName
        if (isStringEmpty(ARG.className)) {
            seeUsage()
            return
        }

        if (ALIAS == ARG.className) {
            seeAlias("", CLASS_JSON)
            return
        }

        var methodAliasPaths = ""
        if (innerFixName) {
            // 尝试从类别名文件中查找类全名
            val methodAliasKey = "methodAliasPaths"
            val aliasJson = getAlias(CLASS_JSON)
            val clazzJson = aliasJson.getJSONObject(ARG.className)
            if (clazzJson?.containsKey(CLAZZ_KEY) != true) {
                innerFixName = false
            } else {
                ARG.className = clazzJson.getString(CLAZZ_KEY)
                debugOutput("find class alias: %s", ARG.className)
                methodAliasPaths = clazzJson.getJSONArray(methodAliasKey).joinToString(File.separator)
            }
        }

        val clazz: Class<*>?
        debugOutput("loading class: %s", ARG.className)
        try {
            clazz = parseClass(ARG.className!!)
        } catch (e: Exception) {
            debugOutput(ExceptionUtil.stacktraceToString(e, Int.MAX_VALUE))
            ARG.className = ""
            resolveResultByClassMethod(false)
            return
        }

        if (clazz == Hutool::class.java) {
            result = "class not support: org.code4everything.hutool.Hutool"
            return
        }

        debugOutput("load class success")
        resolveResultByClassMethod(clazz!!, innerFixName, methodAliasPaths)
    }

    private fun resolveResultByClassMethod(clazz: Class<*>, fixName: Boolean, methodAliasPath: String) {
        if (isStringEmpty(ARG.methodName)) {
            seeUsage()
            return
        }

        if (ALIAS == ARG.methodName) {
            if (methodAliasPath.isNotBlank()) {
                seeAlias(clazz.name, methodAliasPath)
            }
            return
        }

        fixMethodName(fixName, methodAliasPath)

        // 将剪贴板字符内容注入到方法参数的指定索引位置
        if (ARG.paramIdxFromClipboard >= 0) {
            ARG.params.add(min(ARG.params.size, ARG.paramIdxFromClipboard), getFromClipboard())
        }

        var method: Method?
        if (omitParamType.get() && isCollectionEmpty(ARG.paramTypes)) {
            // 缺省方法参数类型，自动匹配方法
            debugOutput("getting method ignore case by method name and param count")
            method = autoMatchMethod(clazz)
        } else {
            debugOutput("parsing parameter types")
            val paramTypes = arrayOfNulls<Class<*>>(ARG.paramTypes.size)
            val parseDefaultValue = ARG.params.size < paramTypes.size
            for (i in ARG.paramTypes.indices) {
                // 解析默认值，默认值要么都填写，要么都不填写
                val paramType = parseParamType(i, ARG.paramTypes[i], parseDefaultValue)
                try {
                    paramTypes[i] = parseClass(paramType)
                } catch (e: Exception) {
                    debugOutput(ExceptionUtil.stacktraceToString(e, Int.MAX_VALUE))
                    result = "param type not found: $paramType"
                    return
                }
            }
            debugOutput("parse parameter types success")
            debugOutput("getting method ignore case by method name and param types")
            method = ReflectUtil.getMethod(clazz, true, ARG.methodName, *paramTypes)
        }

        if (method?.let { Modifier.isPublic(it.modifiers) } != true) {
            val msg = "static method not found(ignore case) or is not a public method: %s#%s(%s)"
            val paramTypeArray = ARG.paramTypes.toTypedArray()
            debugOutput(msg, clazz.name, ARG.methodName, ArrayUtil.join(paramTypeArray, ", "))
            ARG.methodName = ""
            resolveResultByClassMethod(clazz, fixName, methodAliasPath)
            return
        }

        // 查看帮助
        if (ARG.help) {
            val helpInfo = method.getAnnotation(HelpInfo::class.java)
            if (helpInfo == null) {
                result = "ops, sorry, method no help info"
                return
            } else if (helpInfo.callbackMethodName.isNotEmpty()) {
                method = clazz.getMethod(helpInfo.callbackMethodName)
            } else {
                result = LineSepConverter().object2String(helpInfo.helps.toList())
                return
            }
        }

        val parameters = method!!.parameters
        val converterClz = IOConverter::class.java
        outputConverterName.set(method.getAnnotation(converterClz).getConverterName(outputConverterName.get()))
        debugOutput("get method success")
        if (ARG.params.size < parameters.size) {
            ARG.params.add(getFromClipboard())
        }
        if (ARG.params.size < parameters.size) {
            result = try {
                val methodFullInfo = parseMethodFullInfo(clazz.name, method.name, ARG.paramTypes)
                "parameter error, method request: $methodFullInfo"
            } catch (e: Exception) {
                val paramTypeArray = parameters.map { x: Parameter -> x.type.name }.toTypedArray()
                "parameter error, required: (${ArrayUtil.join(paramTypeArray, ", ")})"
            }
            return
        }

        // 转换参数类型
        debugOutput("casting parameter to class type")
        val params = arrayOfNulls<Any>(parameters.size)
        val paramJoiner = StringJoiner(", ")
        for (i in parameters.indices) {
            val param = ARG.params[i]
            paramJoiner.add(param)
            val parameter = parameters[i]
            val converterName = parameter.getAnnotation(converterClz).getConverterName(inputConverterNames.get()?.getOrNull(i))
            params[i] = castParam2JavaType(converterName, param, parameter.type, true)
        }

        debugOutput("cast parameter success")
        debugOutput("invoking method: %s#%s(%s)", clazz.name, method.name, paramJoiner)
        result = if (Utils.classLoader == null) ReflectUtil.invokeStatic(method, *params) else {
            val future: FutureTask<Any> = FutureTask { ReflectUtil.invokeStatic(method, *params) }
            Utils.syncRun(future, Utils.classLoader!!)
        }
        debugOutput("invoke method success")
    }

    private fun getFromClipboard(): String {
        var content = "nil"
        try {
            content = ClipboardUtil.getStr()
            debugOutput("get param from clipboard success")
        } catch (e: Exception) {
            debugOutput("get param from clipboard failed, return null")
        }
        return content
    }

    private fun IOConverter?.getConverterName(srcName: String?): String? {
        return if (srcName?.isNotEmpty() == true) srcName else this?.run { className.ifEmpty { value.java.name } }
    }

    private fun parseParamType(index: Int, paramType: String, parseDefaultValue: Boolean): String {
        var innerParse = parseDefaultValue
        val idx = paramType.indexOf('=')
        if (idx < 1) {
            return paramType
        }

        val type: String
        if (paramType[0] == '@') {
            innerParse = true
            type = paramType.substring(1, idx)
        } else {
            type = paramType.substring(0, idx)
        }

        // 解析默认值
        if (innerParse) {
            val param = paramType.substring(idx + 1)
            ARG.params.add(min(index, ARG.params.size), param)
        }
        return type
    }

    private fun autoMatchMethod(clazz: Class<*>): Method? {
        // 找到与方法名一致的方法（忽略大小写）
        var fuzzyList = clazz.methods.filter {
            val modifiers = it.modifiers
            Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && it.name.equals(ARG.methodName, ignoreCase = true)
        }

        if (isCollectionEmpty(fuzzyList)) {
            return null
        }
        if (fuzzyList.size == 1) {
            return fuzzyList.first()
        }

        // 找到离参数个数最相近的方法
        val paramSize = ARG.params.size
        fuzzyList = fuzzyList.sortedWith(Comparator.comparingInt { it.parameterCount })
        var method = fuzzyList.first()
        for (m in fuzzyList) {
            if (m.parameterCount > paramSize) {
                break
            }
            method = m
        }

        // 确定方法参数类型
        method.also {
            ARG.paramTypes = ArrayList()
            for (paramType in it.parameterTypes) {
                ARG.paramTypes.add(paramType.name)
            }
        }
        return method
    }

    fun castParam2JavaType(converterName: String?, param: String, type: Class<*>, replace: Boolean): Any? {
        var p = param
        if ("nil" == p) {
            return null
        }

        if (replace && resultContainer.isNotEmpty()) {
            // 替换连续命令中的结果记录值，格式：\\0,\\1,\\2...
            val endIds = resultContainer.size - 1
            for (i in 0..endIds) {
                val key = "\\\\" + i
                val value = resultContainer[i]
                p = p.replace(key, value)
                if (i == endIds) {
                    p = p.replace("\\\\p", value)
                }
            }
        }
        if (converterName == null && CharSequence::class.java.isAssignableFrom(type)) {
            return p
        }

        // 转换参数类型
        var converter: Converter<*>? = null
        try {
            converter = getConverter(converterName, type)
            if (converter != null) {
                debugOutput("cast param[%s] using converter: %s", p, converter.javaClass.name)
                return converter.string2Object(p)
            }
        } catch (e: Exception) {
            val msg = "cast param[%s] to type[%s] using converter[%s] failed: %s"
            val err = ExceptionUtil.stacktraceToString(e, Int.MAX_VALUE)
            debugOutput(msg, p, type.name, converter!!.javaClass.name, err)
        }

        debugOutput("auto convert param[%s] to type: %s", p, type.name)
        return TypeUtils.cast(p, type, null)
    }

    private fun getConverter(converterName: String?, type: Class<*>): Converter<*>? {
        if (converterName != null) {
            return Converter.getConverter(converterName, type)
        }
        if (MutableList::class.java.isAssignableFrom(type)) {
            return ListStringConverter()
        }
        if (MutableSet::class.java.isAssignableFrom(type)) {
            return SetStringConverter()
        }
        if (type.isArray) {
            return ArrayConverter(type)
        }
        val converterName0: String? = getAlias(CONVERTER_JSON).getString(type.name)
        return converterName0?.let { newConverter(parseClass(it) as Class<Converter<*>>?, type) }
    }

    private fun fixMethodName(fixName: Boolean, methodAliasPath: String) {
        // 从方法别名文件中找到方法名
        if (fixName && methodAliasPath.isNotBlank()) {
            val methodKey = "methodName"
            val aliasJson = getAlias(methodAliasPath)
            val methodJson = aliasJson.getJSONObject(ARG.methodName)
            if (Objects.nonNull(methodJson)) {
                val methodName = methodJson.getString(methodKey)
                if (!isStringEmpty(methodName)) {
                    ARG.methodName = methodName
                }
                parseMethod(methodJson)
                debugOutput("get method name: %s", ARG.methodName)
                omitParamType.set(false)
            }
        }
    }

    private fun seeUsage() {
        println()
        if (Objects.isNull(ARG)) {
            ARG = MethodArg()
        }
        if (Objects.isNull(commander)) {
            commander = JCommander.newBuilder().addObject(ARG).build()
        }
        commander.usage()
    }

    private fun filter(aliasJson: JSONObject) {
        if (isCollectionEmpty(ARG.params)) {
            return
        }

        // 使用用户输入的关键词过滤
        val iterator = aliasJson.entries.iterator()
        val filter = ARG.params.map { it.lowercase() }
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val json = aliasJson.getJSONObject(entry.key)
            val method = json?.getString("method") ?: json?.getString("clazz")
            val name = (entry.key + (method ?: "")).lowercase()
            if (filter.stream().noneMatch { s: String? -> name.contains(s!!) }) {
                iterator.remove()
            }
        }
    }

    private fun seeAlias(className: String, path: String) {
        val aliasJson = getAlias(path)
        filter(aliasJson)
        val joiner = StringJoiner("\n")
        val maxLength = Holder.of(0)
        val map = TreeMap<String, String>()
        aliasJson.keys.forEach(Consumer { k: String ->
            val length = k.length
            if (length > maxLength.get()) {
                maxLength.set(length)
            }
            val json = aliasJson.getJSONObject(k)
            if (json.containsKey(CLAZZ_KEY)) {
                // 类别名
                map[k] = json.getString(CLAZZ_KEY)
            } else {
                // 类方法别名或方法别名字
                var methodName = json.getString("method")
                if (isStringEmpty(methodName)) {
                    methodName = json.getString("methodName")
                }
                ARG.methodName = methodName
                // 清除过滤参数，使用定义的默认方法
                ARG.params.clear()
                parseMethod(json)
                try {
                    // 拿到方法的形参名称，参数类型
                    map[k] = parseMethodFullInfo(className, ARG.methodName!!, ARG.paramTypes)
                } catch (e: Exception) {
                    // 这里只能输出方法参数类型，无法输出形参类型
                    debugOutput("parse method param name error: %s", ExceptionUtil.stacktraceToString(e, Int.MAX_VALUE))
                    val typeString = ArrayUtil.join(ARG.paramTypes.toTypedArray(), ", ")
                    map[k] = "${ARG.methodName}($typeString)"
                }
            }
        })

        // 输出别名到终端
        debugOutput("max length: %s", maxLength.get())
        map.forEach { (k: String?, v: String) -> joiner.add("${k.padEnd(maxLength.get(), ' ')} = $v") }
        result = joiner.toString()
    }

    private fun parseMethod(json: JSONObject) {
        if (ARG.methodName!!.endsWith(")")) {
            val idx = ARG.methodName!!.indexOf("(")
            if (idx < 1) {
                debugOutput("method format error: %s", ARG.methodName)
                return
            }
            val split = ARG.methodName!!.substring(idx + 1, ARG.methodName!!.length - 1).split(",").toTypedArray()
            ARG.paramTypes = Arrays.stream(split).filter { it?.isNotEmpty() ?: false }.collect(Collectors.toList())
            ARG.methodName = ARG.methodName!!.substring(0, idx)
        } else if (Objects.nonNull(json)) {
            val paramTypes =
                json.getObject<ArrayList<String>>(PARAM_KEY, object : TypeReference<ArrayList<String>?>() {})
            ARG.paramTypes = paramTypes ?: Collections.emptyList()
        }
        if (Objects.isNull(json)) {
            return
        }

        // 重载方法
        val specificTypes = json[ARG.params.size.toString() + "param"] ?: return
        if (specificTypes is List<*>) {
            ARG.paramTypes = specificTypes.stream().map { it.toString() }.collect(Collectors.toList())
        } else {
            ARG.paramTypes = arrayListOf<String>().apply { addAll(specificTypes.toString().split(",")) }
        }
    }

    private fun parseMethodFullInfo(className: String, methodName: String, paramTypes: List<String>): String {
        var mn = methodName
        var outClassName = false
        val clazz: Class<*>
        if (isStringEmpty(className)) {
            // 来自方法别名
            val idx = methodName!!.indexOf("#")
            clazz = parseClass(methodName.substring(0, idx))
            mn = methodName.substring(idx + 1)
            outClassName = true
        } else {
            clazz = parseClass(className)
        }

        val params = arrayOfNulls<Class<*>>(paramTypes.size)
        val defaultValueMap: MutableMap<String?, String?> = HashMap(4, 1f)
        for (i in paramTypes.indices) {
            val paramType = paramTypes[i]
            val idx = paramType.indexOf('=')
            if (idx > 0) {
                val typeName = paramType.substring(if (paramType[0] == '@') 1 else 0, idx)
                params[i] = parseClass(typeName)
                defaultValueMap[params[i]!!.name + i] = paramType.substring(idx + 1)
            } else {
                params[i] = parseClass(paramType)
            }
        }

        val method = clazz.getMethod(mn, *params)
        return (if (outClassName) clazz.name + "#" else "") + getMethodFullInfo(method, defaultValueMap)
    }

    private fun convertResult(): String {
        debugOutput("converting result")
        try {
            val res = convertResult(result, outputConverterName.get())
            debugOutput("result convert success")
            return res
        } catch (e: Exception) {
            debugOutput("convert result error: %s", e.message)
        }
        return ""
    }

    fun convertResult(obj: Any?, converterName: String?): String {
        if (obj == null) {
            debugOutput("get null result, convert to empty")
            return ""
        }

        val resClass: Class<*> = obj.javaClass
        if (converterName?.isNotEmpty() == true) {
            return Converter.getConverter(converterName, resClass)!!.object2String(obj)
        }

        if (obj is CharSequence) {
            return obj.toString()
        }
        if (obj is File) {
            return FileConverter().object2String(obj)
        }
        if (obj is Date) {
            return DateConverter().object2String(obj)
        }
        if (obj is Map<*, *>) {
            return MapConverter().object2String(obj)
        }
        if (obj is Double) {
            return String.format("%.2f", obj)
        }
        if (obj is Charset) {
            return CharsetConverter().object2String(obj)
        }
        if (obj is Collection<*>) {
            return ListStringConverter().object2String(obj)
        }
        if (obj is Pattern) {
            return PatternConverter().object2String(obj)
        }
        if (obj is Week) {
            return WeekConverter().object2String(obj)
        }
        if (obj is JSON) {
            return JsonObjectConverter(Any::class.java).object2String(obj)
        }
        if (obj.javaClass.isArray) {
            return ArrayConverter(resClass).object2String(obj)
        }

        val name = resClass.name
        val converterJson = getAlias(CONVERTER_JSON)
        val converterName0 = converterJson.getString(name)
        return if (isStringEmpty(converterName0)) {
            ObjectUtil.toString(obj)
        } else newConverter(parseClass(converterName0) as Class<out Converter<*>>?, resClass)!!.object2String(obj)
    }

    fun getAlias(filename: String): JSONObject {
        if (!ALIAS_CACHE.containsKey(filename)) {
            val userAlias = getJson(HUTOOL_USER_HOME + File.separator + filename)
            val hutoolAlias = getJson(homeDir + File.separator + filename)
            ALIAS_CACHE[filename] = Utils.mergeJson(userAlias, hutoolAlias)
        }
        return ALIAS_CACHE[filename]!!
    }

    private fun getJson(path: String): JSONObject {
        val file = File(path)
        return if (file.exists() && file.isFile) JSON.parseObject(FileUtil.readUtf8String(path)) else JSONObject()
    }

    fun debugOutput(msg: String, vararg params: Any?) {
        var m = msg
        if (isDebug) {
            val stackTrace = Thread.currentThread().stackTrace
            val className = if (stackTrace.size > 2) stackTrace[2].className else "Unknown"
            val lineNumber = if (stackTrace.size > 2) stackTrace[2].lineNumber.toString() else "NaN"
            if (params.isNotEmpty()) {
                m = String.format(m, *params)
            }
            m = "${simpleDateFormat.format(Date())} [${Thread.currentThread().name}] $className:$lineNumber - $m"
            println(m)
        }
    }

    fun test2(cmd: String): String = test(*cmd.split(" ").toTypedArray())

    fun test(vararg args: String): String {
        val alias = args[0]
        val cmd = java.lang.String.join(" ", *args)
        val cs = CharArray(cmd.length + 11) { '=' }
        cs[0] = '\n'
        val separator = String(cs)
        println("$separator\n>> hu $cmd <<$separator")
        var idx = args.size
        val newArgs = Arrays.copyOf(args, idx + 2)
        newArgs[0] = if (alias == "plugin") "$PLUGIN_NAME#run" else alias
        newArgs[idx++] = "--work-dir"
        newArgs[idx] = Paths.get(".").toAbsolutePath().normalize().toString()
        main(newArgs)
        return resultString.get()
    }
}
