package org.code4everything.hutool

import cn.hutool.core.comparator.ComparatorChain
import cn.hutool.core.date.ChineseDate
import cn.hutool.core.date.DateTime
import cn.hutool.core.date.DateUtil
import cn.hutool.core.exceptions.ExceptionUtil
import cn.hutool.core.io.FileUtil
import cn.hutool.core.lang.Holder
import cn.hutool.core.lang.JarClassLoader
import cn.hutool.core.math.Calculator
import cn.hutool.core.util.ClassUtil
import cn.hutool.core.util.RandomUtil
import cn.hutool.core.util.ReUtil
import cn.hutool.core.util.ReflectUtil
import com.alibaba.fastjson.JSONObject
import java.io.File
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Parameter
import java.util.Arrays
import java.util.Date
import java.util.StringJoiner
import java.util.concurrent.FutureTask
import java.util.regex.Pattern
import java.util.stream.Collectors
import javassist.ClassPool
import javassist.bytecode.LocalVariableAttribute
import kotlin.math.ceil
import kotlin.streams.toList
import org.code4everything.hutool.converter.ClassConverter
import org.code4everything.hutool.converter.DateConverter
import org.code4everything.hutool.converter.FileConverter
import org.code4everything.hutool.converter.JsonObjectConverter
import org.code4everything.hutool.converter.LineSepConverter
import org.code4everything.hutool.converter.ListStringConverter
import org.code4everything.hutool.converter.PatternConverter

object Utils {

    private val classAliasJson by lazy { Hutool.getAlias(Hutool.CLASS_JSON) }
    private val mvnRepositoryHome by lazy { listOf("~", ".m2", "repository") }
    private val clazzCache by lazy { HashMap<String, Class<*>>(4, 1f) }
    private val classNameCache by lazy { HashMap<String, String>(4, 1f) }

    internal var classLoader: JarClassLoader? = null

    /**
     * 可在别名文件中自定义输入输出转换器
     */
    @JvmStatic
    fun power(any: Any?): Any? = any

    @JvmStatic
    @IOConverter(LineSepConverter::class)
    fun getConverters(): List<String> {
        return ClassUtil.scanPackage().filter { !it.isInterface && Converter::class.java.isAssignableFrom(it) }.map { it.name }.sorted()
    }

    @JvmStatic
    @HelpInfo(helps = ["see day month year progress"])
    fun dayProcess(@IOConverter(DateConverter::class) specificDate: DateTime): String {
        val date = DateUtil.beginOfDay(specificDate)
        val todayProcess = (specificDate.time - date.time) * 100 / 24.0 / 60 / 60 / 1000
        val weekEnum = DateUtil.dayOfWeekEnum(date)
        var week = weekEnum.value - 1
        week = (if (week == 0) 7 else week) * 24 - 24

        val weekProcess = (week + specificDate.hour(true)) * 100 / 7.0 / 24
        val monthProcess = DateUtil.dayOfMonth(date) * 100 / DateUtil.endOfMonth(date).dayOfMonth().toDouble()
        val yearProcess = DateUtil.dayOfYear(date) * 100 / DateUtil.endOfYear(date).dayOfYear().toDouble()
        var template = String.format(
            "%s %s %s%n", lunar(specificDate), weekEnum.toChinese("周"), Hutool.simpleDateFormat.format(specificDate)
        )

        template += String.format("%n今日 [%s]: %05.2f%%", getDayProcessString(todayProcess), todayProcess)
        template += String.format("%n本周 [%s]: %05.2f%%", getDayProcessString(weekProcess), weekProcess)
        template += String.format("%n本月 [%s]: %05.2f%%", getDayProcessString(monthProcess), monthProcess)
        return template + String.format("%n本年 [%s]: %05.2f%%", getDayProcessString(yearProcess), yearProcess)
    }

