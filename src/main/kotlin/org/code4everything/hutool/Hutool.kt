package org.code4everything.hutool

import cn.hutool.core.date.Week
import cn.hutool.core.exceptions.ExceptionUtil
import cn.hutool.core.io.FileUtil
import cn.hutool.core.lang.Holder
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
import java.util.function.Consumer
import java.util.regex.Pattern
import java.util.stream.Collectors
import javassist.ClassPool
import javassist.CtClass
import javassist.NotFoundException
import org.code4everything.hutool.Converter.Companion.newConverter
import org.code4everything.hutool.Utils.getMethodFullInfo
import org.code4everything.hutool.Utils.isArrayEmpty
import org.code4everything.hutool.Utils.isCollectionEmpty
import org.code4everything.hutool.Utils.isStringEmpty
import org.code4everything.hutool.Utils.padAfter
import org.code4everything.hutool.Utils.parseClass
import org.code4everything.hutool.Utils.parseClassName
import org.code4everything.hutool.converter.ArrayConverter
import org.code4everything.hutool.converter.CharsetConverter
import org.code4everything.hutool.converter.DateConverter
import org.code4everything.hutool.converter.FileConverter
import org.code4everything.hutool.converter.ListStringConverter
import org.code4everything.hutool.converter.MapConverter
import org.code4everything.hutool.converter.PatternConverter
import org.code4everything.hutool.converter.SetStringConverter
import org.code4everything.hutool.converter.WeekConverter

/**
 * @author pantao
 * @since 2020/10/27
 */
object Hutool {

