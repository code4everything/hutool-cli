package org.code4everything.hutool;

import cn.hutool.core.date.ChineseDate;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.LocalVariableAttribute;

import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * @author pantao
 * @since 2020/10/30
 */
public final class Utils {

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

    public static Class<?> parseClass(String className) throws ClassNotFoundException {
        switch (className) {
            case "boolean":
                return boolean.class;
            case "byte":
                return byte.class;
            case "short":
                return short.class;
            case "int":
                return int.class;
            case "long":
                return long.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            default:
                return Class.forName(className);
        }
    }

    public static String outputPublicStaticMethods(Class<?> clazz) {
        if (Objects.isNull(clazz)) {
            return StrUtil.EMPTY;
        }

        ClassPool pool = ClassPool.getDefault();
        StringJoiner joiner = new StringJoiner("\n");
        try {
            CtClass ctClass = pool.get(clazz.getName());
            for (CtMethod method : ctClass.getMethods()) {
                int modifiers = method.getModifiers();
                if (!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers)) {
                    continue;
                }
                joiner.add(getMethodFullInfo(true, method, null));
            }

        } catch (NotFoundException e) {
            Hutool.debugOutput("parse class static methods error: {}", ExceptionUtil.stacktraceToString(e, Integer.MAX_VALUE));
        }

        return joiner.toString();
    }

    static String getMethodFullInfo(boolean addMethodName, CtMethod method, Map<String, String> defaultValueMap) throws NotFoundException {
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

        return StrUtil.format("{}({})", addMethodName ? method.getName() : "", paramJoiner.toString());
    }
}
