package org.code4everything.hutool;

import cn.hutool.core.date.ChineseDate;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.LocalVariableAttribute;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * @author pantao
 * @since 2020/10/30
 */
public final class Utils {

    private static boolean notExistsExternalClassPath = true;

    private static JSONObject classAliasJson = null;

    private Utils() {}

    public static String lunar(Date date) {
        return new ChineseDate(date).toString();
    }

    public static long date2Millis(Date date) {
        return Objects.isNull(date) ? 0 : date.getTime();
    }

    public static String toUpperCase(String str) {
        return StrUtil.str(str).toUpperCase();
    }

    public static String toLowerCase(String str) {
        return StrUtil.str(str).toLowerCase();
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
        String converterPrefix = "org.code4everything.hutool.converter.";
        if (className.startsWith(converterPrefix)) {
            try {
                return Class.forName(className);
            } catch (Exception e) {
                String path = "file:" + Hutool.workDir + File.separator + "converter" + File.separator;
                try (URLClassLoader loader = new URLClassLoader(new URL[]{new URL(path)}, Utils.class.getClassLoader())) {
                    return loader.loadClass(className);
                }
            }
        }
        return Class.forName(parseClassName0(className));
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
            case "regex.pattern":
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
        if (Hutool.classNameParsed) {
            return className;
        }
        if (Objects.isNull(classAliasJson)) {
            classAliasJson = Hutool.getAlias("", Hutool.workDir, Hutool.CLASS_JSON);
            classAliasJson.putAll(Hutool.getAlias("", "", Hutool.CLASS_JSON));
        }
        JSONObject classJson = classAliasJson.getJSONObject(className);
        if (Objects.nonNull(classJson)) {
            String className0 = classJson.getString(Hutool.CLAZZ_KEY);
            if (StrUtil.isNotEmpty(className0)) {
                className = className0;
            }
        }
        return className;
    }

    public static String outputPublicStaticMethods(String className) {
        return StrUtil.isBlank(className) ? StrUtil.EMPTY : outputPublicStaticMethods0(className);
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
            Hutool.debugOutput("parse class static methods error: {}", ExceptionUtil.stacktraceToString(e, Integer.MAX_VALUE));
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
            if (Objects.nonNull(defaultValueMap)) {
                String defaultValue = defaultValueMap.get(paramType);
                if (StrUtil.isNotEmpty(defaultValue)) {
                    paramStr += "=" + defaultValue;
                }
            }
            paramJoiner.add(paramStr);
        }

        return StrUtil.format("{}({})", method.getName(), paramJoiner.toString());
    }
}