    @JvmStatic
    private fun getDayProcessString(@IOConverter process: Double): String {
        val p = (ceil(process * 100) / 100).toInt()
        val cs = CharArray(100)
        Arrays.fill(cs, 0, p, 'o')
        Arrays.fill(cs, p, 100, ' ')
        return String(cs)
    }

    @JvmStatic
    fun toHttpUrlString(paramMap: Map<String?, *>?): String {
        if (paramMap == null || paramMap.isEmpty()) {
            return ""
        }

        val sb = StringBuilder()
        var sep = "?"
        for ((k, v) in paramMap) {
            val value = v?.toString()
            if (isStringEmpty(value)) {
                continue
            }
            sb.append(sep).append(k).append("=").append(value).also { sep = "&" }
        }
        return sb.toString()
    }

    @JvmStatic
    @IOConverter(JsonObjectConverter::class)
    fun mergeJson(@IOConverter(JsonObjectConverter::class) master: JSONObject, @IOConverter(JsonObjectConverter::class) second: JSONObject): JSONObject {
        if (master.isEmpty()) {
            return second
        }

        second.entries.forEach {
            val key = it.key
            val value = it.value
            if (!master.containsKey(key)) {
                // 不存在时直接新增
                master[key] = value
            }

            val obj = master[key]
            if (obj is JSONObject && value is JSONObject) {
                // 都是JsonObject时，继续深入合并，否则跳过
                master[key] = mergeJson(obj, value)
            }
        }
        return master
    }

    @JvmStatic
    @IOConverter(LineSepConverter::class)
    fun getFieldNames(@IOConverter(ClassConverter::class) clazz: Class<*>): List<String> {
        var clz = clazz
        if (clz.isPrimitive) {
            clz = parseClass0(parseClassName0("j." + clz.name))
        }

        val fields = ReflectUtil.getFields(clz)
        val result = arrayListOf<String>()
        val modifierMap: MutableMap<String, List<String>> = HashMap()
        val comparators = ComparatorChain<String>().apply {
            addComparator(Comparator.comparingInt { o ->
                modifierMap.computeIfAbsent(o) { s ->
                    val ss = s.split(" ").toTypedArray()
                    if (ss.size > 2) Arrays.stream(ss).limit(ss.size - 2L).collect(Collectors.toList()) else emptyList()
                }.size
            })
            addComparator(Comparator.naturalOrder())
        }

        val modifierList: MutableList<String> = ArrayList()
        val holder = Holder.of("")
        val filter = MethodArg.getSubParams(Hutool.ARG, 1)
        Arrays.stream(fields).filter { e: Field ->
            if (isCollectionEmpty(filter)) {
                return@filter true
            }
            val fieldName = e.name.lowercase()
            filter.stream().anyMatch { fieldName.contains(it) }
        }.map { field: Field ->
            var line = ""
            val modifiers = field.modifiers
            if (Modifier.isPrivate(modifiers)) {
                line += "private "
            } else if (Modifier.isProtected(modifiers)) {
                line += "protected "
            } else if (Modifier.isPublic(modifiers)) {
                line += "public "
            }
            if (Modifier.isStatic(modifiers)) {
                line += "static "
            }
            if (Modifier.isFinal(modifiers)) {
                line += "final "
            }
            if (Modifier.isTransient(modifiers)) {
                line += "transient "
            }
            if (Modifier.isVolatile(modifiers)) {
                line += "volatile "
            }
            line + field.type.simpleName + " " + field.name
        }.sorted(comparators).forEach { s: String ->
            var line = s
            val list = modifierMap[s]!!
            if (modifierList != list) {
                line = holder.get().toString() + line
                modifierList.clear()
                modifierList.addAll(list)
            }
            result.add(line)
            holder.set("\n")
        }
        return result
    }

    @JvmStatic
    fun getStaticFieldValue(@IOConverter(ClassConverter::class) clazz: Class<*>, fieldName: String?): Any {
        var clz = clazz
        if (clz.isPrimitive) {
            clz = parseClass0(parseClassName0("j." + clz.name))
        }
        val field = ReflectUtil.getField(clz, fieldName)
        return ReflectUtil.getStaticFieldValue(field)
    }

