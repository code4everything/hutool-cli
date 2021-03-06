package org.code4everything.hutool;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.Holder;
import cn.hutool.core.swing.clipboard.ClipboardUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import com.beust.jcommander.JCommander;
import org.code4everything.hutool.converter.ObjectPropertyConverter;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author pantao
 * @since 2020/10/27
 */
public final class Hutool {

    public static final String CLASS_JSON = "class.json";

    public static final String CONVERTER_JSON = "converter.json";

    public static final String CLAZZ_KEY = "clazz";

    private static final MethodArg ARG = new MethodArg();

    private static final String CLASS_PREFIX = "cn.hutool.";

    private static final String ALIAS = "alias";

    private static final String PARAM_KEY = "paramTypes";

    private static final String COMMAND_JSON = "command.json";

    private static final String VERSION = "v1.1";

    static String workDir = ".";

    private static boolean nonParamType = true;

    private static JCommander commander;

    private static Object result;

    private Hutool() {}

    public static void main(String[] args) {
        commander = JCommander.newBuilder().addObject(ARG).build();
        commander.setProgramName("hutool-cli");

        try {
            commander.parse(args);
        } catch (Exception e) {
            seeUsage();
            return;
        }

        if (ArrayUtil.isEmpty(args)) {
            seeUsage();
            return;
        }

        if (ARG.debug && ARG.exception) {
            throw new CliException();
        }

        if (ARG.version) {
            Console.log();
            Console.log("hutool-cli: {}", VERSION);
            return;
        } else {
            debugOutput("hutool-cli: {}", VERSION);
        }

        debugOutput("received command line arguments: {}", Arrays.asList(args));
        debugOutput("handling result");
        ARG.command.addAll(ARG.main);
        handleResult();
        debugOutput("result handled success");

        if (Objects.isNull(result)) {
            return;
        }
        convertResult();
        String resultString = ObjectUtil.toString(result);
        if (ARG.copy) {
            debugOutput("copying result into clipboard");
            ClipboardUtil.setStr(resultString);
            debugOutput("result copied");
        }
        Console.log();
        Console.log(resultString);
    }

    private static void handleResult() {
        boolean fixClassName = true;

        if (CollUtil.isNotEmpty(ARG.command)) {
            String command = ARG.command.get(0);
            debugOutput("get command: {}", command);
            if (ALIAS.equals(command)) {
                seeAlias(COMMAND_JSON);
                return;
            }

            ARG.params.addAll(ListUtil.sub(ARG.command, 1, ARG.command.size()));

            char sharp = '#';
            int idx = command.indexOf(sharp);
            if (idx > 0) {
                debugOutput("invoke use class name and method name combined in command mode");
                ARG.className = command.substring(0, idx);
                ARG.methodName = command.substring(idx + 1);
                handleResultOfClass(true);
                return;
            }

            String methodKey = "method";
            JSONObject aliasJson = getAlias(COMMAND_JSON);

            JSONObject methodJson = aliasJson.getJSONObject(command);
            if (Objects.isNull(methodJson) || !methodJson.containsKey(methodKey)) {
                Console.log("command[{}] not found!", command);
                return;
            }

            String classMethod = methodJson.getString(methodKey);
            debugOutput("get method: {}", classMethod);
            idx = classMethod.lastIndexOf(sharp);
            if (idx < 1) {
                Console.log("method[{}] format error, required: com.example.Main#main", classMethod);
                return;
            }

            debugOutput("parse method to class name and method name");
            ARG.className = classMethod.substring(0, idx);
            ARG.methodName = classMethod.substring(idx + 1);

            List<String> paramTypes = methodJson.getObject(PARAM_KEY, new TypeReference<List<String>>() {});
            ARG.paramTypes = ObjectUtil.defaultIfNull(paramTypes, Collections.emptyList());
            fixClassName = methodJson.getBooleanValue("allowAlias");
            nonParamType = false;
        }

        handleResultOfClass(fixClassName);
    }

    private static void handleResultOfClass(boolean fixName) {
        if (StrUtil.isEmpty(ARG.className)) {
            seeUsage();
            return;
        }

        if (ALIAS.equals(ARG.className)) {
            seeAlias(CLASS_JSON);
            return;
        }

        List<String> methodAliasPaths = null;
        if (fixName) {
            String methodAliasKey = "methodAliasPaths";
            JSONObject aliasJson = getAlias(CLASS_JSON);

            JSONObject clazzJson = aliasJson.getJSONObject(ARG.className);
            if (Objects.isNull(clazzJson) || !clazzJson.containsKey(CLAZZ_KEY)) {
                ARG.className = StrUtil.addPrefixIfNot(ARG.className, CLASS_PREFIX);
                fixName = false;
            } else {
                ARG.className = clazzJson.getString(CLAZZ_KEY);
                debugOutput("find class alias: {}", ARG.className);
                methodAliasPaths = clazzJson.getObject(methodAliasKey, new TypeReference<List<String>>() {});
            }
        }

        Class<?> clazz;
        debugOutput("loading class: {}", ARG.className);
        try {
            clazz = Class.forName(ARG.className);
        } catch (ClassNotFoundException e) {
            debugOutput(ExceptionUtil.stacktraceToString(e, Integer.MAX_VALUE));
            ARG.className = StrUtil.EMPTY;
            handleResultOfClass(false);
            return;
        }

        debugOutput("load class success");
        handleResultOfMethod(clazz, fixName, methodAliasPaths);
    }

