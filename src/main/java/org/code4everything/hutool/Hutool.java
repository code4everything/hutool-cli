package org.code4everything.hutool;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Holder;
import cn.hutool.core.swing.clipboard.ClipboardUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import com.beust.jcommander.JCommander;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.code4everything.hutool.converter.ListStringConverter;
import org.code4everything.hutool.converter.MapConverter;
import org.code4everything.hutool.converter.ObjectPropertyConverter;
import org.code4everything.hutool.converter.SetStringConverter;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

/**
 * @author pantao
 * @since 2020/10/27
 */
public final class Hutool {

    public static final String CLASS_JSON = "class.json";

    public static final String CONVERTER_JSON = "converter.json";

    public static final String CLAZZ_KEY = "clazz";

    private static final String CLASS_PREFIX = "cn.hutool.";

    private static final String ALIAS = "alias";

    private static final String PARAM_KEY = "paramTypes";

    private static final String COMMAND_JSON = "command.json";

    private static final String VERSION = "v1.2";

    private static final String HUTOOL_USER_HOME = System.getProperty("user.home") + File.separator + "hutool-cli";

    public static MethodArg ARG = new MethodArg();

    static String workDir = System.getenv("HUTOOL_PATH");

    static boolean classNameParsed = false;

    private static boolean nonParamType = true;

    private static JCommander commander;

    private static Object result;

    private static List<String> resultContainer = null;

    private static SimpleDateFormat simpleDateFormat = null;

    private Hutool() {}

    public static SimpleDateFormat getSimpleDateFormat() {
        if (simpleDateFormat == null) {
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        }
        return simpleDateFormat;
    }

    public static void setSimpleDateFormat(SimpleDateFormat simpleDateFormat) {
        Hutool.simpleDateFormat = simpleDateFormat;
    }

    private static String handleCmd(String[] args) {
        commander = JCommander.newBuilder().addObject(ARG).build();
        commander.setProgramName("hutool-cli");

        try {
            commander.parse(args);
        } catch (Exception e) {
            seeUsage();
            return "";
        }

        if (ARG.debug && ARG.exception) {
            throw new CliException();
        }

        if (ARG.version) {
            System.out.println("hutool-cli: " + VERSION);
            return "";
        } else {
            debugOutput("hutool-cli: " + VERSION);
        }

        debugOutput("received command line arguments: " + ArrayUtil.toString(args));
        debugOutput("handling result");
        ARG.command.addAll(ARG.main);
        handleResult();
        debugOutput("result handled success");

        if (result == null) {
            return "";
        }
        convertResult();
        String resultString = ObjectUtil.toString(result);
        if (ARG.copy) {
            debugOutput("copying result into clipboard");
            ClipboardUtil.setStr(resultString);
            debugOutput("result copied");
        }
        if (ARG.debug) {
            System.out.println();
        }
        return resultString;
    }

    public static void main(String[] args) {
        if (Utils.isArrayEmpty(args)) {
            seeUsage();
            return;
        }

        List<String> list = new ArrayList<>(8);
        for (String arg : args) {
            if ("//".equals(arg)) {
                String res = handleCmd(list.toArray(new String[0]));
                if (ARG.debug) {
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
            System.out.println(handleCmd(list.toArray(new String[0])));
        }
    }

    private static void handleResult() {
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
                debugOutput("invoke use class name and method name combined in command mode");
                ARG.className = command.substring(0, idx);
                ARG.methodName = command.substring(idx + 1);
                handleResultOfClass(true);
                return;
            }

            String methodKey = "method";
            JSONObject aliasJson = getAlias(command, "", COMMAND_JSON);

            JSONObject methodJson = aliasJson.getJSONObject(command);
            if (Objects.isNull(methodJson) || !methodJson.containsKey(methodKey)) {
                System.out.println("command[" + command + "] not found!");
                return;
            }

            String classMethod = methodJson.getString(methodKey);
            debugOutput("get method: %s", classMethod);
            idx = classMethod.lastIndexOf(sharp);
            if (idx < 1) {
                System.out.println("method[" + classMethod + "] format error, required: com.example.Main#main");
                return;
            }

            debugOutput("parse method to class name and method name");
            ARG.className = classMethod.substring(0, idx);
            ARG.methodName = classMethod.substring(idx + 1);

            List<String> paramTypes = methodJson.getObject(PARAM_KEY, new TypeReference<List<String>>() {});
            ARG.paramTypes = ObjectUtil.defaultIfNull(paramTypes, Collections.emptyList());
            fixClassName = nonParamType = false;
        }

        handleResultOfClass(fixClassName);
    }

    private static void handleResultOfClass(boolean fixName) {
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
            String methodAliasKey = "methodAliasPaths";
            JSONObject aliasJson = getAlias(ARG.className, "", CLASS_JSON);
            classNameParsed = true;

            JSONObject clazzJson = aliasJson.getJSONObject(ARG.className);
            if (Objects.isNull(clazzJson) || !clazzJson.containsKey(CLAZZ_KEY)) {
                ARG.className = Utils.addPrefixIfNot(ARG.className, CLASS_PREFIX);
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
            handleResultOfClass(false);
            return;
        }

        debugOutput("load class success");
        handleResultOfMethod(clazz, fixName, methodAliasPaths);
    }