    @JvmStatic
    @IOConverter(ListStringConverter::class)
    fun getMatchedItems(@IOConverter(PatternConverter::class) regex: Pattern, content: String?): List<String> {
        val result = ArrayList<String>()
        val matcher = regex.matcher(content ?: "")
        while (matcher.find()) {
            result.add(matcher.group(0))
        }
        return result
    }

    @JvmStatic
    fun getSupperClass(@IOConverter(ClassConverter::class) clazz: Class<*>?): String {
        val joiner = StringJoiner("\n")
        getSupperClass(joiner, "", clazz)
        return joiner.toString()
    }

    @JvmStatic
    private fun getSupperClass(joiner: StringJoiner, prefix: String, clazz: Class<*>?) {
        if (clazz == null || Any::class.java == clazz) {
            return
        }

        joiner.add(prefix + (if (clazz.isInterface) "<>" else "") + clazz.name)
        getSupperClass(joiner, "$prefix|${" ".repeat(4)}", clazz.superclass)
        for (anInterface in clazz.interfaces) {
            getSupperClass(joiner, "$prefix|${" ".repeat(4)}", anInterface)
        }
    }

    @JvmStatic
    fun calc(expression: String?, @IOConverter scale: Int): String {
        val res = Calculator.conversion(expression)
        return String.format("%.${scale}f", res)
    }

    @JvmStatic
    fun lunar(@IOConverter(DateConverter::class) date: Date?): String = ChineseDate(date).toString()

    @JvmStatic
    fun toUpperCase(str: String?): String = str?.uppercase() ?: ""

    @JvmStatic
    fun toLowerCase(str: String?): String = str?.lowercase() ?: ""

    @JvmStatic
    internal fun syncRun(future: FutureTask<Any>, classloader: ClassLoader): Any {
        val t = Thread(future)
        t.name = "thread-" + RandomUtil.randomString(3).lowercase()
        t.contextClassLoader = classloader
        t.start()
        return future.get()
    }

    @JvmStatic
    @IOConverter(LineSepConverter::class)
    fun grep(
        @IOConverter(PatternConverter::class) pattern: Pattern,
        @IOConverter(ListStringConverter::class) lines: List<String>?
    ): List<String> {
        var line = lines
        if (isCollectionEmpty(line) && !isStringEmpty(Hutool.resultString.get())) {
            line = ListStringConverter().useLineSep().string2Object(Hutool.resultString.get())
        }
        return line!!.filter { pattern.matcher(it).find() }
    }

    @JvmStatic
    fun <T> isArrayEmpty(arr: Array<T>?): Boolean = arr?.isEmpty() ?: true

    @JvmStatic
    @IOConverter(ClassConverter::class)
    internal fun parseClass(className: String): Class<*> {
        return clazzCache.computeIfAbsent(className) {
            when (it) {
                "bool", "boolean" -> Boolean::class.javaPrimitiveType
                "byte" -> Byte::class.javaPrimitiveType
                "short" -> Short::class.javaPrimitiveType
                "char" -> Char::class.javaPrimitiveType
                "int" -> Int::class.javaPrimitiveType
                "long" -> Long::class.javaPrimitiveType
                "float" -> Float::class.javaPrimitiveType
                "double" -> Double::class.javaPrimitiveType
                else -> parseClass0(className)
            }!!
        }
    }