    private static void handleResultOfMethod(Class<?> clazz, boolean fixName, List<String> methodAliasPaths) {
        if (StrUtil.isEmpty(ARG.methodName)) {
            seeUsage();
            return;
        }

        if (ALIAS.equals(ARG.methodName)) {
            if (CollUtil.isNotEmpty(methodAliasPaths)) {
                seeAlias(methodAliasPaths.toArray(new String[0]));
            }
            return;
        }

        fixMethodName(fixName, methodAliasPaths);

        if (ARG.paramIdxFromClipboard >= 0) {
            ARG.params.add(Math.min(ARG.params.size(), ARG.paramIdxFromClipboard), ClipboardUtil.getStr());
        }

        debugOutput("parsing parameter types");
        Class<?>[] paramTypes = new Class<?>[ARG.paramTypes.size()];
        boolean parseDefaultValue = ARG.params.size() < paramTypes.length;
        for (int i = 0; i < ARG.paramTypes.size(); i++) {
            String paramType = parseParamType(i, ARG.paramTypes.get(i), parseDefaultValue);
            // 解析默认值，默认值要么都填写，要么都不填写
            try {
                paramTypes[i] = Class.forName(paramType);
            } catch (ClassNotFoundException e) {
                debugOutput(ExceptionUtil.stacktraceToString(e, Integer.MAX_VALUE));
                Console.log("param type not found: {}", paramType);
                return;
            }
        }
        debugOutput("parse parameter types success");

        Method method;
        if (nonParamType && ArrayUtil.isEmpty(paramTypes)) {
            debugOutput("getting method ignore case by method name and param count");
            method = autoMatchMethod(clazz);
        } else {
            debugOutput("getting method ignore case by method name and param types");
            method = ReflectUtil.getMethod(clazz, true, ARG.methodName, paramTypes);
        }
        if (Objects.isNull(method)) {
            String msg = "static method not found(ignore case): {}#{}({})";
            String[] paramTypeArray = ARG.paramTypes.toArray(new String[0]);
            debugOutput(msg, clazz.getName(), ARG.methodName, ArrayUtil.join(paramTypeArray, ", "));
            ARG.methodName = StrUtil.EMPTY;
            handleResultOfMethod(clazz, fixName, methodAliasPaths);
            return;
        }
        paramTypes = method.getParameterTypes();
        debugOutput("get method success");

        if (ARG.params.size() < paramTypes.length) {
            String[] paramTypeArray = ARG.paramTypes.toArray(new String[0]);
            Console.log("parameter error, required: ({})", ArrayUtil.join(paramTypeArray, ", "));
            return;
        }

        debugOutput("casting parameter to class type");
        ParserConfig parserConfig = new ParserConfig();
        Object[] params = new Object[paramTypes.length];
        StringJoiner paramJoiner = new StringJoiner(", ");
        JSONObject converterJson = getAlias(CONVERTER_JSON);
        for (int i = 0; i < paramTypes.length; i++) {
            String param = ARG.params.get(i);
            paramJoiner.add(param);
            params[i] = castParam2JavaType(converterJson, parserConfig, param, paramTypes[i]);
        }
        debugOutput("cast parameter success");
        debugOutput("invoking method: {}#{}({})", ARG.className, method.getName(), paramJoiner);
        result = ReflectUtil.invokeStatic(method, params);
        debugOutput("invoke method success");
    }

    private static String parseParamType(int index, String paramType, boolean parseDefaultValue) {
        int idx = paramType.indexOf('=');
        if (idx < 1) {
            return paramType;
        }

        String type = paramType.substring(0, idx);

        if (parseDefaultValue) {
            String param = paramType.substring(idx + 1);
            ARG.params.add(Math.min(index, ARG.params.size()), param);
        }

        return type;
    }

