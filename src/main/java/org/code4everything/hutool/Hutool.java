package org.code4everything.hutool;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
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
public class Hutool {

    private static final MethodArg ARG = new MethodArg();

    private static final String CLASS_PREFIX = "cn.hutool.";

    private static JCommander commander;

    private static Object result;

    public static void main(String[] args) {
        commander = JCommander.newBuilder().addObject(ARG).build();
        commander.setProgramName("hutool-cli");

        try {
            commander.parse(args);
        } catch (Exception e) {
            commander.usage();
            return;
        }

        if (ArrayUtil.isEmpty(args)) {
            commander.usage();
            return;
        }

        debugOutput("received command line arguments: {}", Arrays.asList(args));
        handleResult();

        String resultString = StrUtil.toString(result);
        if (ARG.copy) {
            ClipboardUtil.setStr(resultString);
        }
        Console.log();
        Console.log(resultString);
    }

    private static void handleResult() {
        boolean fixClassName = true;

        if (CollUtil.isNotEmpty(ARG.command)) {
            String methodKey = "method";
            String paramKey = "paramTypes";
            JSONObject aliasJson = getAlias("command.json");

            String command = ARG.command.get(0);
            JSONObject methodJson = aliasJson.getJSONObject(command);
            if (Objects.isNull(methodJson) || !methodJson.containsKey(methodKey)) {
                Console.log("command[{}] not found!", command);
                return;
            }

            String classMethod = methodJson.getString(methodKey);
            if (!classMethod.contains("#")) {
                Console.log("method[{}] format error, required: com.example.Main#main", classMethod);
                return;
            }

            int idx = classMethod.lastIndexOf('#');
            ARG.className = classMethod.substring(0, idx);
            ARG.methodName = classMethod.substring(idx + 1);

            List<String> paramTypes = methodJson.getObject(paramKey, new TypeReference<List<String>>() {});
            ARG.paramTypes = ObjectUtil.defaultIfNull(paramTypes, Collections.emptyList());
            ARG.params.addAll(ListUtil.sub(ARG.command, 1, ARG.command.size()));
            fixClassName = false;
        }

        handleResultOfClass(fixClassName);
    }

    private static void handleResultOfClass(boolean fixName) {
        if (StrUtil.isEmpty(ARG.className)) {
            commander.usage();
            return;
        }

        List<String> methodAliasPaths = null;
        if (fixName) {
            String classKey = "clazz";
            String methodAliasKey = "methodAliasPaths";
            JSONObject aliasJson = getAlias("class.json");

            JSONObject clazzJson = aliasJson.getJSONObject(ARG.className);
            if (Objects.isNull(clazzJson) || !clazzJson.containsKey(classKey)) {
                ARG.className = StrUtil.addPrefixIfNot(ARG.className, CLASS_PREFIX);
                fixName = false;
            } else {
                ARG.className = clazzJson.getString(classKey);
                methodAliasPaths = clazzJson.getObject(methodAliasKey, new TypeReference<List<String>>() {});
            }
        }

        Class<?> clazz;
        try {
            clazz = Class.forName(ARG.className);
        } catch (ClassNotFoundException e) {
            debugOutput(ExceptionUtil.stacktraceToString(e, Integer.MAX_VALUE));
            ARG.className = StrUtil.EMPTY;
            handleResultOfClass(false);
            return;
        }

        handleResultOfMethod(clazz, fixName, methodAliasPaths);
    }

    private static void handleResultOfMethod(Class<?> clazz, boolean fixName, List<String> methodAliasPaths) {
        if (StrUtil.isEmpty(ARG.methodName)) {
            commander.usage();
            return;
        }

        fixMethodName(fixName, methodAliasPaths);

        StringJoiner paramJoiner = new StringJoiner(",");
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
            paramJoiner.add(paramType);
        }

        Method method;
        if (ArrayUtil.isEmpty(paramTypes)) {
            method = ReflectUtil.getMethodByNameIgnoreCase(clazz, ARG.methodName);
            if (Objects.nonNull(method)) {
                paramTypes = method.getParameterTypes();
            }
        } else {
            method = ReflectUtil.getMethod(clazz, true, ARG.methodName, paramTypes);
        }

        if (Objects.isNull(method)) {
            String msg = "static method not found(ignore case): {}#{}({})";
            debugOutput(msg, clazz.getName(), ARG.methodName, paramJoiner);
            ARG.methodName = StrUtil.EMPTY;
            handleResultOfMethod(clazz, fixName, methodAliasPaths);
            return;
        }

        if (ARG.params.size() < paramTypes.length) {
            Console.log("parameter error, required: {}", Arrays.asList(paramTypes));
            return;
        }

        ParserConfig parserConfig = new ParserConfig();
        Object[] params = new Object[paramTypes.length];
        paramJoiner = new StringJoiner(",");
        for (int i = 0; i < paramTypes.length; i++) {
            String param = ARG.params.get(i);
            paramJoiner.add(param);
            params[i] = TypeUtils.cast(param, paramTypes[i], parserConfig);
        }
        debugOutput("invoke method: {}#{}({})", ARG.className, method.getName(), paramJoiner);
        result = ReflectUtil.invokeStatic(method, params);
    }

    private static void fixMethodName(boolean fixName, List<String> methodAliasPaths) {
        if (fixName && CollUtil.isNotEmpty(methodAliasPaths)) {
            String methodKey = "methodName";
            String paramKey = "paramTypes";
            JSONObject aliasJson = getAlias(methodAliasPaths.toArray(new String[0]));

            JSONObject methodJson = aliasJson.getJSONObject(ARG.methodName);
            if (Objects.nonNull(methodJson)) {
                String methodName = methodJson.getString(methodKey);
                if (StrUtil.isNotBlank(methodName)) {
                    ARG.methodName = methodName;
                }
                List<String> paramTypes = methodJson.getObject(paramKey, new TypeReference<List<String>>() {});
                ARG.paramTypes = ObjectUtil.defaultIfNull(paramTypes, Collections.emptyList());
            }
        }
    }

    private static JSONObject getAlias(String... paths) {
        String path = Paths.get(".", paths).toAbsolutePath().normalize().toString();
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
            Console.log();
            Console.log(msg, params);
        }
    }
}
