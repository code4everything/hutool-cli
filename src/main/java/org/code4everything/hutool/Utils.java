package org.code4everything.hutool;

import cn.hutool.core.date.ChineseDate;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.JarClassLoader;
import cn.hutool.core.math.Calculator;
import cn.hutool.core.util.ReflectUtil;
import com.alibaba.fastjson.JSONObject;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.LocalVariableAttribute;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author pantao
 * @since 2020/10/30
 */
public final class Utils {

    private static JSONObject classAliasJson = null;

    private static JarClassLoader classLoader = null;

    private Utils() {}

    public static Object getStaticFieldValue(Class<?> clazz, String fieldName) {
        Field field = ReflectUtil.getField(clazz, fieldName);
        return ReflectUtil.getStaticFieldValue(field);
    }

    public static List<String> getMatchedItems(Pattern regex, String content) {
        ArrayList<String> result = new ArrayList<>();
        Matcher matcher = regex.matcher(content);
        while (matcher.find()) {
            result.add(matcher.group(0));
        }
        return result;
    }

    public static String getSupperClass(Class<?> clazz) {
        StringJoiner joiner = new StringJoiner("\n");
        getSupperClass(joiner, "", clazz);
        return joiner.toString();
    }

    private static void getSupperClass(StringJoiner joiner, String prefix, Class<?> clazz) {
        if (clazz == null || Object.class == clazz) {
            return;
        }

        joiner.add(prefix + (clazz.isInterface() ? "<>" : "") + clazz.getName());

        getSupperClass(joiner, prefix + "|    ", clazz.getSuperclass());
        for (Class<?> anInterface : clazz.getInterfaces()) {
            getSupperClass(joiner, prefix + "|    ", anInterface);
        }
    }

    public static boolean assignableFrom(Class<?> sourceClass, Class<?> testClass) {
        return sourceClass.isAssignableFrom(testClass);
    }

    public static String calc(String expression, int scale) {
        double res = Calculator.conversion(expression);
        return String.format("%." + scale + "f", res);
    }

    public static String lunar(Date date) {
        return new ChineseDate(date).toString();
    }

    public static long date2Millis(Date date) {
        return Objects.isNull(date) ? 0 : date.getTime();
    }

    public static String toUpperCase(String str) {
        return isStringEmpty(str) ? "" : str.toUpperCase();
    }

    public static String toLowerCase(String str) {
        return isStringEmpty(str) ? "" : str.toLowerCase();
    }

    public static String grep(Pattern pattern, List<String> lines) {
        StringJoiner joiner = new StringJoiner("\n");
        for (String line : lines) {
            if (pattern.matcher(line).find()) {
                joiner.add(line);
            }
        }
        return joiner.toString();
    }

    static <T> boolean isArrayEmpty(T[] arr) {
        return arr == null || arr.length == 0;
    }

    public static Class<?> parseClass(String className) throws Exception {
        switch (className) {
            case "bool":
            case "boolean":
                return boolean.class;
            case "byte":
                return byte.class;
            case "short":
                return short.class;
            case "char":
                return char.class;
            case "int":
                return int.class;
            case "long":
                return long.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            default:
                return parseClass0(className);
        }
    }

    private static Class<?> parseClass0(String className) throws Exception {
        className = parseClassName0(className);
        try {
            return Class.forName(className);
        } catch (Exception e) {
            // ignore
        }

        if (Objects.isNull(classLoader)) {
            classLoader = new JarClassLoader();
            classLoader.addJar(FileUtil.file(Hutool.homeDir, "external"));
            File externalConf = FileUtil.file(Hutool.homeDir, "external.conf");
            if (FileUtil.exist(externalConf)) {
                String[] externalPaths = FileUtil.readUtf8String(externalConf).split(",");
                for (String externalPath : externalPaths) {
                    if (Objects.isNull(externalPath)) {
                        continue;
                    }
                    File external = FileUtil.file(externalPath.trim());
                    if (FileUtil.exist(external)) {
                        classLoader.addURL(external);
                    }
                }
            }
        }

        return classLoader.loadClass(className);
    }

    public static String parseClassName(String className) {
        switch (className) {
            case "bool":
            case "boolean":
                return "boolean";
            case "byte":
                return "byte";
            case "short":
                return "short";
            case "char":
                return "char";
            case "int":
                return "int";
            case "long":
                return "long";
            case "float":
                return "float";
            case "double":
                return "double";
            default:
                return parseClassName0(className);
        }
    }