    @JvmStatic
    private fun parseClass0(className: String): Class<*> {
        var cn = className
        cn = parseClassName0(cn)
        try {
            return Class.forName(cn)
        } catch (e: Exception) {
            // ignore
        }

        Hutool.debugOutput("loading class: %s", cn)

        if (classLoader == null) {
            classLoader = JarClassLoader()
            classLoader!!.addJar(FileUtil.file(Hutool.homeDir, "external"))
            val externalConf = FileUtil.file(Hutool.homeDir, "external.conf")
            if (FileUtil.exist(externalConf)) {
                val externalPaths = FileUtil.readUtf8String(externalConf).split(Pattern.compile("[\n,]"))
                for (externalPath in externalPaths) {
                    val external = parseClasspath(externalPath)
                    if (FileUtil.exist(external)) {
                        Hutool.debugOutput("add class path: %s", external!!.absolutePath)
                        classLoader!!.addURL(external)
                    }
                }
            }
        }

        return classLoader!!.loadClass(cn)
    }

    @JvmStatic
    @IOConverter(FileConverter::class)
    private fun parseClasspath(path: String): File? {
        var p = path
        p = p.trim().trimEnd(',')
        if (p.isEmpty() || p.startsWith("//")) {
            return null
        }

        if (p.startsWith("mvn:")) {
            p = p.substring(4)
            if (isStringEmpty(p)) {
                return null
            }
            val coordinates = p.split(Pattern.compile(":"), 3).toTypedArray()
            if (coordinates.size != 3) {
                Hutool.debugOutput("mvn coordinate format error: %s", p)
                return null
            }

            val paths = ArrayList(mvnRepositoryHome)
            paths.addAll(coordinates[0].split('.'))
            val name = coordinates[1]
            val version = coordinates[2]
            paths.add(name)
            paths.add(version)
            paths.add("$name-$version.jar")
            val file = FileUtil.file(*paths.toTypedArray())
            Hutool.debugOutput("get mvn path: %s", file.absolutePath)
            return file
        }

        return FileUtil.file(p)
    }

    @JvmStatic
    fun parseClassName(className: String): String {
        return classNameCache.computeIfAbsent(className) {
            when (it) {
                "bool", "boolean" -> "boolean"
                "byte" -> "byte"
                "short" -> "short"
                "char" -> "char"
                "int" -> "int"
                "long" -> "long"
                "float" -> "float"
                "double" -> "double"
                else -> parseClassName0(className)
            }
        }
    }

    @JvmStatic
    private fun parseClassName0(className: String): String {
        return when (className) {
            "string" -> "java.lang.String"
            "object" -> "java.lang.Object"
            "j.char.seq" -> "java.lang.CharSequence"
            "file" -> "java.io.File"
            "charset" -> "java.nio.charset.Charset"
            "date" -> "java.util.Date"
            "class" -> "java.lang.Class"
            "j.bool", "j.boolean" -> "java.lang.Boolean"
            "j.byte" -> "java.lang.Byte"
            "j.short" -> "java.lang.Short"
            "j.int", "j.integer" -> "java.lang.Integer"
            "j.char" -> "java.lang.Character"
            "j.long" -> "java.lang.Long"
            "j.float" -> "java.lang.Float"
            "j.double" -> "java.lang.Double"
            "reg.pattern" -> "java.util.regex.Pattern"
            "map" -> "java.util.Map"
            "list" -> "java.util.List"
            "set" -> "java.util.Set"
            else -> parseClassName00(className)
        }
    }

    @JvmStatic
    private fun parseClassName00(className: String): String {
        var cn = className
        if (cn.endsWith(';') && cn.contains("[L")) {
            val idx = cn.indexOf("L")
            val prefix = cn.substring(0, idx + 1)
            val name = parseClassName(cn.substring(idx + 1, cn.length - 1))
            return "$prefix$name;"
        }

        if (cn.length > 16) {
            return cn
        }

        var nameFromAlias = false
        val classJson = classAliasJson.getJSONObject(cn)
        if (classJson != null) {
            val className0 = classJson.getString(Hutool.CLAZZ_KEY)
            if (!isStringEmpty(className0)) {
                cn = className0
                nameFromAlias = true
            }
        }

        if (nameFromAlias || cn.startsWith('@')) {
            return cn.removePrefix("@")
        }
        return parseClassName00("@$cn")
    }