    private static void handleResultOfMethod(Class<?> clazz, boolean fixName, List<String> methodAliasPaths) {
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
                paramTypes[i] = Utils.parseClass(paramType);
            } catch (Exception e) {
                debugOutput(ExceptionUtil.stacktraceToString(e, Integer.MAX_VALUE));
                System.out.println("param type not found: " + paramType);
                return;
            }
        }
        debugOutput("parse parameter types success");

        Method method;
        if (nonParamType && Utils.isArrayEmpty(paramTypes)) {
            debugOutput("getting method ignore case by method name and param count");
            method = autoMatchMethod(clazz);
        } else {
            debugOutput("getting method ignore case by method name and param types");
            method = ReflectUtil.getMethod(clazz, true, ARG.methodName, paramTypes);
        }
        if (Objects.isNull(method)) {
            String msg = "static method not found(ignore case): %s#%s(%s)";
            String[] paramTypeArray = ARG.paramTypes.toArray(new String[0]);
            debugOutput(msg, clazz.getName(), ARG.methodName, ArrayUtil.join(paramTypeArray, ", "));
            ARG.methodName = "";
            handleResultOfMethod(clazz, fixName, methodAliasPaths);
            return;
        }
        paramTypes = method.getParameterTypes();
        debugOutput("get method success");

        if (ARG.params.size() < paramTypes.length) {
            String[] paramTypeArray = ARG.paramTypes.stream().map(Utils::parseClassName).toArray(String[]::new);
            System.out.println("parameter error, required: (" + ArrayUtil.join(paramTypeArray, ", ") + ")");
            return;
        }

        debugOutput("casting parameter to class type");
        ParserConfig parserConfig = new ParserConfig();
        Object[] params = new Object[paramTypes.length];
        StringJoiner paramJoiner = new StringJoiner(", ");
        JSONObject converterJson = getAlias("", workDir, CONVERTER_JSON);
        for (int i = 0; i < paramTypes.length; i++) {
            String param = ARG.params.get(i);
            paramJoiner.add(param);
            params[i] = castParam2JavaType(converterJson, parserConfig, param, paramTypes[i]);
        }
        debugOutput("cast parameter success");
        debugOutput("invoking method: %s#%s(%s)", ARG.className, method.getName(), paramJoiner);
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

        if (Objects.nonNull(method)) {
            ARG.paramTypes = new ArrayList<>();
            for (Class<?> paramType : method.getParameterTypes()) {
                ARG.paramTypes.add(paramType.getName());
            }
        }
        return method;
    }

    @SuppressWarnings("rawtypes")
    private static Object castParam2JavaType(JSONObject convertJson, ParserConfig parserConfig, String param, Class<?> type) {
        String converterName = convertJson.getString(type.getName());

        if (resultContainer != null) {
            for (int i = 0; i < resultContainer.size(); i++) {
                String key = "\\\\" + i;
                String value = resultContainer.get(i);
                param = param.replace(key, value);
            }
        }

        Converter<?> converter = null;
        try {
            if (List.class.isAssignableFrom(type)) {
                converter = new ListStringConverter();
            } else if (Set.class.isAssignableFrom(type)) {
                converter = new SetStringConverter();
            } else if (!Utils.isStringEmpty(converterName)) {
                Class<?> converterClass = Utils.parseClass(converterName);
                converter = (Converter) ReflectUtil.newInstance(converterClass);
            }
            if (converter != null) {
                debugOutput("cast param[%s] using converter: %s", param, converter.getClass().getName());
                return converter.string2Object(param);
            }
        } catch (Exception e) {
            debugOutput("cast param[%s] to type[%s] using converter[%s] failed: %s", param, type.getName(), converter.getClass().getName(), ExceptionUtil.stacktraceToString(e, Integer.MAX_VALUE));
        }
        debugOutput("auto convert param[%s] to type: %s", param, type.getName());
        return TypeUtils.cast(param, type, parserConfig);
    }

    private static void fixMethodName(boolean fixName, List<String> methodAliasPaths) {
        if (fixName && !Utils.isCollectionEmpty(methodAliasPaths)) {
            String methodKey = "methodName";
            JSONObject aliasJson = getAlias(ARG.methodName, "", methodAliasPaths.toArray(new String[0]));

            JSONObject methodJson = aliasJson.getJSONObject(ARG.methodName);
            if (Objects.nonNull(methodJson)) {
                String methodName = methodJson.getString(methodKey);
                if (!Utils.isStringEmpty(methodName)) {
                    ARG.methodName = methodName;
                }
                debugOutput("get method name: %s", ARG.methodName);
                List<String> paramTypes = methodJson.getObject(PARAM_KEY, new TypeReference<List<String>>() {});
                ARG.paramTypes = ObjectUtil.defaultIfNull(paramTypes, Collections.emptyList());
                nonParamType = false;
            }
        }
    }

    private static void seeUsage() {
        System.out.println();
        commander.usage();
    }

    private static void seeAlias(String className, String... paths) {
        JSONObject aliasJson = getAlias("", workDir, paths);
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
                // class alias
                map.put(k, json.getString(CLAZZ_KEY));
            } else {
                // method alias
                String methodName = json.getString("method");
                if (Utils.isStringEmpty(methodName)) {
                    methodName = json.getString("methodName");
                }

                List<String> paramTypes = json.getObject(PARAM_KEY, new TypeReference<List<String>>() {});
                paramTypes = ObjectUtil.defaultIfNull(paramTypes, Collections.emptyList());
                try {
                    map.put(k, parseMethodFullInfo(className, methodName, paramTypes));
                } catch (Exception e) {
                    debugOutput("parse method param name error: %s", ExceptionUtil.stacktraceToString(e, Integer.MAX_VALUE));
                    String typeString = ArrayUtil.join(paramTypes.toArray(new String[0]), ", ");
                    map.put(k, methodName + "(" + typeString + ")");
                }
            }
        });

        debugOutput("max length: %s", maxLength.get());
        map.forEach((k, v) -> joiner.add(Utils.padAfter(k, maxLength.get(), ' ') + " = " + v));
        result = joiner.toString();
    }

    private static String parseMethodFullInfo(String className, String methodName, List<String> paramTypes) throws NotFoundException {
        String mn = methodName;
        boolean outClassName = false;
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass;
        if (Utils.isStringEmpty(className)) {
            int idx = methodName.indexOf("#");
            ctClass = pool.get(Utils.parseClassName(methodName.substring(0, idx)));
            mn = methodName.substring(idx + 1);
            outClassName = true;
        } else {
            ctClass = pool.get(className);
        }

        CtClass[] params = new CtClass[paramTypes.size()];
        Map<String, String> defaultValueMap = new HashMap<>(4, 1);
        for (int i = 0; i < paramTypes.size(); i++) {
            String paramTypeClass = paramTypes.get(i);
            int idx = paramTypeClass.indexOf("=");
            if (idx > 0) {
                String old = paramTypeClass;
                paramTypeClass = old.substring(0, idx);
                defaultValueMap.put(paramTypeClass, old.substring(idx + 1));
            }
            params[i] = pool.get(Utils.parseClassName(paramTypeClass));
        }
        CtMethod ctMethod = ctClass.getDeclaredMethod(mn, params);
        return (outClassName ? ctClass.getName() + "#" : "") + Utils.getMethodFullInfo(ctMethod, defaultValueMap);
    }

    @SuppressWarnings("rawtypes")
    private static void convertResult() {
        if (Objects.isNull(result) || result instanceof CharSequence) {
            return;
        }

        if (!ARG.formatOutput) {
            autoConvert();
            return;
        }

        String name = result.getClass().getName();
        JSONObject converterJson = getAlias("", workDir, CONVERTER_JSON);
        String converterName = converterJson.getString(name);

        try {
            if (Utils.isStringEmpty(converterName)) {
                for (Map.Entry<String, Object> entry : converterJson.entrySet()) {
                    Class<?> clazz = Utils.parseClass(entry.getKey());
                    if (clazz.isAssignableFrom(result.getClass())) {
                        converterName = entry.getValue().toString();
                        break;
                    }
                }
            }
            Converter<?> converter;
            if (Utils.isStringEmpty(converterName)) {
                converter = new ObjectPropertyConverter();
            } else {
                Class<?> converterClz = Utils.parseClass(converterName);
                converter = (Converter) ReflectUtil.newInstance(converterClz);
            }
            debugOutput("converting result");
            result = converter.object2String(result);
            debugOutput("result convert success");
        } catch (Exception e) {
            debugOutput("converter[%s] not found!", converterName);
        }
    }

    private static void autoConvert() {
        if (result instanceof File) {
            result = ((File) result).getAbsolutePath();
        } else if (result instanceof Date) {
            result = getSimpleDateFormat().format((Date) result);
        } else if (result instanceof Map) {
            result = new MapConverter().object2String(result);
        } else if (result instanceof Double) {
            result = String.format("%.2f", result);
        }
    }

    public static JSONObject getAlias(String aliasKey, String parentDir, String... paths) {
        if (Utils.isStringEmpty(parentDir)) {
            parentDir = HUTOOL_USER_HOME;
        }
        String path = Paths.get(parentDir, paths).toAbsolutePath().normalize().toString();
        debugOutput("alias json file path: %s", path);

        JSONObject json;
        if (FileUtil.exist(path)) {
            json = JSON.parseObject(FileUtil.readUtf8String(path));
        } else {
            json = new JSONObject();
        }

        if (Utils.isStringEmpty(aliasKey) || json.containsKey(aliasKey)) {
            return json;
        }

        return getAlias("", workDir, paths);
    }

    public static void debugOutput(String msg, Object... params) {
        if (ARG.debug) {
            msg = getSimpleDateFormat().format(new Date()) + " debug output: " + String.format(msg, params);
            System.out.println(msg);
        }
    }
}