    private static String parseClassName0(String className) {
        switch (className) {
            case "string":
                return "java.lang.String";
            case "j.char.seq":
                return "java.lang.CharSequence";
            case "file":
                return "java.io.File";
            case "charset":
                return "java.nio.charset.Charset";
            case "date":
                return "java.util.Date";
            case "class":
                return "java.lang.Class";
            case "j.bool":
            case "j.boolean":
                return "java.lang.Boolean";
            case "j.byte":
                return "java.lang.Byte";
            case "j.short":
                return "java.lang.Short";
            case "j.int":
            case "j.integer":
                return "java.lang.Integer";
            case "j.char":
                return "java.lang.Character";
            case "j.long":
                return "java.lang.Long";
            case "j.float":
                return "java.lang.Float";
            case "j.double":
                return "java.lang.Double";
            case "reg.pattern":
                return "java.util.regex.Pattern";
            case "map":
                return "java.util.Map";
            case "list":
                return "java.util.List";
            case "set":
                return "java.util.Set";
            default:
                return parseClassName00(className);
        }
    }

    private static String parseClassName00(String className) {
        if (className.length() > 16) {
            return className;
        }
        if (classAliasJson == null) {
            classAliasJson = Hutool.getAlias("", Hutool.homeDir, Hutool.CLASS_JSON);
            classAliasJson.putAll(Hutool.getAlias("", "", Hutool.CLASS_JSON));
        }
        JSONObject classJson = classAliasJson.getJSONObject(className);
        if (classJson != null) {
            String className0 = classJson.getString(Hutool.CLAZZ_KEY);
            if (!isStringEmpty(className0)) {
                className = className0;
            }
        }
        return className;
    }

    public static boolean isStringEmpty(String str) {
        return str == null || str.length() == 0;
    }

    static <T> boolean isCollectionEmpty(Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }

    static String addPrefixIfNot(String str, String prefix) {
        if (isStringEmpty(str) || isStringEmpty(prefix) || str.startsWith(prefix)) {
            return str;
        }
        return prefix + str;
    }

    static String addSuffixIfNot(String str, String suffix) {
        if (isStringEmpty(str) || isStringEmpty(suffix) || str.endsWith(suffix)) {
            return str;
        }
        return str + suffix;
    }

    public static String padAfter(String str, int len, char pad) {
        if (str == null) {
            str = "";
        }

        int diff = len - str.length();
        if (diff < 1) {
            return str;
        }

        char[] cs = new char[diff];
        for (int i = 0; i < diff; i++) {
            cs[i] = pad;
        }

        return str + new String(cs);
    }

    public static String outputPublicStaticMethods(String className) {
        return isStringEmpty(className) ? "" : outputPublicStaticMethods0(className);
    }

    private static String outputPublicStaticMethods0(String className) {
        ClassPool pool = ClassPool.getDefault();
        StringJoiner joiner = new StringJoiner("\n");
        try {
            CtClass ctClass = pool.get(parseClassName(className));
            CtMethod[] methods = ctClass.getMethods();
            List<String> lineList = new ArrayList<>(methods.length);
            for (CtMethod method : methods) {
                int modifiers = method.getModifiers();
                if (!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers)) {
                    continue;
                }
                lineList.add(getMethodFullInfo(method, null));
            }
            lineList.stream().sorted(String::compareTo).forEach(joiner::add);
        } catch (Exception e) {
            Hutool.debugOutput("parse class static methods error: %s", ExceptionUtil.stacktraceToString(e, Integer.MAX_VALUE));
        }

        return joiner.toString();
    }

    static String getMethodFullInfo(CtMethod method, Map<String, String> defaultValueMap) throws NotFoundException {
        StringJoiner paramJoiner = new StringJoiner(", ");
        LocalVariableAttribute attribute = (LocalVariableAttribute) method.getMethodInfo().getCodeAttribute().getAttribute(LocalVariableAttribute.tag);

        CtClass[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            CtClass parameterType = parameterTypes[i];
            String paramType = parameterType.getName();

            String paramStr = attribute.variableName(i) + ":" + paramType;
            if (defaultValueMap != null) {
                String defaultValue = defaultValueMap.get(paramType + i);
                if (!isStringEmpty(defaultValue)) {
                    paramStr += "=" + defaultValue;
                }
            }
            paramJoiner.add(paramStr);
        }

        return method.getName() + "(" + paramJoiner + ")";
    }
}