    @JvmStatic
    fun isStringEmpty(str: String?): Boolean = str?.isEmpty() ?: true

    @JvmStatic
    fun <T> isCollectionEmpty(collection: Collection<T>?): Boolean = collection?.isEmpty() ?: true

    fun addPrefixIfNot(str: String, prefix: String): String {
        return if (isStringEmpty(str) || isStringEmpty(prefix) || str.startsWith(prefix)) str else prefix + str
    }

    @JvmStatic
    fun addSuffixIfNot(str: String, suffix: String): String {
        return if (isStringEmpty(str) || isStringEmpty(suffix) || str.endsWith(suffix)) str else str + suffix
    }

    @JvmStatic
    @IOConverter(LineSepConverter::class)
    fun outputPublicStaticMethods(className: String): List<String> {
        if (isStringEmpty(className)) {
            return emptyList()
        }

        val filter = MethodArg.getSubParams(Hutool.ARG, 1).map { it.lowercase() }
        return outputPublicStaticMethods0(className, filter)
    }

    @JvmStatic
    @IOConverter(LineSepConverter::class)
    internal fun outputPublicStaticMethods0(className: String, filter: List<String>, forceEquals: Boolean = false): List<String> {
        try {
            val clazz = parseClass(className)
            val methods = clazz.methods
            val lineList: MutableList<String> = ArrayList(methods.size)
            for (method in methods) {
                val modifiers = method.modifiers
                if (!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers)) {
                    continue
                }
                if (filter.isNotEmpty()) {
                    val methodName = method.name.lowercase()
                    if (filter.stream().noneMatch { if (forceEquals) methodName == it else methodName.contains(it) }) {
                        continue
                    }
                }
                lineList.add(getMethodFullInfo(method, null))
            }
            return lineList.stream().sorted(String::compareTo).toList()
        } catch (e: Exception) {
            Hutool.debugOutput(
                "parse class static methods error: %s", ExceptionUtil.stacktraceToString(e, Int.MAX_VALUE)
            )
        }
        return emptyList()
    }

    @JvmStatic
    internal fun getMethodFullInfo(method: Method, defaultValueMap: Map<String?, String?>?): String {
        val parameters = method.parameters
        if (Hutool.isDebug) {
            val joiner = StringJoiner(",")
            Arrays.stream(parameters).forEach { joiner.add(it.type.name) }
            Hutool.debugOutput("parse method full info: %s#%s(%s)", method.declaringClass.name, method.name, joiner)
        }

        val paramJoiner = StringJoiner(", ")
        for (i in parameters.indices) {
            val parameter = parameters[i]
            val paramType = parameter.type.name
            val name = getParamName(parameter)
            var paramStr = "$name:$paramType"
            if (defaultValueMap != null) {
                val defaultValue = defaultValueMap[paramType + i]
                if (!isStringEmpty(defaultValue)) {
                    paramStr = "$paramStr=$defaultValue"
                }
            }
            paramJoiner.add(paramStr)
        }

        return "${method.name}($paramJoiner)"
    }

    private fun getParamName(parameter: Parameter): String {
        // 优先使用注解和Java8编译参数
        val param = parameter.getAnnotation(Param::class.java)
        val name = param?.value?.ifEmpty { param.name } ?: parameter.name

        val idx = name.removePrefix("arg")
        if (ReUtil.isMatch("[0-9]+", idx)) {
            // 无Java8编译参数时，使用javassist获取形参名称
            val method = parameter.declaringExecutable
            val pool = ClassPool.getDefault()
            val ctClass = pool[method.declaringClass.name]
            val ctMethod = ctClass.getDeclaredMethod(method.name, method.parameterTypes.map { pool[it.name] }.toTypedArray())
            val codeAttribute = ctMethod.methodInfo.codeAttribute
            val attribute = codeAttribute.getAttribute(LocalVariableAttribute.tag) as LocalVariableAttribute?
            return attribute!!.variableName(idx.toInt())
        }

        return name
    }
}
