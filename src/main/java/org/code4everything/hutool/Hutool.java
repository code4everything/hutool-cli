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

import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author pantao
 * @since 2020/10/27
 */
public final class Hutool {

    private static final MethodArg ARG = new MethodArg();

    private static final String CLASS_PREFIX = "cn.hutool.";

    private static final String ALIAS = "alias";

    private static final String PARAM_KEY = "paramTypes";

    private static final String CLAZZ_KEY = "clazz";

    private static final String COMMAND_JSON = "command.json";

    private static final String CLASS_JSON = "class.json";

    private static final String VERSION = "v1.0";

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

        if (ARG.paramFromClipboard) {
            ARG.params.add(ClipboardUtil.getStr());
        }

        debugOutput("received command line arguments: {}", Arrays.asList(args));
        debugOutput("handling result");
        ARG.command.addAll(ARG.main);
        handleResult();
        debugOutput("result handled success");

        if (Objects.isNull(result)) {
            return;
        }
        String resultString = StrUtil.toString(result);
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

        debugOutput("parsing parameter types");
        Class<?>[] paramTypes = new Class<?>[ARG.paramTypes.size()];
        for (int i = 0; i < ARG.paramTypes.size(); i++) {
            String paramType = ARG.paramTypes.get(i);
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
        if (ArrayUtil.isEmpty(paramTypes)) {
            debugOutput("getting method ignore case by method name");
            method = ReflectUtil.getMethodByNameIgnoreCase(clazz, ARG.methodName);
            if (Objects.nonNull(method)) {
                ARG.paramTypes.clear();
                paramTypes = method.getParameterTypes();
                for (Class<?> paramType : paramTypes) {
                    ARG.paramTypes.add(paramType.getName());
                }
            }
        } else {
            debugOutput("getting method ignore case by method name and param types");
            method = ReflectUtil.getMethod(clazz, true, ARG.methodName, paramTypes);
        }
        if (Objects.isNull(method)) {
            String msg = "static method not found(ignore case): {}#{}({})";
            String[] paramTypeArray = ARG.paramTypes.toArray(new String[0]);
            debugOutput(msg, clazz.getName(), ARG.methodName, ArrayUtil.join(paramTypeArray, ","));
            ARG.methodName = StrUtil.EMPTY;
            handleResultOfMethod(clazz, fixName, methodAliasPaths);
            return;
        }
        debugOutput("get method success");

        if (ARG.params.size() < paramTypes.length) {
            String[] paramTypeArray = ARG.paramTypes.toArray(new String[0]);
            Console.log("parameter error, required: ({})", ArrayUtil.join(paramTypeArray, ","));
            return;
        }

        debugOutput("casting parameter to class type");
        ParserConfig parserConfig = new ParserConfig();
        Object[] params = new Object[paramTypes.length];
        StringJoiner paramJoiner = new StringJoiner(",");
        for (int i = 0; i < paramTypes.length; i++) {
            String param = ARG.params.get(i);
            paramJoiner.add(param);
            params[i] = TypeUtils.cast(param, paramTypes[i], parserConfig);
        }
        debugOutput("cast parameter success");
        debugOutput("invoking method: {}#{}({})", ARG.className, method.getName(), paramJoiner);
        result = ReflectUtil.invokeStatic(method, params);
        debugOutput("invoke method success");
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
                String typeString = ArrayUtil.join(paramTypes.toArray(new String[0]), ",");
                map.put(k, StrUtil.format("{}({})", methodName, typeString));
            }
        });

        debugOutput("max length: {}", maxLength.get());
        map.forEach((k, v) -> joiner.add(StrUtil.padAfter(k, maxLength.get(), ' ') + " = " + v));
        result = joiner;
    }

    private static JSONObject getAlias(String... paths) {
        String path = Paths.get(".", paths).toAbsolutePath().normalize().toString();
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
