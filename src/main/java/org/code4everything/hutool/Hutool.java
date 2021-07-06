package org.code4everything.hutool;

import cn.hutool.core.date.Week;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Holder;
import cn.hutool.core.swing.clipboard.ClipboardUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.log.dialect.console.ConsoleLog;
import cn.hutool.log.level.Level;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.util.TypeUtils;
import com.beust.jcommander.JCommander;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.code4everything.hutool.converter.ArrayConverter;
import org.code4everything.hutool.converter.CharsetConverter;
import org.code4everything.hutool.converter.DateConverter;
import org.code4everything.hutool.converter.FileConverter;
import org.code4everything.hutool.converter.ListStringConverter;
import org.code4everything.hutool.converter.MapConverter;
import org.code4everything.hutool.converter.PatternConverter;
import org.code4everything.hutool.converter.SetStringConverter;
import org.code4everything.hutool.converter.WeekConverter;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author pantao
 * @since 2020/10/27
 */
public final class Hutool {

    public static final String CLASS_JSON = "class.json";

    public static final String CONVERTER_JSON = "converter.json";

    public static final String CLAZZ_KEY = "clazz";

    static final String COMMAND_JSON = "command.json";

    static final String HUTOOL_USER_HOME = System.getProperty("user.home") + File.separator + "hutool-cli";

    private static final String ALIAS = "alias";

    private static final String PARAM_KEY = "paramTypes";

    private static final String VERSION = "v1.3";

    private static final Map<String, JSONObject> ALIAS_CACHE = new HashMap<>(4, 1);

    public static MethodArg ARG;

    public static Object result;

    static String homeDir = System.getenv("HUTOOL_PATH");

    static String resultString;

    private static boolean omitParamType = true;

    private static JCommander commander;

    private static List<String> resultContainer = null;

    private static SimpleDateFormat simpleDateFormat = null;

    private static IOConverter outputConverter = null;

    private Hutool() {}

    public static SimpleDateFormat getSimpleDateFormat() {
        if (simpleDateFormat == null) {
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        }
        return simpleDateFormat;
    }

    public static boolean isDebug() {
        return Objects.nonNull(ARG) && ARG.debug;
    }

    private static String resolveCmd(String[] args) {
        commander = JCommander.newBuilder().addObject(ARG).build();
        commander.setProgramName("hutool-cli");

        try {
            commander.parse(args);
        } catch (Exception e) {
            seeUsage();
            return "";
        }

        if (isDebug() && ARG.exception) {
            throw new CliException();
        }

        if (ARG.version) {
            return "hutool-cli: " + VERSION;
        } else {
            debugOutput("hutool-cli: " + VERSION);
        }

        debugOutput("received arguments: " + ArrayUtil.toString(args));
        debugOutput("starting resolve");
        ARG.command.addAll(ARG.main);
        resolveResult();
        debugOutput("result handled success");

        resultString = convertResult();
        if (ARG.copy) {
            debugOutput("copying result into clipboard");
            ClipboardUtil.setStr(resultString);
            debugOutput("result copied");
        }
        if (isDebug()) {
            System.out.println();
        }
        return resultString;
    }

    public static void main(String[] args) {
        if (Utils.isArrayEmpty(args)) {
            seeUsage();
            return;
        }

        ConsoleLog.setLevel(Level.ERROR);
        ARG = new MethodArg();
        List<String> list = new ArrayList<>(8);
        resultContainer = null;
        // 使用字符串 `//` 切割多条命令
        for (String arg : args) {
            if ("//".equals(arg)) {
                // 处理上条命令，并记录结果
                String res = resolveCmd(list.toArray(new String[0]));
                if (isDebug()) {
                    System.out.println(res);
                }
                if (Objects.isNull(resultContainer)) {
                    resultContainer = new ArrayList<>(5);
                }
                resultContainer.add(res);
                list.clear();
                MethodArg methodArg = ARG;
                ARG = new MethodArg();
                ARG.workDir = methodArg.workDir;
            } else {
                list.add(arg);
            }
        }

        if (!list.isEmpty()) {
            System.out.println();
            System.out.println(resolveCmd(list.toArray(new String[0])));
        }
    }