    const val CLASS_JSON = "class.json"
    const val CONVERTER_JSON = "converter.json"
    const val CLAZZ_KEY = "clazz"
    const val COMMAND_JSON = "command.json"
    val HUTOOL_USER_HOME = System.getProperty("user.home") + File.separator + "hutool-cli"
    private const val ALIAS = "alias"
    private const val PARAM_KEY = "paramTypes"
    private const val VERSION = "v1.3"
    private val ALIAS_CACHE: MutableMap<String, JSONObject?> = HashMap(4, 1f)
    lateinit var ARG: MethodArg
    var result: Any? = null
    var homeDir = System.getenv("HUTOOL_PATH")
    var resultString: String? = null
    private var omitParamType = true
    private var commander: JCommander? = null
    private var resultContainer: MutableList<String?>? = null
    val simpleDateFormat: SimpleDateFormat by lazy { SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS") }

    private var outputConverter: IOConverter? = null
    val isDebug: Boolean
        get() = Objects.nonNull(ARG) && ARG!!.debug

    private fun resolveCmd(args: Array<String>): String? {
        commander = JCommander.newBuilder().addObject(ARG).build()
        commander!!.setProgramName("hutool-cli")
        try {
            commander!!.parse(*args)
        } catch (e: Exception) {
            seeUsage()
            return ""
        }
        if (isDebug && ARG!!.exception) {
            throw CliException()
        }
        if (ARG!!.version) {
            return "hutool-cli: " + VERSION
        } else {
            debugOutput("hutool-cli: %s", VERSION)
        }
        debugOutput("received arguments: %s", ArrayUtil.toString(args))
        debugOutput("starting resolve")
        ARG!!.command.addAll(ARG!!.main)
        resolveResult()
        debugOutput("result handled success")
        resultString = convertResult()
        if (ARG!!.copy) {
            debugOutput("copying result into clipboard")
            ClipboardUtil.setStr(resultString)
            debugOutput("result copied")
        }
        if (isDebug) {
            println()
        }
        return resultString
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
        resultContainer = null
        // 使用字符串 `//` 切割多条命令
        for (arg in args) {
            if ("//" == arg) {
                // 处理上条命令，并记录结果
                val res = resolveCmd(list.toTypedArray())
                if (isDebug) {
                    println(res)
                }
                if (Objects.isNull(resultContainer)) {
                    resultContainer = ArrayList(5)
                }
                resultContainer!!.add(res)
                list.clear()
                val methodArg = ARG
                ARG = MethodArg()
                ARG!!.workDir = methodArg!!.workDir
            } else {
                list.add(arg)
            }
        }
        if (!list.isEmpty()) {
            println()
            println(resolveCmd(list.toTypedArray()))
        }
    }

    private fun resolveResult() {
        var fixClassName = true
        if (!isCollectionEmpty(ARG!!.command)) {
            val command = ARG!!.command[0]
            debugOutput("get command: %s", command)
            ARG!!.params.addAll(ARG!!.command.subList(1, ARG!!.command.size))
            if (ALIAS == command) {
                seeAlias("", COMMAND_JSON)
                return
            }
            val sharp = '#'
            var idx = command.indexOf(sharp)
            if (idx > 0) {
                // 非类方法别名，使用类别名和方法别名调用
                debugOutput("invoke use class name and method name combined in command mode")
                ARG!!.className = command.substring(0, idx)
                ARG!!.methodName = command.substring(idx + 1)
                resolveResultByClassMethod(true)
                return
            }

            // 从命令文件中找到类名和方法名以及参数类型，默认值
            val methodKey = "method"
            val aliasJson = getAlias(command, "", COMMAND_JSON)
            val methodJson = aliasJson!!.getJSONObject(command)
            if (Objects.isNull(methodJson) || !methodJson.containsKey(methodKey)) {
                result = "command[$command] not found!"
                return
            }
            val classMethod = methodJson.getString(methodKey)
            idx = classMethod.lastIndexOf(sharp)
            if (idx < 1) {
                result = "method[$classMethod] format error, required: com.example.Main#main"
                return
            }
            debugOutput("parse method to class name and method name")
            ARG!!.className = classMethod.substring(0, idx)
            ARG!!.methodName = classMethod.substring(idx + 1)
            parseMethod(methodJson)
            debugOutput("get method: %s", ARG!!.methodName)
            omitParamType = false
            fixClassName = omitParamType
        }
        resolveResultByClassMethod(fixClassName)
    }

    private fun resolveResultByClassMethod(fixName: Boolean) {
        var fixName = fixName
        if (isStringEmpty(ARG!!.className)) {
            seeUsage()
            return
        }
        if (ALIAS == ARG!!.className) {
            seeAlias("", CLASS_JSON)
            return
        }
        var methodAliasPaths: List<String>? = null
        if (fixName) {
            // 尝试从类别名文件中查找类全名
            val methodAliasKey = "methodAliasPaths"
            val aliasJson = getAlias(ARG!!.className, "", CLASS_JSON)
            val clazzJson = aliasJson!!.getJSONObject(ARG!!.className)
            if (Objects.isNull(clazzJson) || !clazzJson.containsKey(CLAZZ_KEY)) {
                fixName = false
            } else {
                ARG!!.className = clazzJson.getString(CLAZZ_KEY)
                debugOutput("find class alias: %s", ARG!!.className)
                methodAliasPaths = clazzJson.getObject(methodAliasKey, object : TypeReference<List<String?>?>() {})
            }
        }
        val clazz: Class<*>?
        debugOutput("loading class: %s", ARG!!.className)
        try {
            clazz = parseClass(ARG!!.className!!)
        } catch (e: Exception) {
            debugOutput(ExceptionUtil.stacktraceToString(e, Int.MAX_VALUE))
            ARG!!.className = ""
            resolveResultByClassMethod(false)
            return
        }
        if (clazz == Hutool::class.java) {
            result = "class not support: org.code4everything.hutool.Hutool"
            return
        }
        debugOutput("load class success")
        resolveResultByClassMethod(clazz, fixName, methodAliasPaths)
    }

    private fun resolveResultByClassMethod(clazz: Class<*>?, fixName: Boolean, methodAliasPaths: List<String>?) {
        if (isStringEmpty(ARG!!.methodName)) {
            seeUsage()
            return
        }
        if (ALIAS == ARG!!.methodName) {
            if (!isCollectionEmpty(methodAliasPaths)) {
                seeAlias(clazz!!.name, *methodAliasPaths!!.toTypedArray())
            }
            return
        }
        fixMethodName(fixName, methodAliasPaths)

        // 将剪贴板字符内容注入到方法参数的指定索引位置
        if (ARG!!.paramIdxFromClipboard >= 0) {
            ARG!!.params.add(Math.min(ARG!!.params.size, ARG!!.paramIdxFromClipboard), ClipboardUtil.getStr())
        }
        val method: Method?
        if (omitParamType && isCollectionEmpty(ARG!!.paramTypes) && !isCollectionEmpty(ARG!!.params)) {
            // 缺省方法参数类型，自动匹配方法
            debugOutput("getting method ignore case by method name and param count")
            method = autoMatchMethod(clazz)
        } else {
            debugOutput("parsing parameter types")
            val paramTypes = arrayOfNulls<Class<*>?>(ARG!!.paramTypes.size)
            val parseDefaultValue = ARG!!.params.size < paramTypes.size
            for (i in ARG!!.paramTypes.indices) {
                // 解析默认值，默认值要么都填写，要么都不填写
                val paramType = parseParamType(i, ARG!!.paramTypes[i], parseDefaultValue)
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
            method = ReflectUtil.getMethod(clazz, true, ARG!!.methodName, *paramTypes)
        }
        if (Objects.isNull(method) || !Modifier.isPublic(method!!.modifiers)) {
            val msg = "static method not found(ignore case) or is not a public method: %s#%s(%s)"
            val paramTypeArray = ARG!!.paramTypes.toTypedArray()
            debugOutput(msg, clazz!!.name, ARG!!.methodName, ArrayUtil.join(paramTypeArray, ", "))
            ARG!!.methodName = ""
            resolveResultByClassMethod(clazz, fixName, methodAliasPaths)
            return
        }
        val parameters = method.parameters
        outputConverter = method!!.getAnnotation(IOConverter::class.java)
        debugOutput("get method success")
        if (ARG!!.params.size < parameters.size) {
            ARG!!.params.add(ClipboardUtil.getStr())
        }
        if (ARG!!.params.size < parameters.size) {
            try {
                val methodFullInfo = parseMethodFullInfo(clazz!!.name, method.name, ARG!!.paramTypes)
                result = "parameter error, method request: $methodFullInfo"
            } catch (e: Exception) {
                val paramTypeArray = parameters.map { x: Parameter -> x.type.name }.toTypedArray()
                result = "parameter error, required: (" + ArrayUtil.join(paramTypeArray, ", ") + ")"
            }
            return
        }

        // 转换参数类型
        debugOutput("casting parameter to class type")
        val params = arrayOfNulls<Any>(parameters.size)
        val paramJoiner = StringJoiner(", ")
        for (i in parameters.indices) {
            val param = ARG!!.params[i]
            paramJoiner.add(param)
            val parameter = parameters[i]
            params[i] = castParam2JavaType(parameter.getAnnotation(IOConverter::class.java), param, parameter.type, true)
        }
        debugOutput("cast parameter success")
        debugOutput("invoking method: %s#%s(%s)", clazz!!.name, method.name, paramJoiner)
        result = ReflectUtil.invokeStatic(method, *params)
        debugOutput("invoke method success")
    }

    private fun parseParamType(index: Int, paramType: String, parseDefaultValue: Boolean): String {
        var parseDefaultValue = parseDefaultValue
        val idx = paramType.indexOf('=')
        if (idx < 1) {
            return paramType
        }
        val type: String
        if (paramType[0] == '@') {
            parseDefaultValue = true
            type = paramType.substring(1, idx)
        } else {
            type = paramType.substring(0, idx)
        }

        // 解析默认值
        if (parseDefaultValue) {
            val param = paramType.substring(idx + 1)
            ARG!!.params.add(Math.min(index, ARG!!.params.size), param)
        }
        return type
    }

    private fun autoMatchMethod(clazz: Class<*>?): Method? {
        // 找到与方法名一致的方法（忽略大小写）
        val methods = clazz!!.methods
        val fuzzyList: MutableList<Method> = ArrayList()
        for (method in methods) {
            val modifiers = method.modifiers
            if (!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers)) {
                continue
            }
            if (method.name.equals(ARG!!.methodName, ignoreCase = true)) {
                fuzzyList.add(method)
            }
        }
        if (isCollectionEmpty(fuzzyList)) {
            return null
        }

        // 找到离参数个数最相近的方法
        val paramSize = ARG!!.params.size
        fuzzyList.sortWith(Comparator.comparingInt { it.parameterCount })
        var method: Method? = null
        for (m in fuzzyList) {
            if (Objects.isNull(method)) {
                method = m
            }
            if (m.parameterCount > paramSize) {
                break
            }
            method = m
        }

        // 确定方法参数类型
        if (Objects.nonNull(method)) {
            ARG!!.paramTypes = ArrayList()
            for (paramType in method!!.parameterTypes) {
                ARG!!.paramTypes.add(paramType.name)
            }
        }
        return method
    }

    fun castParam2JavaType(inputConverter: IOConverter?, param: String, type: Class<*>, replace: Boolean): Any? {
        var param = param
        if ("nil" == param) {
            return null
        }
        if (replace && resultContainer != null) {
            // 替换连续命令中的结果记录值，格式：\\0,\\1,\\2...
            val endIds = resultContainer!!.size - 1
            for (i in 0..endIds) {
                val key = "\\\\" + i
                val value = resultContainer!![i]
                param = param.replace(key, value!!)
                if (i == endIds) {
                    param = param.replace("\\\\p", value)
                }
            }
        }
        if (Objects.isNull(inputConverter) && CharSequence::class.java.isAssignableFrom(type)) {
            return param
        }

        // 转换参数类型
        var converter: Converter<*>? = null
        try {
            converter = getConverter(inputConverter!!, type)
            if (converter != null) {
                debugOutput("cast param[%s] using converter: %s", param, converter.javaClass.name)
                return converter.string2Object(param)
            }
        } catch (e: Exception) {
            Objects.requireNonNull(converter)
            debugOutput("cast param[%s] to type[%s] using converter[%s] failed: %s", param, type.name, converter!!.javaClass.name, ExceptionUtil.stacktraceToString(e, Int.MAX_VALUE))
        }
        debugOutput("auto convert param[%s] to type: %s", param, type.name)
        return TypeUtils.cast(param, type, null)
    }

    @Throws(Exception::class)
    private fun getConverter(inputConverter: IOConverter, type: Class<*>): Converter<*>? {
        if (Objects.nonNull(inputConverter)) {
            return Converter.getConverter(inputConverter, type)
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
        val converterName = getAlias("", homeDir, CONVERTER_JSON)!!.getString(type.name)
        return if (isStringEmpty(converterName)) {
            null
        } else newConverter(parseClass(converterName) as Class<out Converter<*>?>?, type)
    }

    private fun fixMethodName(fixName: Boolean, methodAliasPaths: List<String?>?) {
        // 从方法别名文件中找到方法名
        if (fixName && !isCollectionEmpty(methodAliasPaths)) {
            val methodKey = "methodName"
            val aliasJson = getAlias(ARG!!.methodName, "", *methodAliasPaths!!.toTypedArray())
            val methodJson = aliasJson!!.getJSONObject(ARG!!.methodName)
            if (Objects.nonNull(methodJson)) {
                val methodName = methodJson.getString(methodKey)
                if (!isStringEmpty(methodName)) {
                    ARG!!.methodName = methodName
                }
                parseMethod(methodJson)
                debugOutput("get method name: %s", ARG!!.methodName)
                omitParamType = false
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
        commander!!.usage()
    }

    private fun seeAlias(className: String, vararg paths: String) {
        // 用户自定义别名会覆盖工作目录定义的别名
        val aliasJson = getAlias("", homeDir, *paths)
        aliasJson!!.putAll(getAlias("", "", *paths)!!)
        if (!isCollectionEmpty(ARG!!.params)) {
            val iterator: MutableIterator<Map.Entry<String, Any>> = aliasJson.entries.iterator()
            while (iterator.hasNext()) {
                val key = iterator.next().key
                if (ARG!!.params.stream().noneMatch { s: String? -> key.contains(s!!) }) {
                    iterator.remove()
                }
            }
        }
        val joiner = StringJoiner("\n")
        val maxLength = Holder.of(0)
        val map: MutableMap<String, String> = TreeMap()
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
                ARG!!.methodName = methodName
                parseMethod(json)
                try {
                    // 拿到方法的形参名称，参数类型
                    map[k] = parseMethodFullInfo(className, ARG!!.methodName, ARG!!.paramTypes)
                } catch (e: Exception) {
                    // 这里只能输出方法参数类型，无法输出形参类型
                    debugOutput("parse method param name error: %s", ExceptionUtil.stacktraceToString(e, Int.MAX_VALUE))
                    val typeString = ArrayUtil.join(ARG!!.paramTypes.toTypedArray(), ", ")
                    map[k] = ARG!!.methodName + "(" + typeString + ")"
                }
            }
        })

        // 输出别名到终端
        debugOutput("max length: %s", maxLength.get())
        map.forEach { (k: String?, v: String) -> joiner.add(padAfter(k, maxLength.get(), ' ') + " = " + v) }
        result = joiner.toString()
    }

    private fun parseMethod(json: JSONObject) {
        if (ARG!!.methodName!!.endsWith(")")) {
            val idx = ARG!!.methodName!!.indexOf("(")
            if (idx < 1) {
                debugOutput("method format error: %s", ARG!!.methodName)
                return
            }
            val split = ARG!!.methodName!!.substring(idx + 1, ARG!!.methodName!!.length - 1).split(",").toTypedArray()
            ARG!!.paramTypes = Arrays.stream(split).filter { it?.isNotEmpty() ?: false }.collect(Collectors.toList())
            ARG!!.methodName = ARG!!.methodName!!.substring(0, idx)
        } else if (Objects.nonNull(json)) {
            val paramTypes = json.getObject<ArrayList<String>>(PARAM_KEY, object : TypeReference<ArrayList<String>?>() {})
            ARG!!.paramTypes = paramTypes ?: Collections.emptyList()
        }
        if (Objects.isNull(json)) {
            return
        }

        // 重载方法
        val specificTypes = json[ARG!!.params.size.toString() + "param"] ?: return
        if (specificTypes is List<*>) {
            ARG!!.paramTypes = specificTypes.stream().map { it.toString() }.collect(Collectors.toList())
        } else {
            ARG!!.paramTypes = Arrays.asList(*specificTypes.toString().split(",").toTypedArray())
        }
    }

    @Throws(NotFoundException::class)
    private fun parseMethodFullInfo(className: String, methodName: String?, paramTypes: List<String>): String {
        var mn = methodName
        var outClassName = false
        val pool = ClassPool.getDefault()
        val ctClass: CtClass
        if (isStringEmpty(className)) {
            // 来自类方法别名
            val idx = methodName!!.indexOf("#")
            ctClass = pool[parseClassName(methodName.substring(0, idx))]
            mn = methodName.substring(idx + 1)
            outClassName = true
        } else {
            // 来自方法别名
            ctClass = pool[className]
        }

        // 用javassist库解析形参类型
        val params = arrayOfNulls<CtClass>(paramTypes.size)
        val defaultValueMap: MutableMap<String?, String?> = HashMap(4, 1f)
        for (i in paramTypes.indices) {
            var paramTypeClass = paramTypes[i]
            val idx = paramTypeClass.indexOf('=')
            if (idx > 0) {
                val old = paramTypeClass
                val typeName = old.substring(if (old[0] == '@') 1 else 0, idx)
                paramTypeClass = parseClassName(typeName)
                defaultValueMap[paramTypeClass + i] = old.substring(idx + 1)
            } else {
                paramTypeClass = parseClassName(paramTypeClass)
            }
            params[i] = pool[paramTypeClass]
        }
        val ctMethod = ctClass.getDeclaredMethod(mn, params)
        return (if (outClassName) ctClass.name + "#" else "") + getMethodFullInfo(ctMethod, defaultValueMap)
    }

    private fun convertResult(): String {
        debugOutput("converting result")
        try {
            val res = convertResult(result, outputConverter)
            debugOutput("result convert success")
            return res
        } catch (e: Exception) {
            debugOutput("convert result error: %s", e.message)
        }
        return ""
    }

    @Throws(Exception::class)
    fun convertResult(obj: Any?, ioConverter: IOConverter?): String {
        if (Objects.isNull(obj)) {
            return ""
        }
        val resClass: Class<*> = obj!!.javaClass
        if (Objects.isNull(ioConverter)) {
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
            if (obj.javaClass.isArray) {
                return ArrayConverter(resClass).object2String(obj)
            }
        }
        if (Objects.nonNull(ioConverter)) {
            return Converter.getConverter(ioConverter!!, resClass)!!.object2String(obj)
        }
        val name = resClass.name
        val converterJson = getAlias("", homeDir, CONVERTER_JSON)
        val converterName = converterJson!!.getString(name)
        return if (isStringEmpty(converterName)) {
            ObjectUtil.toString(obj)
        } else newConverter(parseClass(converterName) as Class<out Converter<*>?>?, resClass)!!.object2String(obj)
    }

    fun getAlias(aliasKey: String?, parentDir: String?, vararg paths: String?): JSONObject {
        // 先查找用户自定义别名，没找到再从工作目录查找
        var parentDir = parentDir
        if (isStringEmpty(parentDir)) {
            parentDir = HUTOOL_USER_HOME
        }
        val path = Paths.get(parentDir, *paths).toAbsolutePath().normalize().toString()
        debugOutput("alias json file path: %s", path)
        var json = ALIAS_CACHE[path]
        if (json == null) {
            json = if (FileUtil.exist(path)) {
                JSON.parseObject(FileUtil.readUtf8String(path))
            } else {
                JSONObject()
            }
            ALIAS_CACHE[path] = json
        }
        return if (isStringEmpty(aliasKey) || json!!.containsKey(aliasKey)) {
            json!!
        } else getAlias("", homeDir, *paths)
    }

    fun debugOutput(msg: String, vararg params: Any?) {
        var msg = msg
        if (isDebug) {
            val stackTrace = Thread.currentThread().stackTrace
            val className = if (stackTrace.size > 2) stackTrace[2].className else "Unknown"
            val lineNumber = if (stackTrace.size > 2) stackTrace[2].lineNumber.toString() else "NaN"
            if (params != null && params.size > 0) {
                msg = String.format(msg, *params)
            }
            msg = simpleDateFormat!!.format(Date()) + " " + className + ":" + lineNumber + " - " + msg
            println(msg)
        }
    }

    fun test(vararg args: String): String? {
        val cmd = java.lang.String.join(" ", *args)
        val cs = CharArray(cmd.length + 11)
        Arrays.fill(cs, '=')
        cs[0] = '\n'
        val separator = String(cs)
        println("$separator\n>> hu $cmd <<$separator")
        var idx = args.size
        val newArgs = Arrays.copyOf(args, idx + 2)
        newArgs[idx++] = "--work-dir"
        newArgs[idx] = Paths.get(".").toAbsolutePath().normalize().toString()
        main(newArgs)
        return resultString
    }
}