    private static Method autoMatchMethod(Class<?> clazz) {
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

        if (CollUtil.isEmpty(fuzzyList)) {
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

        if (Objects.nonNull(method)) {
            ARG.paramTypes = new ArrayList<>();
            for (Class<?> paramType : method.getParameterTypes()) {
                ARG.paramTypes.add(paramType.getName());
            }
        }
        return method;
    }

    @SuppressWarnings("rawtypes")
    private static Object castParam2JavaType(JSONObject convertJson, ParserConfig parserConfig, String param,
                                             Class<?> type) {
        String converterName = convertJson.getString(type.getName());
        if (StrUtil.isNotEmpty(converterName)) {
            try {
                Class<?> converterClass = Class.forName(converterName);
                Converter<?> converter = (Converter) ReflectUtil.newInstance(converterClass);
                return converter.string2Object(param);
            } catch (Exception e) {
                debugOutput("cast param[{}] to type[{}] using converter[{}] failed: {}", param, type.getName(),
                        converterName, ExceptionUtil.stacktraceToString(e, Integer.MAX_VALUE));
            }
        }
        return TypeUtils.cast(param, type, parserConfig);
    }

    private static void fixMethodName(boolean fixName, List<String> methodAliasPaths) {
        if (fixName && CollUtil.isNotEmpty(methodAliasPaths)) {
            String methodKey = "methodName";
            JSONObject aliasJson = getAlias(methodAliasPaths.toArray(new String[0]));

            JSONObject methodJson = aliasJson.getJSONObject(ARG.methodName);
            if (Objects.nonNull(methodJson)) {
                String methodName = methodJson.getString(methodKey);
                if (StrUtil.isNotBlank(methodName)) {
                    ARG.methodName = methodName;
                }
                debugOutput("get method name: {}", ARG.methodName);
                List<String> paramTypes = methodJson.getObject(PARAM_KEY, new TypeReference<List<String>>() {});
                ARG.paramTypes = ObjectUtil.defaultIfNull(paramTypes, Collections.emptyList());
                nonParamType = false;
            }
        }
    }

    private static void seeUsage() {
        Console.log();
        commander.usage();
    }

    private static void seeAlias(String... paths) {
        JSONObject aliasJson = getAlias(paths);
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
                // class alias
                map.put(k, json.getString(CLAZZ_KEY));
            } else {
                // method alias
                String methodName = json.getString("method");
                if (StrUtil.isEmpty(methodName)) {
                    methodName = json.getString("methodName");
                }
                List<String> paramTypes = json.getObject(PARAM_KEY, new TypeReference<List<String>>() {});
                paramTypes = ObjectUtil.defaultIfNull(paramTypes, Collections.emptyList());
                String typeString = ArrayUtil.join(paramTypes.toArray(new String[0]), ", ");
                map.put(k, StrUtil.format("{}({})", methodName, typeString));
            }
        });

        debugOutput("max length: {}", maxLength.get());
        map.forEach((k, v) -> joiner.add(StrUtil.padAfter(k, maxLength.get(), ' ') + " = " + v));
        result = joiner.toString();
    }

    @SuppressWarnings("rawtypes")
    private static void convertResult() {
        if (Objects.isNull(result) || !ARG.formatOutput || result instanceof CharSequence) {
            return;
        }

        String name = result.getClass().getName();
        JSONObject converterJson = getAlias(CONVERTER_JSON);
        String converterName = converterJson.getString(name);

        try {
            if (StrUtil.isEmpty(converterName)) {
                for (Map.Entry<String, Object> entry : converterJson.entrySet()) {
                    Class<?> clazz = Class.forName(entry.getKey());
                    if (clazz.isAssignableFrom(result.getClass())) {
                        converterName = entry.getValue().toString();
                        break;
                    }
                }
            }
            Converter<?> converter;
            if (StrUtil.isEmpty(converterName)) {
                converter = new ObjectPropertyConverter();
            } else {
                Class<?> converterClz = Class.forName(converterName);
                converter = (Converter) ReflectUtil.newInstance(converterClz);
            }
            debugOutput("converting result");
            result = converter.object2String(result);
            debugOutput("result convert success");
        } catch (Exception e) {
            debugOutput("converter[{}] not found!", converterName);
        }
    }

    public static JSONObject getAlias(String... paths) {
        String path = Paths.get(workDir, paths).toAbsolutePath().normalize().toString();
        debugOutput("alias json file path: {}", path);
        String json = null;
        if (FileUtil.exist(path)) {
            json = FileUtil.readUtf8String(path);
        }
        json = StrUtil.emptyToDefault(json, "{}");
        return JSON.parseObject(json);
    }

    private static void debugOutput(String msg, Object... params) {
        if (ARG.debug) {
            msg = DatePattern.NORM_DATETIME_MS_FORMAT.format(DateUtil.date()) + " debug output: " + msg;
            Console.log(msg, params);
        }
    }
}