    private static void resolveResult() {
        boolean fixClassName = true;

        if (!Utils.isCollectionEmpty(ARG.command)) {
            String command = ARG.command.get(0);
            debugOutput("get command: %s", command);
            if (ALIAS.equals(command)) {
                seeAlias("", COMMAND_JSON);
                return;
            }

            ARG.params.addAll(ARG.command.subList(1, ARG.command.size()));

            char sharp = '#';
            int idx = command.indexOf(sharp);
            if (idx > 0) {
                // 非类方法别名，使用类别名和方法别名调用
                debugOutput("invoke use class name and method name combined in command mode");
                ARG.className = command.substring(0, idx);
                ARG.methodName = command.substring(idx + 1);
                resolveResultByClassMethod(true);
                return;
            }

            // 从命令文件中找到类名和方法名以及参数类型，默认值
            String methodKey = "method";
            JSONObject aliasJson = getAlias(command, "", COMMAND_JSON);

            JSONObject methodJson = aliasJson.getJSONObject(command);
            if (Objects.isNull(methodJson) || !methodJson.containsKey(methodKey)) {
                result = "command[" + command + "] not found!";
                return;
            }

            String classMethod = methodJson.getString(methodKey);
            idx = classMethod.lastIndexOf(sharp);
            if (idx < 1) {
                result = "method[" + classMethod + "] format error, required: com.example.Main#main";
                return;
            }

            debugOutput("parse method to class name and method name");
            ARG.className = classMethod.substring(0, idx);
            ARG.methodName = classMethod.substring(idx + 1);
            parseMethod(methodJson);
            debugOutput("get method: %s", ARG.methodName);
            fixClassName = omitParamType = false;
        }

        resolveResultByClassMethod(fixClassName);
    }

    private static void resolveResultByClassMethod(boolean fixName) {
        if (Utils.isStringEmpty(ARG.className)) {
            seeUsage();
            return;
        }

        if (ALIAS.equals(ARG.className)) {
            seeAlias("", CLASS_JSON);
            return;
        }

        List<String> methodAliasPaths = null;
        if (fixName) {
            // 尝试从类别名文件中查找类全名
            String methodAliasKey = "methodAliasPaths";
            JSONObject aliasJson = getAlias(ARG.className, "", CLASS_JSON);

            JSONObject clazzJson = aliasJson.getJSONObject(ARG.className);
            if (Objects.isNull(clazzJson) || !clazzJson.containsKey(CLAZZ_KEY)) {
                fixName = false;
            } else {
                ARG.className = clazzJson.getString(CLAZZ_KEY);
                debugOutput("find class alias: %s", ARG.className);
                methodAliasPaths = clazzJson.getObject(methodAliasKey, new TypeReference<List<String>>() {});
            }
        }

        Class<?> clazz;
        debugOutput("loading class: %s", ARG.className);
        try {
            clazz = Utils.parseClass(ARG.className);
        } catch (Exception e) {
            debugOutput(ExceptionUtil.stacktraceToString(e, Integer.MAX_VALUE));
            ARG.className = "";
            resolveResultByClassMethod(false);
            return;
        }

        if (clazz == Hutool.class) {
            result = "class not support: org.code4everything.hutool.Hutool";
            return;
        }

        debugOutput("load class success");
        resolveResultByClassMethod(clazz, fixName, methodAliasPaths);
    }

