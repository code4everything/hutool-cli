package org.code4everything.hutool;

import cn.hutool.core.util.StrUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * @author pantao
 * @since 2020/10/30
 */
public final class Utils {

    private Utils() {}

    public static long date2Millis(Date date) {
        return Objects.isNull(date) ? 0 : date.getTime();
    }

    public static String toUpperCase(String str) {
        return StrUtil.str(str).toUpperCase();
    }

    public static String toLowerCase(String str) {
        return StrUtil.str(str).toLowerCase();
    }

    public static String outputPublicStaticMethods(Class<?> clazz) {
        if (Objects.isNull(clazz)) {
            return StrUtil.EMPTY;
        }

        StringJoiner joiner = new StringJoiner("\n");
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            int modifiers = method.getModifiers();
            if (!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers)) {
                continue;
            }

            StringJoiner paramJoiner = new StringJoiner(", ");
            for (Class<?> parameterType : method.getParameterTypes()) {
                paramJoiner.add(parameterType.getName());
            }
            joiner.add(StrUtil.format("{}({})", method.getName(), paramJoiner.toString()));
        }

        return joiner.toString();
    }
}