    private static void resolveResultByClassMethod(Class<?> clazz, boolean fixName, List<String> methodAliasPaths) {
        if (Utils.isStringEmpty(ARG.methodName)) {
            seeUsage();
            return;
        }

        if (ALIAS.equals(ARG.methodName)) {
            if (!Utils.isCollectionEmpty(methodAliasPaths)) {
                seeAlias(clazz.getName(), methodAliasPaths.toArray(new String[0]));
            }
            return;
        }

        fixMethodName(fixName, methodAliasPaths);

        // 将剪贴板字符内容注入到方法参数的指定索引位置
        if (ARG.paramIdxFromClipboard >= 0) {
            ARG.params.add(Math.min(ARG.params.size(), ARG.paramIdxFromClipboard), ClipboardUtil.getStr());
        }

        Method method;
        if (omitParamType && Utils.isCollectionEmpty(ARG.paramTypes) && !Utils.isCollectionEmpty(ARG.params)) {
            // 缺省方法参数类型，自动匹配方法
            debugOutput("getting method ignore case by method name and param count");
            method = autoMatchMethod(clazz);
        } else {
            debugOutput("parsing parameter types");
            Class<?>[] paramTypes = new Class<?>[ARG.paramTypes.size()];
            boolean parseDefaultValue = ARG.params.size() < paramTypes.length;
            for (int i = 0; i < ARG.paramTypes.size(); i++) {
                // 解析默认值，默认值要么都填写，要么都不填写
                String paramType = parseParamType(i, ARG.paramTypes.get(i), parseDefaultValue);
                try {
                    paramTypes[i] = Utils.parseClass(paramType);
                } catch (Exception e) {
                    debugOutput(ExceptionUtil.stacktraceToString(e, Integer.MAX_VALUE));
                    result = "param type not found: " + paramType;
                    return;
                }
            }
            debugOutput("parse parameter types success");
            debugOutput("getting method ignore case by method name and param types");
            method = ReflectUtil.getMethod(clazz, true, ARG.methodName, paramTypes);
        }

        if (Objects.isNull(method) || !Modifier.isPublic(method.getModifiers())) {
            String msg = "static method not found(ignore case) or is not a public method: %s#%s(%s)";
            String[] paramTypeArray = ARG.paramTypes.toArray(new String[0]);
            debugOutput(msg, clazz.getName(), ARG.methodName, ArrayUtil.join(paramTypeArray, ", "));
            ARG.methodName = "";
            resolveResultByClassMethod(clazz, fixName, methodAliasPaths);
            return;
        }

        Parameter[] parameters = method.getParameters();
        outputConverter = method.getAnnotation(IOConverter.class);
        debugOutput("get method success");

        if (ARG.params.size() < parameters.length) {
            ARG.params.add(ClipboardUtil.getStr());
        }
        if (ARG.params.size() < parameters.length) {
            try {
                String methodFullInfo = parseMethodFullInfo(clazz.getName(), method.getName(), ARG.paramTypes);
                result = "parameter error, method request: " + methodFullInfo;
            } catch (Exception e) {
                String[] paramTypeArray = Arrays.stream(parameters).map(x -> x.getType().getName()).toArray(String[]::new);
                result = "parameter error, required: (" + ArrayUtil.join(paramTypeArray, ", ") + ")";
            }
            return;
        }

        // 转换参数类型
        debugOutput("casting parameter to class type");
        Object[] params = new Object[parameters.length];
        StringJoiner paramJoiner = new StringJoiner(", ");
        for (int i = 0; i < parameters.length; i++) {
            String param = ARG.params.get(i);
            paramJoiner.add(param);
            Parameter parameter = parameters[i];
            params[i] = castParam2JavaType(parameter.getAnnotation(IOConverter.class), param, parameter.getType(), true);
        }

        debugOutput("cast parameter success");
        debugOutput("invoking method: %s#%s(%s)", clazz.getName(), method.getName(), paramJoiner);
        result = ReflectUtil.invokeStatic(method, params);
        debugOutput("invoke method success");
    }

    private static String parseParamType(int index, String paramType, boolean parseDefaultValue) {
        int idx = paramType.indexOf('=');
        if (idx < 1) {
            return paramType;
        }

        String type = paramType.substring(0, idx);

        // 解析默认值
        if (parseDefaultValue) {
            String param = paramType.substring(idx + 1);
            ARG.params.add(Math.min(index, ARG.params.size()), param);
        }

        return type;
    }

    private static Method autoMatchMethod(Class<?> clazz) {
        // 找到与方法名一致的方法（忽略大小写）
        Method[] methods = clazz.getMethods();
        List<Method> fuzzyList = new ArrayList<>();
        for (Method method : methods) {
            int modifiers = method.getModifiers();
            if (!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers)) {
                continue;
            }
            if (method.getName().equalsIgnoreCase(ARG.methodName)) {
                fuzzyList.add(method);
            }
        }

        if (Utils.isCollectionEmpty(fuzzyList)) {
            return null;
        }

        // 找到离参数个数最相近的方法
        int paramSize = ARG.params.size();
        fuzzyList.sort(Comparator.comparingInt(Method::getParameterCount));
        Method method = null;
        for (Method m : fuzzyList) {
            if (Objects.isNull(method)) {
                method = m;
            }
            if (m.getParameterCount() > paramSize) {
                break;
            }
            method = m;
        }

        // 确定方法参数类型
        if (Objects.nonNull(method)) {
            ARG.paramTypes = new ArrayList<>();
            for (Class<?> paramType : method.getParameterTypes()) {
                ARG.paramTypes.add(paramType.getName());
            }
        }
        return method;
    }

    public static Object castParam2JavaType(IOConverter inputConverter, String param, Class<?> type, boolean replace) {
        if ("nil".equals(param)) {
            return null;
        }

        if (replace && resultContainer != null) {
            // 替换连续命令中的结果记录值，格式：\\0,\\1,\\2...
            for (int i = 0; i < resultContainer.size(); i++) {
                String key = "\\\\" + i;
                String value = resultContainer.get(i);
                param = param.replace(key, value);
            }
        }

        if (Objects.isNull(inputConverter) && CharSequence.class.isAssignableFrom(type)) {
            return param;
        }

        // 转换参数类型
        Converter<?> converter = null;
        try {
            converter = getConverter(inputConverter, type);
            if (converter != null) {
                debugOutput("cast param[%s] using converter: %s", param, converter.getClass().getName());
                return converter.string2Object(param);
            }
        } catch (Exception e) {
            Objects.requireNonNull(converter);
            debugOutput("cast param[%s] to type[%s] using converter[%s] failed: %s", param, type.getName(), converter.getClass().getName(), ExceptionUtil.stacktraceToString(e, Integer.MAX_VALUE));
        }
        debugOutput("auto convert param[%s] to type: %s", param, type.getName());
        return TypeUtils.cast(param, type, null);
    }

    @SuppressWarnings({"unchecked"})
    private static Converter<?> getConverter(IOConverter inputConverter, Class<?> type) throws Exception {
        if (Objects.nonNull(inputConverter)) {
            return Converter.getConverter(inputConverter, type);
        }

        if (List.class.isAssignableFrom(type)) {
            return new ListStringConverter();
        }
        if (Set.class.isAssignableFrom(type)) {
            return new SetStringConverter();
        }
        if (type.isArray()) {
            return new ArrayConverter(type);
        }

        String converterName = getAlias("", homeDir, CONVERTER_JSON).getString(type.getName());
        if (Utils.isStringEmpty(converterName)) {
            return null;
        }
        return Converter.newConverter((Class<? extends Converter<?>>) Utils.parseClass(converterName), type);
    }

    private static void fixMethodName(boolean fixName, List<String> methodAliasPaths) {
        // 从方法别名文件中找到方法名
        if (fixName && !Utils.isCollectionEmpty(methodAliasPaths)) {
            String methodKey = "methodName";
            JSONObject aliasJson = getAlias(ARG.methodName, "", methodAliasPaths.toArray(new String[0]));

            JSONObject methodJson = aliasJson.getJSONObject(ARG.methodName);
            if (Objects.nonNull(methodJson)) {
                String methodName = methodJson.getString(methodKey);
                if (!Utils.isStringEmpty(methodName)) {
                    ARG.methodName = methodName;
                }
                parseMethod(methodJson);
                debugOutput("get method name: %s", ARG.methodName);
                omitParamType = false;
            }
        }
    }

    private static void seeUsage() {
        System.out.println();
        if (Objects.isNull(ARG)) {
            ARG = new MethodArg();
        }
        if (Objects.isNull(commander)) {
            commander = JCommander.newBuilder().addObject(ARG).build();
        }
        commander.usage();
    }

    private static void seeAlias(String className, String... paths) {
        // 用户自定义别名会覆盖工作目录定义的别名
        JSONObject aliasJson = getAlias("", homeDir, paths);
        aliasJson.putAll(getAlias("", "", paths));
        StringJoiner joiner = new StringJoiner("\n");
        Holder<Integer> maxLength = Holder.of(0);
        Map<String, String> map = new TreeMap<>();

        aliasJson.keySet().forEach(k -> {
            int length = k.length();
            if (length > maxLength.get()) {
                maxLength.set(length);
            }

            JSONObject json = aliasJson.getJSONObject(k);
            if (json.containsKey(CLAZZ_KEY)) {
                // 类别名
                map.put(k, json.getString(CLAZZ_KEY));
            } else {
                // 类方法别名或方法别名字
                String methodName = json.getString("method");
                if (Utils.isStringEmpty(methodName)) {
                    methodName = json.getString("methodName");
                }
                ARG.methodName = methodName;
                parseMethod(json);
                try {
                    // 拿到方法的形参名称，参数类型
                    map.put(k, parseMethodFullInfo(className, ARG.methodName, ARG.paramTypes));
                } catch (Exception e) {
                    // 这里只能输出方法参数类型，无法输出形参类型
                    debugOutput("parse method param name error: %s", ExceptionUtil.stacktraceToString(e, Integer.MAX_VALUE));
                    String typeString = ArrayUtil.join(ARG.paramTypes.toArray(new String[0]), ", ");
                    map.put(k, methodName + "(" + typeString + ")");
                }
            }
        });

        // 输出别名到终端
        debugOutput("max length: %s", maxLength.get());
        map.forEach((k, v) -> joiner.add(Utils.padAfter(k, maxLength.get(), ' ') + " = " + v));
        result = joiner.toString();
    }

    private static void parseMethod(JSONObject json) {
        if (ARG.methodName.endsWith(")")) {
            int idx = ARG.methodName.indexOf("(");
            if (idx < 1) {
                debugOutput("method format error: " + ARG.methodName);
                return;
            }
            String[] split = ARG.methodName.substring(idx + 1, ARG.methodName.length() - 1).split(",");
            ARG.paramTypes = Arrays.stream(split).filter(e -> !Utils.isStringEmpty(e)).collect(Collectors.toList());
            ARG.methodName = ARG.methodName.substring(0, idx);
        } else if (Objects.nonNull(json)) {
            List<String> paramTypes = json.getObject(PARAM_KEY, new TypeReference<List<String>>() {});
            ARG.paramTypes = ObjectUtil.defaultIfNull(paramTypes, Collections.emptyList());
        }

        if (Objects.isNull(json)) {
            return;
        }

        // 重载方法
        String specificTypes = json.getString(ARG.params.size() + "param");
        if (Utils.isStringEmpty(specificTypes)) {
            return;
        }
        ARG.paramTypes = Arrays.asList(specificTypes.split(","));
    }

    private static String parseMethodFullInfo(String className, String methodName, List<String> paramTypes) throws NotFoundException {
        String mn = methodName;
        boolean outClassName = false;
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass;
        if (Utils.isStringEmpty(className)) {
            // 来自类方法别名
            int idx = methodName.indexOf("#");
            ctClass = pool.get(Utils.parseClassName(methodName.substring(0, idx)));
            mn = methodName.substring(idx + 1);
            outClassName = true;
        } else {
            // 来自方法别名
            ctClass = pool.get(className);
        }

        // 用javassist库解析形参类型
        CtClass[] params = new CtClass[paramTypes.size()];
        Map<String, String> defaultValueMap = new HashMap<>(4, 1);
        for (int i = 0; i < paramTypes.size(); i++) {
            String paramTypeClass = paramTypes.get(i);
            int idx = paramTypeClass.indexOf("=");
            if (idx > 0) {
                String old = paramTypeClass;
                paramTypeClass = Utils.parseClassName(old.substring(0, idx));
                defaultValueMap.put(paramTypeClass + i, old.substring(idx + 1));
            } else {
                paramTypeClass = Utils.parseClassName(paramTypeClass);
            }
            params[i] = pool.get(paramTypeClass);
        }
        CtMethod ctMethod = ctClass.getDeclaredMethod(mn, params);
        return (outClassName ? ctClass.getName() + "#" : "") + Utils.getMethodFullInfo(ctMethod, defaultValueMap);
    }

    private static String convertResult() {
        debugOutput("converting result");
        try {
            String res = convertResult(result, outputConverter);
            debugOutput("result convert success");
            return res;
        } catch (Exception e) {
            debugOutput("convert result error: " + e.getMessage());
        }
        return "";
    }

    @SuppressWarnings({"unchecked"})
    public static String convertResult(Object obj, IOConverter ioConverter) throws Exception {
        if (Objects.isNull(obj)) {
            return "";
        }

        Class<?> resClass = obj.getClass();
        if (Objects.isNull(ioConverter)) {
            if (obj instanceof CharSequence) {
                return obj.toString();
            }
            if (obj instanceof File) {
                return new FileConverter().object2String(obj);
            }
            if (obj instanceof Date) {
                return new DateConverter().object2String(obj);
            }
            if (obj instanceof Map) {
                return new MapConverter().object2String(obj);
            }
            if (obj instanceof Double) {
                return String.format("%.2f", obj);
            }
            if (obj instanceof Charset) {
                return new CharsetConverter().object2String(obj);
            }
            if (obj instanceof Collection) {
                return new ListStringConverter().object2String(obj);
            }
            if (obj instanceof Pattern) {
                return new PatternConverter().object2String(obj);
            }
            if (obj instanceof Week) {
                return new WeekConverter().object2String(obj);
            }
            if (obj.getClass().isArray()) {
                return new ArrayConverter(resClass).object2String(obj);
            }
        }

        if (Objects.nonNull(ioConverter)) {
            return Converter.getConverter(ioConverter, resClass).object2String(obj);
        }

        String name = resClass.getName();
        JSONObject converterJson = getAlias("", homeDir, CONVERTER_JSON);
        String converterName = converterJson.getString(name);

        if (Utils.isStringEmpty(converterName)) {
            return ObjectUtil.toString(obj);
        }
        return Converter.newConverter((Class<? extends Converter<?>>) Utils.parseClass(converterName), resClass).object2String(obj);
    }

    public static JSONObject getAlias(String aliasKey, String parentDir, String... paths) {
        // 先查找用户自定义别名，没找到再从工作目录查找
        if (Utils.isStringEmpty(parentDir)) {
            parentDir = HUTOOL_USER_HOME;
        }
        String path = Paths.get(parentDir, paths).toAbsolutePath().normalize().toString();
        debugOutput("alias json file path: %s", path);

        JSONObject json = ALIAS_CACHE.get(path);
        if (json == null) {
            if (FileUtil.exist(path)) {
                json = JSON.parseObject(FileUtil.readUtf8String(path));
            } else {
                json = new JSONObject();
            }
            ALIAS_CACHE.put(path, json);
        }

        if (Utils.isStringEmpty(aliasKey) || json.containsKey(aliasKey)) {
            return json;
        }

        return getAlias("", homeDir, paths);
    }

    public static void debugOutput(String msg, Object... params) {
        if (isDebug()) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String className = stackTrace.length > 2 ? stackTrace[2].getClassName() : "Unknown";
            String lineNumber = stackTrace.length > 2 ? String.valueOf(stackTrace[2].getLineNumber()) : "NaN";
            msg = getSimpleDateFormat().format(new Date()) + " " + className + ":" + lineNumber + " - " + String.format(msg, params);
            System.out.println(msg);
        }
    }

    public static String test(String cmd, Object... formatArgs) {
        cmd = String.format(cmd, formatArgs);
        int len = cmd.length() + 10;
        char[] cs = new char[len + 1];
        Arrays.fill(cs, '=');
        cs[0] = '\n';
        String separator = new String(cs);
        System.out.println(separator + "\n>> hu " + cmd + " <<" + separator);
        Hutool.main((cmd + " --work-dir " + Paths.get(".").toAbsolutePath().normalize().toString()).split(" "));
        return Hutool.resultString;
    }
}
